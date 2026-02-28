import scrapy


class MetadataItem(scrapy.Item):
    title = scrapy.Field()
    date_scraped = scrapy.Field()
    url = scrapy.Field()


class QueryItem(scrapy.Item):
    metadata = scrapy.Field()
    text = scrapy.Field()
