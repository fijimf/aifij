# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is AI DeepFij, a Spring Boot application for college basketball data analysis and machine learning predictions. The application scrapes ESPN data, manages team and game statistics, and provides ML models for game predictions.

## Build and Development Commands

### Basic Maven Commands
- `mvn clean compile` - Clean and compile the project
- `mvn spring-boot:run` - Run the application locally
- `mvn test` - Run all tests
- `mvn clean package` - Build JAR file
- `mvn clean package -DskipTests` - Build without running tests

### Database Setup
- `./scripts/run_db.sh` - Start PostgreSQL database container for local development
- Uses PostgreSQL 13 with credentials: username=postgres, password=p@ssw0rd, port=5432

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
- Repositories in `repo/` extend JpaRepository
- Flyway migrations in `src/main/resources/db/migration/`

**Service Layer**
- `ScheduleService` - Primary service for data management and ESPN scraping
- `StatisticService` - Handles team statistics calculation and management
- `MachineLearningService` - Coordinates ML model generation and data processing
- `ScrapingService` - ESPN API integration for fetching live data

**Web Layer**
- REST controllers in `controller/` and `controller/admin/`
- Admin endpoints require authentication, most others are public
- JWT-based authentication with configurable security profiles

**Machine Learning**
- Feature generators in `ml/generators/feature/`
- Target generators in `ml/generators/target/` 
- Model data generation coordinated by `MachineLearningService`

### Key Data Flow
1. Data is scraped from ESPN APIs (conferences, teams, games, standings)
2. Raw data is converted to JPA entities and persisted
3. Statistics are calculated from game results
4. ML features/targets are generated from historical data
5. REST APIs expose data for consumption

### Configuration Profiles
- `local` profile - Disables security for local development
- `prod` profile - Full security enabled
- Database configuration in `application.properties` and profile-specific files

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
- Audit trail maintained in `audit` table for admin operations

### Testing
- Unit tests for repositories using Testcontainers
- Integration tests with sample JSON data in test resources
- Test configuration uses separate `application.properties`