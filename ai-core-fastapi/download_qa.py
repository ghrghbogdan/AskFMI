import json
from datasets import load_dataset
from tqdm import tqdm

def format_dataset():
    dataset_name = "OpenLLM-Ro/ro_sft_ultrachat"
    output_filename = "ro_ultrachat_formatted.json"

    print(f"📥 Începem streaming-ul pentru: {dataset_name}...")
    
    # MODIFICARE MAJORĂ: Adăugăm streaming=True
    # Asta înseamnă că nu descarcă totul în RAM, ci citește pe măsură ce procesează.
    try:
        dataset = load_dataset(dataset_name, split="train", streaming=True)
    except Exception as e:
        print(f"Eroare la conexiune/încărcare: {e}")
        return

    formatted_data = []
    
    print("⚙️  Se procesează datele (acest pas poate dura puțin, dar e sigur)...")

    # Fiind streaming, nu știm lungimea totală exactă din start, deci tqdm va arăta doar numărul procesat
    for row in tqdm(dataset):
        original_messages = row.get('messages') or row.get('conversations')
        
        if not original_messages:
            continue

        new_messages = []
        
        for msg in original_messages:
            role = msg.get('role')
            content = msg.get('content')

            if role == 'human':
                role = 'user'
            elif role in ['gpt', 'bot']:
                role = 'assistant'

            new_messages.append({
                "role": role,
                "content": content
            })

        formatted_data.append({
            "messages": new_messages
        })

    print(f"💾 Se salvează {len(formatted_data)} conversații în {output_filename}...")
    
    with open(output_filename, "w", encoding="utf-8") as f:
        json.dump(formatted_data, f, indent=2, ensure_ascii=False)

    print("✅ Gata! Fișierul a fost generat cu succes.")

if __name__ == "__main__":
    format_dataset()