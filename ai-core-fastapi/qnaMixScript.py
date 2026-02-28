import re, json, random, argparse, unicodedata, hashlib
from pathlib import Path

MIN_SENT_LEN = 40
MAX_SENT_LEN = 220
MAX_QA_PER_PARAGRAPH_DEFAULT = 2
DIACRITICS = ["ă", "â", "î", "ș", "ț"]
AUTO_LIMIT_RATIO = 0.45

def norm(text: str) -> str:
    text = unicodedata.normalize("NFKC", text).strip()
    return re.sub(r"\s+", " ", text)

def has_ro_markers(t: str) -> bool:
    low = t.lower()
    if any(d in low for d in DIACRITICS):
        return True
    markers = [" este ", " și ", " sunt ", " care ", " într-o ", " unui ", " unei ", " acest ", " această "]
    padded = f" {low} "
    return any(m in padded for m in markers)

def split_sentences(par: str):
    parts = re.split(r'(?<=[\.\?!])\s+', par)
    for s in parts:
        s = s.strip()
        if s:
            yield s

def candidate_sentences(par: str):
    out = []
    for s in split_sentences(par):
        if len(s) < MIN_SENT_LEN or len(s) > MAX_SENT_LEN:
            continue
        if not has_ro_markers(s):
            continue
        out.append(s)
    return out

def sha1(t: str) -> str:
    return hashlib.sha1(t.encode("utf-8")).hexdigest()

def make_question(sentence: str):
    s = sentence.strip()
    m = re.match(r"^([A-ZȘȚĂÂÎ][\w\-\’'\"’]*) (este|reprezintă|a fost) (.+?)[\.\?!]$", s)
    if m:
        subj = m.group(1)
        return f"Ce este {subj}?", subj
    y = re.search(r"(în|din) (\d{4})", s)
    if y:
        ans = y.group(2)
        return "În ce an are loc evenimentul menționat?", ans
    name = re.search(r"\b([A-ZȘȚĂÂÎ][a-zăâîșț]+ [A-ZȘȚĂÂÎ][a-zăâîșț]+)\b", s)
    if name:
        person = name.group(1)
        return "Ce persoană este menționată în text?", person
    org = re.search(r"\b([A-ZȘȚĂÂÎ][a-zăâîșț]+(?:ul|ului))\b", s)
    if org:
        entity = org.group(1)
        return "Ce entitate este menționată?", entity
    frag = s.split(",")[0]
    ans = frag.rstrip(".!?")
    return "Care este ideea principală a propoziției?", ans

def load_mt5():
    from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
    tok = AutoTokenizer.from_pretrained("google/mt5-small")
    model = AutoModelForSeq2SeqLM.from_pretrained("google/mt5-small")
    model.eval()
    return tok, model

def refine_question_mt5(mt5_tok, mt5_model, sentence: str):
    import torch
    prompt = f"Generează o întrebare clară în limba română pornind de la propoziția: {sentence}"
    inp = mt5_tok(prompt, return_tensors="pt")
    with torch.inference_mode():
        gen = mt5_model.generate(**inp, max_new_tokens=40, num_beams=4)
    q = mt5_tok.decode(gen[0], skip_special_tokens=True).strip()
    if not q.endswith("?"):
        q += "?"
    return q

def build_qna(paragraphs, limit, max_qa_per_par, use_mt5=False):
    out = []
    seen_pairs = set()
    if use_mt5:
        mt5_tok, mt5_model = load_mt5()
    else:
        mt5_tok = mt5_model = None
    for par in paragraphs:
        if len(out) >= limit:
            break
        cands = candidate_sentences(par)
        random.shuffle(cands)
        used = 0
        for sent in cands:
            if used >= max_qa_per_par or len(out) >= limit:
                break
            q, ans = make_question(sent)
            if mt5_model:
                try:
                    q = refine_question_mt5(mt5_tok, mt5_model, sent)
                except Exception:
                    pass
            q = norm(q); ans = norm(ans)
            if len(q) < 8 or len(ans) < 3:
                continue
            key = sha1(q + "||" + ans)
            if key in seen_pairs:
                continue
            seen_pairs.add(key)
            out.append({"messages":[{"role":"user","content":q},{"role":"assistant","content":ans}]})
            used += 1
    return out

def read_paragraphs(path: str):
    data = []
    with Path(path).open("r", encoding="utf-8") as f:
        for line in f:
            try:
                obj = json.loads(line)
                txt = obj.get("text", "").strip()
                if txt:
                    data.append(txt)
            except:
                continue
    return data

def parse_args():
    ap = argparse.ArgumentParser()
    ap.add_argument("--input", required=True)
    ap.add_argument("--out", default="ro_qna.json")
    ap.add_argument("--limit", type=int, default=-1)
    ap.add_argument("--seed", type=int, default=42)
    ap.add_argument("--max-per-paragraph", type=int, default=MAX_QA_PER_PARAGRAPH_DEFAULT)
    ap.add_argument("--mt5", action="store_true")
    return ap.parse_args()

def main():
    args = parse_args()
    random.seed(args.seed)
    paragraphs = read_paragraphs(args.input)
    total_pars = len(paragraphs)
    if total_pars == 0:
        print("[INFO] Niciun paragraf.")
        return
    if args.limit < 0:
        args.limit = int(total_pars * AUTO_LIMIT_RATIO)
        print(f"[INFO] AUTO limit={args.limit} (~45% din {total_pars})")
    qna = build_qna(paragraphs, args.limit, args.max_per_paragraph, use_mt5=args.mt5)
    with Path(args.out).open("w", encoding="utf-8") as fw:
        json.dump(qna, fw, ensure_ascii=False, indent=2)
    print(f"[DONE] Q&A={len(qna)} -> {args.out}")

if __name__ == "__main__":
    main()