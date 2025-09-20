# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is AI DeepFij, a Spring Boot application for college basketball data analysis and machine learning predictions. The application scrapes ESPN data, manages team and game statistics, provides ML models for game predictions, and includes user authentication and administrative features.

## Technology Stack

- **Framework**: Spring Boot 3.3.3 with Java 21
- **Database**: PostgreSQL with Flyway migrations
- **Security**: Spring Security with JWT authentication
- **Testing**: JUnit 5, Testcontainers, AssertJ
- **Code Coverage**: JaCoCo
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Caching**: Caffeine cache
- **Math Library**: Apache Commons Math3
- **Build Tool**: Maven

## Build and Development Commands

### Basic Maven Commands
- `mvn clean compile` - Clean and compile the project
- `mvn spring-boot:run` - Run the application locally
- `mvn test` - Run all tests
- `mvn clean package` - Build JAR file
- `mvn clean package -DskipTests` - Build without running tests

### Code Quality and Coverage
- `mvn test` - Run tests with JaCoCo coverage report
- View coverage report: `target/site/jacoco/index.html` (generated after running tests)

### Database Setup
- `./scripts/run_db.sh` - Start PostgreSQL database container for local development
- Uses PostgreSQL 13 with credentials: username=deepfij, password=mutombo, database=deepfij, port=5432

### Running Tests
- `mvn test` - Run all unit and integration tests
- Tests use Testcontainers for PostgreSQL integration testing
- Test resources located in `src/test/resources/json/` contain sample ESPN API responses

### Docker
- `docker build -t aifij .` - Build Docker image
- Application runs on port 8080 with context path `/api`

## Architecture Overview

### Core Components

**Data Layer**
- JPA entities in `model/schedule/` (Team, Game, Conference, Season)
- Statistics entities in `model/statistics/` (StatisticType, TeamStatistic)
- Machine Learning entities in `model/ml/` (Model, ModelRun, ModelRunParams, ModelRunMetrics)
- User management entities (User, Role) with JWT authentication
- Additional features: InspirationalQuote, ConferenceMapping
- Repositories in `repo/` extend JpaRepository
- Flyway migrations in `src/main/resources/db/migration/`

**Service Layer**
- `ScheduleService` - Primary service for data management and ESPN scraping
- `StatisticService` - Handles team statistics calculation and management
- `MachineLearningService` - Coordinates ML model generation, training, and predictions
- `ScrapingService` - ESPN API integration for fetching live data
- `UserService` - User management and authentication
- `TokenBlacklistService` - JWT token management
- `PasswordResetService` - Password reset functionality
- `InspirationalQuoteService` - Motivational quotes feature
- `StartupService` - Application initialization
- `StatisticalService` & `StatisticalModel` - Statistical analysis components

**Web Layer**
- Public controllers: `ScheduleController`, `StatsController`, `InspirationalQuoteController`
- ML controllers: `MachineLearningController`, `ModelController` for model training and predictions
- Authentication: `AuthController` for login, registration, password management
- Admin controllers in `controller/admin/`: `ScheduleAdminController`, `TeamAdminController`, `ConferenceAdminController`, `SeasonAdminController`, `StatisticsAdminController`
- JWT-based authentication with configurable security profiles
- OpenAPI/Swagger documentation available

**Machine Learning**
- Feature generators in `ml/generators/feature/` (e.g., `TeamNamesGenerator`)
- Label generators in `ml/generators/label/` (e.g., `HomeTeamWonGenerator`, `MarginGenerator`, `ScoresGenerator`)
- Model implementations in `ml/generators/models/` (e.g., `MarginNaiveLinearRegression`)
- ML pipeline coordination by `MachineLearningService`
- Model persistence and training tracking with database storage
- Prediction API endpoints for trained models

### Key Data Flow
1. Data is scraped from ESPN APIs (conferences, teams, games, standings)
2. Raw data is converted to JPA entities and persisted
3. Statistics are calculated from game results
4. ML features/labels are generated from historical data
5. Models can be trained and used for predictions
6. REST APIs expose data and ML predictions for consumption

### Configuration Profiles
- `local` profile - Disables security for local development
- `prod` profile - Full security enabled
- Database configuration in `application.properties` and profile-specific files

### Application Configuration
- Default seasons loaded: 2025, 2024 (configurable via `deepfij.seasons_to_load`)
- Server context path: `/api`
- Default port: 8080
- JWT tokens configurable via environment variables
- Actuator endpoints: health, info, env

## Development Guidelines

### Code Standards (from .cursorrules)
- Follow SOLID, DRY, KISS, and YAGNI principles
- Use Java 21 and Spring Boot 3 features
- All entities must use `@Entity`, `@Id`, `@GeneratedValue(strategy=GenerationType.IDENTITY)`
- Repository interfaces extend `JpaRepository` and use `@Repository`
- Service implementations use `@Service` with interface contracts
- Controllers use `@RestController` with `@RequestMapping` class-level paths
- DTOs must be records with validation in compact constructors
- Use `FetchType.LAZY` for entity relationships
- Use `@EntityGraph` for relationship queries to avoid N+1 problems
- Return `ResponseEntity<ApiResponse<T>>` from controller methods
- Implement proper transaction boundaries with `@Transactional`

### Database Operations
- All database schema changes via Flyway migrations
- Use JPQL for custom repository queries

### Testing
- Unit tests for repositories using Testcontainers
- Integration tests with sample JSON data in test resources
- Test configuration uses separate `application.properties`