# USDS Regulations Analysis Backend

A Spring Boot application for downloading and analyzing Federal Regulations from the eCFR API.

## Features

- Downloads CFR Title 7 (Agriculture) regulations from eCFR API
- Stores regulation data in PostgreSQL database
- Calculates word counts for each regulation
- Provides REST endpoints for testing and data retrieval

## Prerequisites

- Java 17 or later
- Maven 3.6 or later
- PostgreSQL 12 or later

## Database Setup

1. Install PostgreSQL and create a database:
```sql
CREATE DATABASE usds_regulations;
CREATE USER usds_user WITH PASSWORD 'usds_password';
GRANT ALL PRIVILEGES ON DATABASE usds_regulations TO usds_user;
```

2. The application will automatically create the necessary tables on startup.

## Running the Application

1. **Clone and navigate to the backend directory:**
```bash
cd backend
```

2. **Update database configuration in `src/main/resources/application.properties` if needed**

3. **Build and run the application:**
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Health Check
```
GET /api/health
```
Returns service health status and database connectivity.

### Download Title 7 Regulations
```
GET /api/test-download
```
Triggers the download of CFR Title 7 regulations from eCFR API and stores them in the database.

Example response:
```json
{
  "success": true,
  "message": "Successfully downloaded 5 regulations from CFR Title 7. Total word count: 15432",
  "executionTimeMs": 3250,
  "statistics": {
    "totalRegulationsInDatabase": 5,
    "title7RegulationsCount": 5,
    "title7TotalWordCount": 15432
  }
}
```

### Get Statistics
```
GET /api/stats
```
Returns statistics about stored regulations.

### List Title 7 Regulations
```
GET /api/regulations/title7
```
Returns a list of all stored Title 7 regulations with basic information.

## Testing the Implementation

1. **Start the application**
2. **Test health endpoint:**
   ```bash
   curl http://localhost:8080/api/health
   ```

3. **Trigger regulation download:**
   ```bash
   curl http://localhost:8080/api/test-download
   ```

4. **Check statistics:**
   ```bash
   curl http://localhost:8080/api/stats
   ```

## Configuration

Key configuration options in `application.properties`:

- `spring.datasource.url`: PostgreSQL database URL
- `spring.datasource.username/password`: Database credentials
- `server.port`: Application port (default: 8080)
- `logging.level.com.usds.regulations`: Logging level

## Implementation Details

### eCFR API Integration

The service uses the official eCFR API endpoints:
- Structure API: `/api/versioner/v1/structure/{date}/title-7.json`
- Search API: `/api/search/v1/results`

### Data Model

The `Regulation` entity stores:
- CFR Title and Part Number
- Regulation Title and Content
- Agency Name
- Word Count (calculated automatically)
- Creation and Update timestamps

### Error Handling

- Graceful handling of API failures
- Fallback to basic regulation entries when content is unavailable
- Comprehensive logging for debugging

## Development Notes

- The current implementation limits processing to 5 parts for testing
- Remove the `maxParts` limit in `EcfrApiService.downloadTitle7Regulations()` for full processing
- The service includes rate limiting (200ms delay between API calls) to be respectful to the eCFR API

## Next Steps

1. Remove testing limitations for full Title 7 processing
2. Add support for other CFR titles
3. Implement more sophisticated content parsing
4. Add data validation and cleanup
5. Create scheduled jobs for regular updates