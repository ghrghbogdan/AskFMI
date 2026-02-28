import time
import os
import subprocess

import datetime

def run_crawler():
    print("Starting crawler job...")
    try:
        subprocess.run(["scrapy", "crawl", "secretary"], check=True)
        print("Crawler job finished successfully.")
    except subprocess.CalledProcessError as e:
        print(f"Crawler job failed with error: {e}")

if __name__ == "__main__":
    print("Crawler Scheduler Initialized. Scheduled to run daily at 02:00 AM.")
    
    while True:
        now = datetime.datetime.now()
        target_time = now.replace(hour=2, minute=0, second=0, microsecond=0)
        
        if now >= target_time:
            target_time += datetime.timedelta(days=1)
            
        wait_seconds = (target_time - now).total_seconds()
        
        print(f"Next run scheduled for {target_time} (in {wait_seconds:.2f} seconds)")
        time.sleep(wait_seconds)
        
        run_crawler()
