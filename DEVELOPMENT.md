# Development Guide

This guide provides practical information for developers working on the AI DeepFij project.

## Quick Start

1. **Prerequisites**
   - Java 21
   - Docker (for database)
   - Maven 3.6+

2. **Database Setup**
   ```bash
   ./scripts/run_db.sh
   ```

3. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access Application**
   - API Base URL: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - Actuator Health: http://localhost:8080/api/actuator/health

## Database Information

- **Host**: localhost:5432
- **Database**: deepfij
- **Username**: deepfij
- **Password**: mutombo
- **Migrations**: Flyway automatically runs on startup

## Key API Endpoints

### Public Endpoints
- `GET /api/schedule/*` - Schedule and game data
- `GET /api/stats/*` - Team statistics
- `GET /api/quotes/*` - Inspirational quotes

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Token refresh

### Machine Learning
- `GET /api/models` - List available models
- `POST /api/models/{id}/train` - Train a model
- `POST /api/models/{id}/train/{runId}/predict` - Make predictions

### Admin Endpoints (Authentication Required)
- `GET /api/admin/*` - Administrative operations
- `POST /api/admin/schedule/scrape` - Trigger data scraping

## Testing

### Run All Tests
```bash
mvn test
```

### View Coverage Report
```bash
mvn test
open target/site/jacoco/index.html
```

### Test Database
Tests use Testcontainers to spin up isolated PostgreSQL instances.

## Development Tips

### Database Schema Changes
1. Create new migration file in `src/main/resources/db/migration/`
2. Follow naming convention: `V{version}__description.sql`
3. Never modify existing migrations

### Adding New Features
1. Create entity classes with proper JPA annotations
2. Create repository interface extending `JpaRepository`
3. Create service interface and implementation
4. Create DTO records for data transfer
5. Create controller with proper error handling
6. Add tests for all layers

### Code Quality
- Follow the patterns in `.cursorrules`
- Use `@Transactional` for multi-step database operations
- Return `ResponseEntity<ApiResponse<T>>` from controllers
- Use `@EntityGraph` to avoid N+1 queries
- Implement DTOs as records with validation

### Authentication
- JWT tokens are used for authentication
- Admin endpoints require authentication
- Use `@PreAuthorize` for method-level security

## Troubleshooting

### Database Connection Issues
1. Ensure Docker is running
2. Check if PostgreSQL container is running: `docker ps`
3. Restart database: `./scripts/run_db.sh`

### Test Failures
1. Ensure no other PostgreSQL instances are running on port 5432
2. Check Testcontainers has access to Docker
3. Verify test resources in `src/test/resources/json/`

### Build Issues
1. Ensure Java 21 is installed: `java -version`
2. Clean build: `mvn clean compile`
3. Check Maven settings: `mvn -version`

## Configuration Profiles

### Local Development
```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

### Production
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

## Machine Learning Development

### Model Development Process
1. Create feature generators in `ml/generators/feature/`
2. Create label generators in `ml/generators/label/`
3. Implement model in `ml/generators/models/`
4. Register model in database via migration
5. Test training via API endpoints

### Training Models
Use the `/api/models/{id}/train` endpoint with query parameters to train models.

### Making Predictions
After successful training, use `/api/models/{id}/train/{runId}/predict` for predictions.