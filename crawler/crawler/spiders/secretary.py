import datetime
import io
import re
import pdfplumber
import scrapy
import fitz

from ..items import MetadataItem, QueryItem
from urllib.parse import urlparse
from w3lib.html import remove_tags

class SecretarySpider(scrapy.Spider):
    name = "secretary"
    allowed_domains = ["fmi.unibuc.ro", "unibuc.ro"]
    
    custom_settings = {
        'CONCURRENT_REQUESTS': 1,
        'ROBOTSTXT_OBEY': False,

        # for google drive
        'USER_AGENT': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }

    start_urls = [
        "https://fmi.unibuc.ro/prezentare/",
        "https://fmi.unibuc.ro/concurs-mateinfoub-2025/",
        "https://fmi.unibuc.ro/admitere/",
        "https://fmi.unibuc.ro/finalizare-studii/",
        # "https://fmi.unibuc.ro/orar/",
        "https://fmi.unibuc.ro/conducere/",
        "https://fmi.unibuc.ro/secretariat/",
        "https://fmi.unibuc.ro/casierie/",
        "https://fmi.unibuc.ro/regulamente/"
    ]

    IGNORED_EXTENSIONS = [
        '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', 
        '.zip', '.rar', '.jpg', '.jpeg', '.png', '.gif'
    ]

    FORBIDDEN_URLS = [
        'facebook.com', 'instagram.com', 'linkedin.com', 'youtube.com',
        'twitter.com', 'old.fmi.unibuc.ro', 'prezentare/camine', 
        'prezentare/schite/', 'conducerea-facultatii-2019-2023/',
        'anunturi-mateinfoub/', 'noutati/', 'planuri-de-invatamant/',
        'https://admitere.fmi.unibuc.ro', 'cazare/',
        # carte MIUB
        'https://drive.google.com/file/d/1q_3gIfcSsQ0KRT0LRzlUHNC3dQFd9SC7/view',
        'mailto:', 'tel:'
    ]
  
    def start_requests(self):
        for i, url in enumerate(self.start_urls):
            priority = (len(self.start_urls) - i) * 10
            yield scrapy.Request(url=url, callback=self.parse, priority=priority)

    def parse(self, response):
        content_div = response.css('.entry-content')

        if not content_div:
            content_div = response.xpath('//div[contains(@class, "nv-single-page-wrap")]')
        
        if not content_div:
             elements = response.xpath('//body//*[self::h1 or self::h2 or self::h3 or self::p]')
        else:
             elements = content_div.xpath('.//*[self::h1 or self::h2 or self::h3 or self::h4 or self::p or self::ul or self::ol]')

        structured_text = []
        metaData = MetadataItem()
        query = QueryItem()

        metaData['title'] = response.xpath('//h1/text()').get() or response.url
        metaData['date_scraped'] = datetime.datetime.now()
        metaData['url'] = response.url


        for element in elements:
            raw_html = element.get()
            html_with_spaces = raw_html.replace('<br>', ' ').replace('<br/>', ' ').replace('<br />', ' ')
            text_content = remove_tags(html_with_spaces)
            clean_text = " ".join(text_content.replace('\n', ' ').split()).strip()
            
            if not clean_text: 
                continue
             
            tag_name = element.root.tag
            if tag_name in ['h1', 'h2', 'h3', 'h4', 'h5', 'h6']:
                structured_text.append({"type": "heading", "level": tag_name, "content": clean_text})
            elif tag_name == 'p':
                structured_text.append({"type": "paragraph", "content": clean_text})
            elif tag_name in ['ul', 'ol']:
                items_text = [li.xpath('string(.)').get().replace('\n', ' ').strip() for li in element.xpath('.//li') if li.xpath('string(.)').get()]
                if items_text:
                    structured_text.append({"type": "list", "items": items_text})
        
        if structured_text:
            query['metadata'] = metaData
            query['text'] = structured_text
            yield query

        # Navigation
        if content_div:
            links = content_div.css('a::attr(href)').getall()
        else:
            links = response.css('a::attr(href)').getall()
            
        current_priority = response.request.priority

        for link in links:
            absolute_url = response.urljoin(link)
            parsed_url = urlparse(absolute_url)

            if any(forbidden in absolute_url.lower() for forbidden in self.FORBIDDEN_URLS):
                continue
            
            if absolute_url.lower().endswith('.pdf'):
                yield scrapy.Request(
                    url=absolute_url,
                    callback=self.parse_pdf,
                    priority=current_priority,
                    cb_kwargs={'parent_title': metaData['title'], 'parent_url': response.url}
                )

            elif "drive.google.com" in absolute_url or "docs.google.com" in absolute_url:
                file_id_match = re.search(r'/d/([a-zA-Z0-9_-]+)', absolute_url)
                
                if file_id_match:
                    file_id = file_id_match.group(1)
                    download_url = f"https://drive.google.com/uc?export=download&id={file_id}"

                    yield scrapy.Request(
                        url=download_url,
                        callback=self.parse_pdf,
                        priority=current_priority,
                        dont_filter=True, 
                        cb_kwargs={'parent_title': metaData['title'], 'parent_url': response.url}
                    )

            elif self.allowed_domains[0] in parsed_url.netloc:
                path_lower = parsed_url.path.lower()
                if not any(path_lower.endswith(ext) for ext in self.IGNORED_EXTENSIONS):
                    yield scrapy.Request(
                        url=absolute_url, 
                        callback=self.parse, 
                        priority=current_priority
                    )


    def parse_pdf(self, response, parent_title, parent_url):
        FORBIDDEN_PDFS = ['barem', 'solutii', 'concurs',
                         'enunturi', 'clasament',
                         'asezare', 'subiecte', 'barem',
                         'amfiteatre', 'participanti',
                         'lista', 'calificati',
                         'repartizare', 'etapa1', 'etapa2',
                         'premii', 'confirmati', 'clasificare',
                         'rezultate']
        
        content_type = response.headers.get('Content-Type', b'').decode('utf-8').lower()
        
        if 'application/pdf' not in content_type:
            if not response.body.startswith(b'%PDF'):
                print(f"!!!PDF not valid")
                return

        query = QueryItem()
        metaData = MetadataItem()

        filename = "document_google_drive.pdf"
        cd = response.headers.get('Content-Disposition')
        if cd:
            cd = cd.decode('utf-8')
            fname_match = re.search(r'filename="?([^"]+)"?', cd)
            if fname_match:
                filename = fname_match.group(1)

        if any(forbidden in filename.lower() for forbidden in FORBIDDEN_PDFS):
            print(f'!!!PDF forbidden')
            return

        metaData['title'] = f"PDF: {filename} (Sursa: {parent_title})"
        metaData['date_scraped'] = datetime.datetime.now()
        metaData['url'] = parent_url
        
        structured_text = []

        try:
            with fitz.open(stream=io.BytesIO(response.body), filetype="pdf") as doc:
                for page_num, page in enumerate(doc):
                    text = page.get_text().strip()
                    if text:
                        structured_text.append({
                            "type": "pdf_page",
                            "page_number": page_num + 1,
                            "content": " ".join(text.replace('\n', ' ').split())
                        })
        except Exception as e:
            self.logger.error(f"Error: {e}")

        if structured_text:
            query['metadata'] = metaData
            query['text'] = structured_text
            yield query