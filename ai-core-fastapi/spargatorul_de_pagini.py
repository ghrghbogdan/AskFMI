import os
import re

INPUT_FILE = "../context_fmi.txt"
OUTPUT_FOLDER = "./data_facultate"

def clean_filename(text):
    clean = re.sub(r'[\\/*?:"<>|#]', "", text)
    clean = " ".join(clean.split())
    return clean[:60]

def main():
    if not os.path.exists(INPUT_FILE):
        print(f"Eroare: Nu găsesc fișierul {INPUT_FILE}")
        return

    if not os.path.exists(OUTPUT_FOLDER):
        os.makedirs(OUTPUT_FOLDER)
        print(f"Am creat folderul '{OUTPUT_FOLDER}'.")

    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        content = f.read()

    pages = re.split(r'={10,}', content)

    count = 0
    for page in pages:
        if not page.strip():
            continue 

        lines = page.strip().split('\n')
        filename = f"pagina_{count}"
        

        for line in lines:
            if "#####" in line:
                potential_title = line.replace("#", "").strip()
                if len(potential_title) > 3:
                    filename = clean_filename(potential_title)
                    break
        
        if filename.startswith("pagina_"):
            for line in lines:
                clean_line = clean_filename(line)
                if len(clean_line) > 5:
                    filename = clean_line
                    break

        final_filename = f"{filename}_{count}.txt"
        save_path = os.path.join(OUTPUT_FOLDER, final_filename)
        
        with open(save_path, "w", encoding="utf-8") as out:
            out.write(page.strip())
        
        count += 1


if __name__ == "__main__":
    main()