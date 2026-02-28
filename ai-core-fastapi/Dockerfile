FROM nvidia/cuda:12.1.0-cudnn8-runtime-ubuntu22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV PYTHONUNBUFFERED=1

# Install Python and dependencies
RUN apt-get update && apt-get install -y \
    python3.10 \
    python3-pip \
    python3-dev \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Create symlinks
RUN ln -s /usr/bin/python3 /usr/bin/python

WORKDIR /app

COPY requirements.txt .

# Install uv
COPY --from=ghcr.io/astral-sh/uv:latest /uv /bin/uv

# Install PyTorch for CUDA 12.1
RUN uv pip install --system --no-cache \
    torch==2.5.1 \
    torchvision==0.20.1 \
    torchaudio==2.5.1 \
    --index-url https://download.pytorch.org/whl/cu121

# Install other dependencies
RUN uv pip install --system --no-cache -r requirements.txt


COPY . .

EXPOSE 8000

CMD ["python", "main.py"]