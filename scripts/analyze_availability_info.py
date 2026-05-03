import json
from collections import defaultdict

def generate_feasibility_report(input_file="../backend/lockers_snapshot.json", output_file="telemetry_feasibility_report.md"):
    print(f"Loading data from snapshot: {input_file}...")
    
    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            grid_cache = json.load(f)
    except FileNotFoundError:
        print(f"Error: File {input_file} not found.")
        return

    stats = defaultdict(lambda: {"total": 0, "A": 0, "B": 0, "C": 0})

    for grid_key, lockers in grid_cache.items():
        for locker in lockers:
            if "parcel_locker" not in locker.get("type", ""):
                continue
            
            country = locker.get("country", "UNKNOWN").upper()

            stats[country]["total"] += 1

            availability = locker.get("locker_availability", {}) 
            details = availability.get("details", {})

            for size in ["A", "B", "C"]:
                status = details.get(size)
                if status and status != "NO_DATA":
                    stats[country][size] += 1

    print("Generating Feasibility Markdown report...")

    md_content = """# 📊 InPost Smart Picker: Feature Feasibility Report (Locker Telemetry)

> **Source:** `lockers_snapshot.json` (Cleaned Geospatial Grid Cache)
> **Objective:** Determine if the `locker_availability` data is reliable and widespread enough to justify building search features around specific box sizes (A, B, C). 
> **Decision Rule:** If the vast majority of lockers return `NO_DATA`, the size-specific filtering feature should be abandoned.

## 1. Executive Summary

This report serves as a Go/No-Go decision metric for the "Locker Availability" filtering feature. We are measuring the percentage of physical service points that actively report real-time telemetry for individual box sizes. A high percentage indicates the feature is viable; a low percentage indicates the API does not support our business logic.

## 2. Telemetry Feasibility by Country

| Country Code | Total Valid Points | Size A Data (%) | Size B Data (%) | Size C Data (%) |
| :--- | :--- | :--- | :--- | :--- |
"""

    for country, data in sorted(stats.items(), key=lambda x: x[1]["total"], reverse=True):
        total = data["total"]
        
        pct_a = (data["A"] / total * 100) if total > 0 else 0
        pct_b = (data["B"] / total * 100) if total > 0 else 0
        pct_c = (data["C"] / total * 100) if total > 0 else 0

        md_content += f"| **{country}** | {total:,} | {pct_a:.2f}% ({data['A']:,}) | {pct_b:.2f}% ({data['B']:,}) | {pct_c:.2f}% ({data['C']:,}) |\n"

    md_content += """
## 3. Business Conclusion

**How to read this data:**
- **Viable (>80%):** If a country shows high data availability, implementing the "Stress-Free" (Size C avoidance) or box-specific search will provide massive value to the users.
- **Not Viable (<50%):** If a country shows poor data availability, relying on `locker_availability` for filtering will result in empty search results for the user. In these regions, the feature should be disabled or deprioritized.
"""

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(md_content)
        
    print(f"Success! Report generated and saved as: {output_file}")

if __name__ == "__main__":
    generate_feasibility_report()