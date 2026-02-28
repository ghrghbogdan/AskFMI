import sys
import torch
import os
import warnings
import shutil
from transformers import AutoTokenizer, AutoModelForCausalLM, BitsAndBytesConfig, pipeline
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
from langchain_community.document_loaders import DirectoryLoader, TextLoader
from langchain.retrievers import ParentDocumentRetriever
from langchain.storage import InMemoryStore
from langchain_text_splitters import RecursiveCharacterTextSplitter

warnings.filterwarnings("ignore")

MODEL_DIR = "/app/model"
DATA_FOLDER = "./data_facultate" 
DB_PATH = "./chroma_db_parent"
TOP_K_DOCUMENTS = 5  # Numărul de documente relevante de folosit
QUANTIZATION = "4bit"
DEVICE_EMBEDDINGS = "cuda" 

def get_bnb_config():
    if QUANTIZATION == "4bit":
        print("Using 4-bit quantization")
        return BitsAndBytesConfig(
            load_in_4bit=True, bnb_4bit_quant_type="nf4",
            bnb_4bit_use_double_quant=True, bnb_4bit_compute_dtype=torch.float16,
        )
    return BitsAndBytesConfig(load_in_8bit=True)

def load_generation_pipeline():
    print(f"Loading Mistral from {MODEL_DIR}...")
    tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR, use_fast=True, local_files_only=True)
    if tokenizer.pad_token is None: tokenizer.pad_token = tokenizer.eos_token
    
    model = AutoModelForCausalLM.from_pretrained(
        MODEL_DIR, low_cpu_mem_usage=True, device_map="auto",
        quantization_config=get_bnb_config(), local_files_only=True
    )

    return pipeline(
        "text-generation", model=model, tokenizer=tokenizer,
        max_new_tokens=1024,
        temperature=0.1, top_p=0.95, repetition_penalty=1.15, return_full_text=False
    )

def get_retriever():
    if os.path.exists(DB_PATH):
        print("Se sterge vechea baza de date")
        try:
            shutil.rmtree(DB_PATH)
        except Exception as e:
            print(f"Warning la stergere DB: {e}")

    embeddings = HuggingFaceEmbeddings(
        model_name="intfloat/multilingual-e5-large",
        model_kwargs={'device': DEVICE_EMBEDDINGS}
    )

    child_splitter = RecursiveCharacterTextSplitter(chunk_size=400, chunk_overlap=50)
    parent_splitter = RecursiveCharacterTextSplitter(chunk_size=2000, chunk_overlap=200)

    vectorstore = Chroma(
        collection_name="split_parents",
        embedding_function=embeddings,
        persist_directory=DB_PATH
    )

    store = InMemoryStore()

    retriever = ParentDocumentRetriever(
        vectorstore=vectorstore,
        docstore=store,
        child_splitter=child_splitter,
        parent_splitter=parent_splitter,
        search_kwargs={"k": TOP_K_DOCUMENTS}
    )
    return retriever

def index_documents(retriever):
    print(f"Loading files from {DATA_FOLDER}...")
    if not os.path.exists(DATA_FOLDER):
        os.makedirs(DATA_FOLDER)
        print(f"Eroare: Folderul {DATA_FOLDER} nu exista sau e gol!")
        return

    loader = DirectoryLoader(DATA_FOLDER, glob="**/*.txt", loader_cls=TextLoader)
    docs = loader.load()
    
    if not docs:
        print("Folderul e gol. Nu am ce să indexez.")
        return

    print(f"Am găsit {len(docs)} documente. Încep indexarea în batch-uri...")
    
    batch_size = 50  
    total_docs = len(docs)
    
    for i in range(0, total_docs, batch_size):
        # Facem o felie (slice) din lista de documente
        batch_docs = docs[i : i + batch_size]
        
        print(f"Processing batch {i} -> {min(i + batch_size, total_docs)} din {total_docs}...")
        try:
            retriever.add_documents(batch_docs, ids=None)
        except Exception as e:
            print(f"Eroare la batch-ul {i}: {e}")
            # Putem continua cu următorul batch chiar dacă unul crapă
            continue

    print("Indexing complete. Toate batch-urile au fost procesate.")

def init_rag():
    try:
        retriever = get_retriever()
        index_documents(retriever)
        pipe = load_generation_pipeline()
    except Exception as e:
        print(f"Error init: {e}")
        return None, None

    print("\nSystem Ready (Parent Document Retrieval). Type 'exit' to quit.\n")
    return retriever, pipe

def query_rag(query, retriever, pipe):
    if not query:
        return "Te rog să introduci o întrebare."

    relevant_docs = retriever.invoke(query)
    
    if not relevant_docs:
        print("Nu am găsit context relevant.")
        return "Nu am găsit informații relevante în documentele mele despre acest subiect."
    
    # Folosim TOP_K_DOCUMENTS documente (sau mai puține dacă nu există)
    top_docs = relevant_docs[:TOP_K_DOCUMENTS]
    
    # Construim contextul combinat și lista de surse
    context_parts = []
    sources = []
    
    for i, doc in enumerate(top_docs, 1):
        source = doc.metadata.get('source', 'Unknown')
        sources.append(source)
        context_parts.append(f"[Sursa {i}: {source}]\n{doc.page_content}")
    
    print(f"[DEBUG] Surse identificate ({len(top_docs)}): {', '.join(sources)}")
    
    combined_context = "\n\n---\n\n".join(context_parts)
    sources_text = ", ".join([f"Sursa {i+1}: {s}" for i, s in enumerate(sources)])
    
    prompt = f"""[INST] Ești un asistent universitar din cadrul Facultatii de Matematica si Informatica (FMI).
Folosește URMĂTOARELE CONTEXTE din mai multe surse pentru a răspunde complet la întrebare.

SURSE: {sources_text}

CONTEXT:
{combined_context}

ÎNTREBARE: {query}

Răspuns în română (sintetizează informațiile din toate sursele): [/INST]"""

    output = pipe(prompt)
    generated_text = output[0]['generated_text']
    
    print(f"\nRăspuns:\n{generated_text}\n")
    
    return generated_text

def main():
    retriever, pipe = init_rag()
    if not retriever or not pipe:
        return

    while True:
        query = input(">> ")
        if query.lower() in ["exit", "quit"]: break
        query_rag(query, retriever, pipe)

if __name__ == "__main__":
    main()