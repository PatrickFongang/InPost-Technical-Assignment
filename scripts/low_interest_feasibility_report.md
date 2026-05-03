# 📊 InPost Smart Picker: 'Low Interest' Feasibility Report

> **Source:** `lockers_snapshot.json` (Cleaned Geospatial Grid Cache)
> **Objective:** Test the hypothesis that lockers simply recommend each other in a loop, rendering the `recommended_low_interest_box_machines_list` useless as an absolute filter.

## 1. Executive Summary

This report analyzes how many unique parcel lockers are actually marked as "low interest" alternatives by other machines. If the vast majority of lockers appear on someone else's "low interest" list, the metric is relative rather than absolute. Using it as a strict boolean filter (`isStressFree()`) might inadvertently filter out perfectly fine machines or leave us with zero genuinely "high-stress" machines.

## 2. Low-Interest Saturation by Country

| Country Code | Total Valid Lockers | Never Recommended (Truly High-Stress) | Recommended At Least Once (Low-Interest) |
| :--- | :--- | :--- | :--- |
| **PL** | 33,289 | 39.15% (13,033) | 60.85% (20,256) |
| **FR** | 26,624 | 100.00% (26,624) | 0.00% (0) |
| **GB** | 26,113 | 100.00% (26,113) | 0.00% (0) |
| **DE** | 17,407 | 100.00% (17,407) | 0.00% (0) |
| **ES** | 14,611 | 100.00% (14,611) | 0.00% (0) |
| **IT** | 11,140 | 100.00% (11,140) | 0.00% (0) |
| **AT** | 4,780 | 100.00% (4,780) | 0.00% (0) |
| **SE** | 4,454 | 100.00% (4,454) | 0.00% (0) |
| **PT** | 3,173 | 100.00% (3,173) | 0.00% (0) |
| **HU** | 2,718 | 100.00% (2,718) | 0.00% (0) |
| **FI** | 2,375 | 100.00% (2,375) | 0.00% (0) |
| **DK** | 2,375 | 100.00% (2,375) | 0.00% (0) |
| **BE** | 1,806 | 100.00% (1,806) | 0.00% (0) |
| **NL** | 1,426 | 100.00% (1,426) | 0.00% (0) |
| **LU** | 72 | 100.00% (72) | 0.00% (0) |

## 3. Business Conclusion

How to evaluate this data:**
- **High Saturation Scenario:** If a vast majority of lockers fall into the "Recommended" bucket, the `low_interest` flag is highly relative. Using it as a strict filter might hide too many viable options from the user. You might need to evaluate a hybrid approach (e.g., combining it with distance or user feedback).
- **Balanced Distribution Scenario:** If there is a distinct divide, the flag holds strong absolute value. You can rely on it to confidently filter out genuinely congested machines.
