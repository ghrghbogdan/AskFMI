from fastapi import FastAPI, Body
from pydantic import BaseModel
from rag_pipeline import init_rag, query_rag
import uvicorn
import torch
app = FastAPI()

from threading import Lock
rag_lock = Lock()

retriever = None
pipe = None

class QueryRequest(BaseModel):
    query: str

@app.on_event("startup")
def startup():
    global retriever, pipe
    retriever, pipe = init_rag()
    import gc
    gc.collect()
    torch.cuda.empty_cache()

@app.post("/query")
def query_endpoint(payload: QueryRequest):
    with rag_lock:
        if not retriever or not pipe:
            return {"answer": "Serverul nu este inițializat corect."}
        
        raspuns = query_rag(payload.query, retriever, pipe)
        return {"answer": raspuns}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)