# DeepFij Project Guidelines

## Project Overview
DeepFij is a sports statistics application focused on basketball (likely college basketball). It tracks teams, seasons, games, and various statistics. The application provides APIs for retrieving team data, tournament information, and statistical analysis.

## Tech Stack
- **Java 21**: Core programming language
- **Spring Boot 3.2.3**: Application framework
- **Spring Data JPA**: Database access and ORM
- **PostgreSQL**: Relational database
- **Flyway**: Database migration tool
- **Spring Security + JWT**: Authentication and authorization
- **Thymeleaf**: Server-side templating
- **Caffeine**: In-memory caching
- **SpringDoc OpenAPI**: API documentation
- **Maven**: Build and dependency management
- **Docker**: Containerization
- **Jenkins**: CI/CD pipeline

## Project Structure
```
src/
├── main/
│   ├── java/com/fijimf/deepfij/
│   │   ├── auth/           # Authentication components
│   │   ├── config/         # Application configuration
│   │   ├── controller/     # REST controllers (API endpoints)
│   │   ├── model/          # Domain models and DTOs
│   │   │   ├── dto/        # Data Transfer Objects
│   │   │   ├── schedule/   # Core domain entities (Team, Season, Game)
│   │   │   ├── scraping/   # Data scraping models
│   │   │   └── statistics/ # Statistical models
│   │   ├── repo/           # Data repositories
│   │   └── service/        # Business logic services
│   │       └── impl/       # Service implementations
│   └── resources/
│       └── db/migration/   # Flyway database migrations
└── test/
    ├── java/com/fijimf/deepfij/
    │   ├── controller/     # Controller tests
    │   ├── model/          # Model tests
    │   ├── repo/           # Repository tests
    │   └── service/        # Service tests
    └── resources/
        └── json/           # Test JSON data
```

## Development Workflow

### Setup
1. Clone the repository
2. Install Java 21 and Maven
3. Install Docker for running PostgreSQL locally
4. Run `mvn clean install` to build the project

### Database
- PostgreSQL database is required
- Flyway migrations are automatically applied on startup
- Database schema is defined in `src/main/resources/db/migration/`

### Running Locally
1. Start a PostgreSQL instance:
   ```
   docker run -d --name deepfij-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:latest
   ```
2. Run the application:
   ```
   mvn spring-boot:run
   ```
3. Access the application at http://localhost:8080
4. Default admin credentials are generated at startup (check logs for password)

## Testing
- JUnit 5 (Jupiter) for unit and integration tests
- Testcontainers for integration tests with real PostgreSQL database
- Run tests with: `mvn test`
- Test categories:
  - Repository tests: Database operations
  - Service tests: Business logic
  - Controller tests: API endpoints
  - JSON parsing tests: External data integration

## Deployment
- Docker container for deployment
- Multi-stage build process defined in Dockerfile
- Jenkins pipeline for CI/CD
- Production deployment uses the `prod` Spring profile

### Building Docker Image
```
docker build -t aifij:latest .
```

### Running Docker Container
```
docker run -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/deepfij aifij:latest
```

## Best Practices
1. **Code Organization**:
   - Follow the package structure
   - Keep controllers thin, put business logic in services
   - Use DTOs for API responses

2. **Database**:
   - Create Flyway migrations for all schema changes
   - Use JPA entities with proper validation
   - Follow naming conventions for tables and columns

3. **Testing**:
   - Write tests for all new features
   - Use Testcontainers for database integration tests
   - Test both happy and sad paths

4. **Security**:
   - Don't hardcode credentials
   - Use proper authentication for all sensitive endpoints
   - Validate all user inputs

5. **Performance**:
   - Use caching for frequently accessed data
   - Create appropriate database indexes
   - Optimize database queries for large datasets