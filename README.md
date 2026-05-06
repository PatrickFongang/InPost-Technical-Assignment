# SmartPicker - InPost Technical Assignment

## Author

- **Name:** Patrick Fongang
- **Email:** patrickfongang.contact@gmail.com

## Overview

SmartPicker is an intelligent, full-stack web application designed to help users effortlessly find the optimal InPost lockers in their vicinity. By combining geospatial proximity calculations with real-time weather data (Open-Meteo) and custom reliability scoring algorithms, it ensures parcels are directed to the safest locations while proactively advising users on thermal protection during extreme weather conditions. 

## Demo & Description

The SmartPicker application is designed to completely abstract the complexity of logistics algorithms away from the end-user, focusing instead on a lightning-fast, highly intuitive user experience. 

### 💡 Product & UX Decisions (The "Why")
Before writing the code, I conducted informal user research via community polling to validate my assumptions and uncover real-world pain points. This research directly shaped the application's logic:

* **The "Easy Access" Illusion:** Many users complained about parcels ending up in unreachable top compartments despite explicitly selecting the "Easy Access Zone" in the official app. Feedback from a former courier revealed the physical constraint behind this: lower compartments typically house large "C-size" boxes. When these fill up quickly (often due to heavy orders or misclassified parcels) the system is forced to allocate any remaining "Easy Access" requests to the only available slots, which are usually at the top. Knowing that a simple UI toggle is a false promise during locker congestion, I removed the manual filter. Instead, the backend calculates a realistic **"Traffic Light" Reliability Score** (High, Medium, Low). The algorithm specifically rewards **POPs (Parcel Service Points)**—where human staff guarantees an effortless pickup—and machines officially flagged as **low interest**, as their lack of congestion significantly reduces the risk of bottom compartments being fully occupied.
* **Protecting Goods & Hardware (Thermo Mode):** The primary purpose of temperature-controlled lockers is protecting sensitive parcels (like food, cosmetics, or electronics) from spoiling in extreme summer heat or winter cold. However, community feedback revealed a surprising secondary pain point: outdoor locker doors physically freezing shut during severe frosts. The application proactively addresses both issues. If the backend (via the Open-Meteo API) detects extreme local temperatures, a warning banner alerts the user to enable "Thermo Mode" (filtering for indoor/climate-controlled lockers). This ensures parcel safety, solves a real-world hardware frustration, and acts as a natural upsell.

### ⚙️ How it works (The Architecture)
1. **Seamless Geocoding:** The user types a natural text address (e.g., "Warszawa, Złota"). The React frontend uses the Nominatim (OpenStreetMap) API to translate this into exact latitude/longitude coordinates on the fly.
2. **The POST Request:** The frontend sends a clean JSON payload (coordinates, radius, thermo-preference) to the Spring Boot backend.
3. **In-Memory Storage (No Database):** To achieve sub-millisecond data retrieval, the application completely bypasses a traditional database. Instead, all locker data is held directly in RAM within the [`LocalLockerCache`](./backend/smartpicker/src/main/java/com/inpost/smartpicker/service/LocalLockerCache.java). This eliminates database query latency entirely.
4. **Fast-Boot & Resilience via Snapshots:** Populating this in-memory cache with tens of thousands of lockers from the external InPost API takes time. To guarantee immediate application availability on server startup, the [`InPostDataRefresher`](./backend/smartpicker/src/main/java/com/inpost/smartpicker/cron/InPostDataRefresher.java) instantly restores the RAM cache from a local disk backup (`lockers_snapshot.json`). A full API synchronization is then triggered asynchronously in the background.
5. **Spatial Binning (Geospatial Grid):** Running distance calculations against 90,000+ lockers for every request is an expensive O(N) operation. To optimize this without a database, the [`LocalLockerCache`](./backend/smartpicker/src/main/java/com/inpost/smartpicker/service/LocalLockerCache.java) groups lockers into a custom coordinate-based grid (`Map<String, List<Locker>>`). When a request comes in, the backend performs O(1) map lookups for the neighboring grid keys, drastically narrowing the search space from the entire country to just the local sector before applying the Haversine distance formula.
6. **Data Cleaning & Filtering:** Finally, Java predicates (informed by my Python data analysis) sanitize dirty data anomalies and apply the thermal logic. The remaining lockers are scored, sorted by exact distance, and returned to the user.

### 📸 Visuals
https://github.com/user-attachments/assets/987a20e4-aaf1-483e-bcbf-4e473dc05ff0

## Technologies

**Backend:**
- **Java 25 & Spring Boot 3**
- **API Design & Standards:** Integrated **OpenAPI (Swagger)** for interactive API documentation and maintained extensive **JavaDoc** for code-level clarity. Implemented strict **Jakarta Validation** on Request DTOs to sanitize inputs, paired with a `@ControllerAdvice` [**GlobalExceptionHandler**](./backend/smartpicker/src/main/java/com/inpost/smartpicker/exception/GlobalExceptionHandler.java) to guarantee consistent, standardized HTTP error responses.
- **JUnit 5, Mockito & AssertJ:** Used for robust, readable unit testing of isolated business logic
- **Logback:** Configured for structured, daily rolling file logging and color-coded console outputs, ensuring production-ready observability.
- **In-Memory Caching:** Implemented a [`LocalLockerCache`](./backend/smartpicker/src/main/java/com/inpost/smartpicker/service/LocalLockerCache.java) combined with a Scheduled Cron job ([`InPostDataRefresher`](./backend/smartpicker/src/main/java/com/inpost/smartpicker/cron/InPostDataRefresher.java)) to fetch, clean, and store locker data in memory, bypassing the need for a database and ensuring lightning-fast geospatial queries.

