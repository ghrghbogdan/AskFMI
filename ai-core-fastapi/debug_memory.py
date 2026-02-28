import torch
from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig
import os

model_dir = "../../Nemira-LLM/model/romistral_model"

print(f"Loading model from {model_dir} in 4-bit...")

bnb_config = BitsAndBytesConfig(
    load_in_4bit=True,
    bnb_4bit_quant_type="nf4",
    bnb_4bit_use_double_quant=True,
    bnb_4bit_compute_dtype=torch.float16,
)

try:
    model = AutoModelForCausalLM.from_pretrained(
        model_dir,
        quantization_config=bnb_config,
        device_map="auto",
        low_cpu_mem_usage=True
    )
    print("Model loaded.")
    
    print("\nMemory Usage:")
    print(f"Allocated: {torch.cuda.memory_allocated() / 1024**3:.2f} GB")
    print(f"Reserved:  {torch.cuda.memory_reserved() / 1024**3:.2f} GB")
    
    # Check footprint of the model object
    print(f"\nModel footprint: {model.get_memory_footprint() / 1024**3:.2f} GB")
    
    print("\nDevice Map:")
    if hasattr(model, "hf_device_map"):
        print(model.hf_device_map)
    else:
        print("No hf_device_map found (unexpected for device_map='auto').")
        
except Exception as e:
    print(f"Error loading model: {e}")
