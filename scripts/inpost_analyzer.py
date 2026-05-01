import requests
import time
import logging
from collections import Counter
import matplotlib.pyplot as plt

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)

def generate_report(max_pages=6122):
    url_template = "https://api-global-points.easypack24.net/v1/points?page={}"
    city_counter = Counter()
    total_lockers = 0

    logging.info(f"Starting API scraping for {max_pages} pages...")

    for page in range(1, max_pages + 1):
        try:
            response = requests.get(url_template.format(page), timeout=10)
            response.raise_for_status()
            data = response.json()

            if page == 1:
                total_lockers = data.get("count", 0)
                logging.info(f"Total lockers found in API: {total_lockers}")

            items = data.get("items", [])
            if not items:
                logging.info("No more items found. Ending pagination.")
                break

            for item in items:
                city = item.get("address_details", {}).get("city")
                if city:
                    city_counter[city] += 1

            if page % 50 == 0:
                logging.info(f"Processed page {page} / {max_pages}...")

            time.sleep(0.2)

        except Exception as e:
            logging.error(f"Failed to fetch page {page}: {e}")

    logging.info("Scraping finished. Processing data...")

    top_50 = city_counter.most_common(50)
    
    if total_lockers == 0:
        total_lockers = sum(city_counter.values())
        
    top_50_sum = sum(count for _, count in top_50)
    total_percentage = (top_50_sum / total_lockers) * 100 if total_lockers > 0 else 0

    logging.info("Generating chart and Markdown report...")

    top_20 = top_50[:20]
    cities = [c[0] for c in top_20]
    counts = [c[1] for c in top_20]

    plt.figure(figsize=(12, 8))
    plt.barh(cities[::-1], counts[::-1], color='#ffcc00', edgecolor='#cc9900') 
    plt.xlabel('Number of Lockers')
    plt.title('Top 20 Cities with Most InPost Lockers in Poland')
    plt.grid(axis='x', linestyle='--', alpha=0.7)
    plt.tight_layout()
    
    chart_filename = 'inpost_top_cities_chart.png'
    plt.savefig(chart_filename)
    plt.close()

    report_filename = 'inpost_report.md'
    with open(report_filename, 'w', encoding='utf-8') as f:
        f.write("# InPost Lockers Distribution Report\n\n")
        
        f.write("## 📊 Overview\n")
        f.write(f"- **Total lockers in Poland:** {total_lockers}\n")
        f.write(f"- **Total lockers in Top 50 cities:** {top_50_sum}\n")
        f.write(f"- **Top 50 Share:** **{total_percentage:.2f}%** of all machines\n\n")
        
        f.write("## 📈 Visual Summary (Top 20)\n")
        f.write(f"![Top 20 Cities Chart]({chart_filename})\n\n")
        
        f.write("## 📋 Top 50 Data Table\n")
        f.write("| Rank | City | Lockers Count | Share (%) |\n")
        f.write("|:----:|:-----|:-------------:|:---------:|\n")
        
        java_list_elements = []
        for rank, (city, count) in enumerate(top_50, 1):
            percentage = (count / total_lockers) * 100 if total_lockers > 0 else 0
            f.write(f"| {rank} | {city} | {count} | {percentage:.2f}% |\n")
            java_list_elements.append(f'"{city}"')
            
        f.write("\n## 💻 Java Integration (Spring Boot)\n")
        f.write("Copy and paste this list directly into your caching Scheduler:\n\n")
        f.write("```java\n")
        f.write("public static final List<String> TOP_CITIES_FOR_CACHE = List.of(\n    ")
        f.write(", ".join(java_list_elements))
        f.write("\n);\n")
        f.write("```\n")

    logging.info(f"Success! Report generated: {report_filename}")
    logging.info(f"Success! Chart generated: {chart_filename}")

if __name__ == "__main__":
    generate_report()