**Frontend:**
- **React.js (Vite):** Selected for building a lightning-fast, reactive user interface with an excellent developer experience.
- **Tailwind CSS:** Allowed for rapid, highly customizable styling without the overhead of external CSS files, ensuring a responsive and modern UX.
- **Leaflet & React-Leaflet:** Integrated for smooth, interactive map visualizations and dynamic `flyTo` camera animations.
- **Lucide React:** Used for clean, consistent, and scalable iconography.

**Data Analysis & Scripts:**
- **Python:** Used extensively in the research phase (located in the [`scripts/`](./scripts) directory) to analyze raw InPost API data.

**External APIs:**
- **Open-Meteo API:** Used for fetching highly accurate, free, and coordinate-based weather forecasts to trigger Thermo Mode alerts.
- **Nominatim (OpenStreetMap):** Implemented on the frontend to seamlessly geocode user-friendly text addresses into strict latitude/longitude coordinates required by the backend.

## How to run

### Prerequisites

To build and run this solution locally, you will need:
- **Java 25**
- **Node.js** (v24 or newer) and **npm**
- *(Optional)* Maven (the project includes a Maven Wrapper `mvnw`, so a local Maven installation is not strictly required)

### Build & run

The project uses a monorepo structure. You will need to start the Backend and Frontend in separate terminal instances.

**1. Start the Backend (Spring Boot)**
Open your first terminal window and run:
```bash
# Clone the repository
git clone https://github.com/PatrickFongang/InPost-Technical-Assignment
cd InPost-Technical-Assignment/backend/smartpicker

# Run the Spring Boot application using the Maven Wrapper
./mvnw spring-boot:run
```
*The backend will start on `http://localhost:8080`. Note: On the first startup, the `InPostDataRefresher` will execute to populate the local cache.*

**2. Start the Frontend (React/Vite)**
Open a second terminal window and run:
```bash
# Navigate to the frontend directory
cd InPost-Technical-Assignment/frontend

# Install dependencies
npm install

# Start the development server
npm run dev
```
*The frontend will start on `http://localhost:5173`. Open this URL in your web browser to interact with the application.*

## What I would do with more time

If I had more time to expand this MVP into a fully mature product, I would prioritize the following areas:

1. **Cloud Deployment & CI/CD:** I would write `Dockerfile`s for both the backend and frontend, set up a `docker-compose.yml` configuration, and deploy the application to a cloud provider (e.g., AWS, Render, or Vercel).
2. **Anonymous Feedback Loop (Algorithm Validation):** To empirically prove that my "Traffic Light" scoring system actually improves the user experience, I would introduce a simple, anonymous post-pickup rating prompt (e.g., "Was your locker accessible? Yes/No"). This real-world telemetry would be fed back into the backend to constantly fine-tune the scoring weights.
3. **Crowdsourced Amenities & Cleanliness (The "Waze" approach):** The community polling revealed significant pain points regarding littering and the general mess left around machines (e.g., discarded packaging by other users), as well as a lack of parking. I would introduce user authentication (OAuth2) to allow users to "tag" lockers with real-time metadata (e.g., *Clean Area*, *Litter Reported*, *Easy Parking*). 

## AI usage

I utilized AI tools (Gemini) primarily as an interactive sparring partner for boilerplate generation and rapid debugging. Given the tight timeframe of the assignment, this approach allowed me to accelerate development and deliver a fully polished, end-to-end product. 

However, all core business logic, technology stack selection, and backend architecture design were strictly driven by me. Every line of code was meticulously reviewed and verified to ensure it met the highest engineering standards and the exact requirements of the project.

## Anything else?

**A Data-Driven, Product-Oriented Mindset**

During development, I didn't just blindly consume the InPost API based on its documentation. I wrote Python analyzers (located in the [`scripts/`](./scripts) directory) to proactively investigate the raw data distributions. This saved me from over-engineering features that would provide zero value. Three key examples of this data-driven approach include:

1. **Handling Dirty Raw Data ([`dirty_data_report.md`](./scripts/dirty_data_report.md)):**<br>Working with the raw API data taught me never to trust the source blindly. My Python scripts (and the visual evidence in [`dirty_data_proof.png`](./scripts/dirty_data_proof.png)) revealed anomalies in the production dataset, specifically internal "TEST" machines leaking into the public API and severe inconsistencies in country naming conventions. To prevent showing fake lockers to users, I implemented strict filtering rules within [`LockerDataCleaningPredicates.java`](./backend/smartpicker/src/main/java/com/inpost/smartpicker/predicate/LockerDataCleaningPredicates.java) to sanitize the data stream before it reaches the cache.
   
2. **The "Low Interest" Trap ([`low_interest_feasibility_report.md`](./scripts/low_interest_feasibility_report.md)):**<br>I initially considered using the `recommended_low_interest_box_machines_list` as a strict boolean filter for a "Stress-Free" mode. However, my analysis revealed that in Poland, over 60% of lockers are flagged as "low interest" by other machines, making it a highly relative metric rather than an absolute one. In other European countries, this data is 100% absent. This insight proved that a strict filter would either hide too many viable options or break entirely outside of Poland.
   
3. **The Telemetry Mirage ([`telemetry_feasibility_report.md`](./scripts/telemetry_feasibility_report.md)):**<br>I wanted to heavily enhance the scoring algorithm by using real-time box availability (capacities for sizes A, B, C). Before implementing the complex Java logic, I ran a feasibility check. The data showed that while Great Britain has excellent telemetry coverage (99.6%), Poland and the rest of Europe have exactly 0% data availability for specific box sizes. This empirical evidence prevented me from building a "dead" feature.
