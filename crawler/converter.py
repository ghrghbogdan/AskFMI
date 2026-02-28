import json
import os

def convert_json_to_txt(input_file, output_file):
    if not os.path.exists(input_file):
        print(f"Error: File not found {input_file}.")
        return

    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        with open(output_file, 'w', encoding='utf-8') as f_out:
            
            for entry in data:
                meta = entry.get('metadata', {})
                title = meta.get('title', 'Fără Titlu')
                url = meta.get('url', 'N/A')
                date = str(meta.get('date_scraped', 'N/A'))

                f_out.write(f"##### {title} #####\n")
                f_out.write(f"Sursa URL: {url}\n")
                f_out.write(f"Data accesării: {date}\n")
                f_out.write("-" * 20 + "\n")

                content_list = entry.get('text', [])
                
                for block in content_list:
                    block_type = block.get('type')
                    content = block.get('content', '')

                    if block_type == 'heading':
                        f_out.write(f"\n[SECȚIUNE: {content}]\n")
                    
                    elif block_type == 'paragraph':
                        if content:
                            f_out.write(f"{content}\n")
                    
                    elif block_type == 'list':
                        items = block.get('items', [])
                        for item in items:
                            f_out.write(f"  - {item}\n")
                        f_out.write("\n")

                    elif block_type == 'pdf_page':
                        page_num = block.get('page_number', '?')
                        f_out.write(f"\n--- Pagina {page_num} ---\n")
                        if content:
                            f_out.write(f"{content}\n")

                f_out.write("\n" + "="*50 + "\n\n")

        print(f"Success! Data saved in '{output_file}'.")

    except json.JSONDecodeError:
        print("Error: JSON file format is not valid.")
    except Exception as e:
        print(f"Unexpected error: {e}")


if __name__ == "__main__":
    input_name = 'output.json'
    output_name = 'context_fmi.txt'
    
    convert_json_to_txt(input_name, output_name)