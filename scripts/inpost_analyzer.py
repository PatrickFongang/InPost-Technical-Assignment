import requests
import time
import logging
from collections import defaultdict
import matplotlib.pyplot as plt

# All logs strictly in English
logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')

def analyze_dirty_data(max_pages=6122):
    url_template = "https://api-global-points.easypack24.net/v1/points?page={}"
    
    clean_to_raws = defaultdict(set)
    raw_counts = defaultdict(int)
    test_lockers = 0

    logging.info(f"Scanning for anomalies across {max_pages} API pages...")

    for page in range(1, max_pages + 1):
        success = False
        for attempt in range(3):
            try:
                response = requests.get(url_template.format(page), timeout=15)
                response.raise_for_status()
                data = response.json()

                items = data.get("items", [])
                if not items:
                    success = True
                    break

                for item in items:
                    raw_city = item.get("address_details", {}).get("city")
                    
                    if raw_city:
                        raw_counts[raw_city] += 1
                        
                        clean_city = raw_city.strip().upper()
                        clean_to_raws[clean_city].add(raw_city)
                        
                        if "TEST" in clean_city or "DO WYKORZYSTANIA" in clean_city:
                            test_lockers += 1

                if page % 100 == 0:
                    logging.info(f"Processed page {page} / {max_pages}...")

                success = True
                time.sleep(0.2) 
                break 

            except Exception as e:
                logging.warning(f"Attempt {attempt + 1}/3 failed for page {page}: {e}")
                time.sleep(2)

        if not success and not items:
             break 

    logging.info("Scanning completed. Generating proof artifacts...")

    dirty_cities = {clean: raws for clean, raws in clean_to_raws.items() if len(raws) > 1}
    
    top_dirty = sorted(dirty_cities.items(), key=lambda x: len(x[1]), reverse=True)[:10]
    
    cities_labels = [c[0] for c in top_dirty]
    variations_counts = [len(c[1]) for c in top_dirty]

    plt.figure(figsize=(12, 6))
    plt.bar(cities_labels, variations_counts, color='#ff4c4c', edgecolor='black')
    plt.ylabel('Number of raw variations')
    plt.title('Dirty Data Proof: Most fragmented city names in InPost API')
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()
    
    chart_filename = 'dirty_data_proof.png'
    plt.savefig(chart_filename)
    plt.close()

    report_filename = 'dirty_data_report.md'
    with open(report_filename, 'w', encoding='utf-8') as f:
        f.write("# 🚨 InPost API Data Pollution Report\n\n")
        f.write("> **Architectural Conclusion:** High name fragmentation makes raw `?city=` queries unreliable. Full in-memory caching and normalization is required.\n\n")
        
        f.write("## 1. Scale of the Problem\n")
        f.write(f"- **Cities with multiple variations:** {len(dirty_cities)}\n")
        f.write(f"- **Garbage lockers (TEST/DO WYKORZYSTANIA):** {test_lockers}\n\n")
        
        f.write("## 2. Visual Proof\n")
        f.write(f"![Pollution Chart]({chart_filename})\n\n")
        
        f.write("## 3. Fragmentation Examples (Top cases)\n")
        f.write("| Normalized City | Raw API Variations | Total Machines |\n")
        f.write("|:---|:---|:---|\n")
        
        for clean_city, raws in top_dirty:
            raws_with_counts = [f"`{r}` ({raw_counts[r]})" for r in raws]
            total_machines = sum(raw_counts[r] for r in raws)
            f.write(f"| **{clean_city}** | {', '.join(raws_with_counts)} | {total_machines} |\n")

    logging.info(f"Done! Check {report_filename} and {chart_filename}")

if __name__ == "__main__":
    analyze_dirty_data()