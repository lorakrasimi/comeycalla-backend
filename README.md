# ComeYCalla · Backend

Spring Boot API for ComeYCalla, a recipe manager with OCR import, weekly meal planner and shopping list.

Full project description, screenshots and setup: https://github.com/lorakrasimi/comeycalla
Frontend repository: https://github.com/lorakrasimi/comeycalla-frontend

## Quick start

1. Clone the project and start the database (`docker compose up -d`, or use your own MySQL).
2. Add `src/main/resources/google-credentials.json` (Google Cloud service account key with the Vision API enabled). Never commit this file.
3. Run:
   chmod +x mvnw   # Linux / macOS
   ./mvnw spring-boot:run

Without Docker, set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` to point to your MySQL instance.