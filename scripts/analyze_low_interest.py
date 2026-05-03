import json
from collections import defaultdict

def generate_low_interest_report(input_file="../lockers_snapshot.json", output_file="low_interest_feasibility_report.md"):
    print(f"Loading data from snapshot: {input_file}...")

    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            grid_cache = json.load(f)
    except FileNotFoundError:
        print(f"Error: File {input_file} not found.")
        return

    all_lockers = defaultdict(set)
    recommended_lockers = defaultdict(set)

    for grid_key, lockers in grid_cache.items():
        for locker in lockers:
            locker_id = locker.get("name")
            if not locker_id:
                continue

            country = locker.get("country", "UNKNOWN").upper()

            all_lockers[country].add(locker_id)

            low_interest_list = locker.get("recommended_low_interest_box_machines_list", [])
            if low_interest_list is not None:
                for rec_id in low_interest_list:
                    if rec_id:
                        recommended_lockers[country].add(str(rec_id))

    print("Generating simplified Feasibility Markdown report...")

    md_content = """# 📊 InPost Smart Picker: 'Low Interest' Feasibility Report

> **Source:** `lockers_snapshot.json` (Cleaned Geospatial Grid Cache)
> **Objective:** Test the hypothesis that lockers simply recommend each other in a loop, rendering the `recommended_low_interest_box_machines_list` useless as an absolute filter.

## 1. Executive Summary

This report analyzes how many unique parcel lockers are actually marked as "low interest" alternatives by other machines. If the vast majority of lockers appear on someone else's "low interest" list, the metric is relative rather than absolute. Using it as a strict boolean filter (`isStressFree()`) might inadvertently filter out perfectly fine machines or leave us with zero genuinely "high-stress" machines.

## 2. Low-Interest Saturation by Country

| Country Code | Total Valid Lockers | Never Recommended (Truly High-Stress) | Recommended At Least Once (Low-Interest) |
| :--- | :--- | :--- | :--- |
"""

    # 2. Calculate metrics and build the table
    for country, lockers_set in sorted(all_lockers.items(), key=lambda x: len(x[1]), reverse=True):
        total = len(lockers_set)
        
        valid_recommended = recommended_lockers[country].intersection(lockers_set)
        
        total_recommended = len(valid_recommended)
        total_never = total - total_recommended
        
        pct_recommended = (total_recommended / total * 100) if total > 0 else 0
        pct_never = (total_never / total * 100) if total > 0 else 0

        md_content += f"| **{country}** | {total:,} | {pct_never:.2f}% ({total_never:,}) | {pct_recommended:.2f}% ({total_recommended:,}) |\n"

    md_content += """
## 3. Business Conclusion

How to evaluate this data:**
- **High Saturation Scenario:** If a vast majority of lockers fall into the "Recommended" bucket, the `low_interest` flag is highly relative. Using it as a strict filter might hide too many viable options from the user. You might need to evaluate a hybrid approach (e.g., combining it with distance or user feedback).
- **Balanced Distribution Scenario:** If there is a distinct divide, the flag holds strong absolute value. You can rely on it to confidently filter out genuinely congested machines.
"""

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(md_content)

    print(f"Success! Report generated and saved as: {output_file}")

if __name__ == "__main__":
    generate_low_interest_report()