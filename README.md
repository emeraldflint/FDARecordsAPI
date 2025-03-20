# FDA Drug Applications API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)

A RESTful API system built with Java and Spring Boot that exposes data published by the FDA (Food and Drug Administration). This application allows users to search for drug record applications submitted to FDA for approval, store specific drug application details, and retrieve stored applications.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
    - [Building the Application](#building-the-application)
    - [Running Tests](#running-tests)
    - [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
    - [Using Swagger UI](#using-swagger-ui)
    - [API Endpoints](#api-endpoints)
- [Database Access](#database-access)
- [Project Structure](#project-structure)
- [Design Decisions](#design-decisions)
- [Future Improvements](#future-improvements)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Search FDA Drug Application Records**: Search by manufacturer name and optional brand name with pagination
- **Store Drug Application Records**: Save specific drug application details (application number, manufacturer name, substance name, product numbers)
- **Retrieve Applications**: Get stored applications with pagination or by application number
- **Robust Error Handling**: Centralized exception handling with appropriate HTTP status codes
- **API Documentation**: Interactive Swagger UI for easy API exploration and testing
- **In-Memory Database**: H2 database for easy local development and testing

## Technology Stack

- **Java 21** (LTS): Latest Long-Term Support Java version
- **Spring Boot 3.4.3**: Java-based framework for building web applications
- **Spring Data JPA**: Data access layer with repository pattern
- **Hibernate**: ORM for database operations
- **H2 Database**: In-memory database for development and testing
- **Gradle**: Build tool and dependency management
- **SpringDoc OpenAPI**: API documentation with Swagger UI
- **JUnit 5 & Mockito**: Testing frameworks
- **Lombok**: Reduces boilerplate code
- **Spring Validation**: Request validation

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 21** or higher
- **Gradle 8.x** (or use the included Gradle wrapper)
- A modern web browser for accessing Swagger UI
- Git (optional, for cloning the repository)

## Getting Started

### Building the Application

1. Clone the repository (or download the source code):

```bash
git clone https://github.com/emeraldflint/FDARecordsAPI.git
cd FDARecordsAPI
```

2. Build the application using Gradle:

```bash
# Using Gradle wrapper (recommended)
./gradlew clean build

# Or using your installed Gradle
gradle clean build
```

This will compile the code, run tests, and package the application into a JAR file located in the `build/libs/` directory.

### Running Tests

You can run tests using the following commands:

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.fdaapi.service.DrugApplicationServiceTest"
```

### Running the Application

You can run the application using the following command:

```bash
# Using Gradle
./gradlew bootRun

# Or directly using the JAR file
java -jar build/libs/fda-api-1.0.0.jar
```

The application will start on `http://localhost:8080/v1/drug-application-records`.

## API Documentation

### Using Swagger UI

This application includes Swagger UI for interactive API documentation and testing. To access Swagger UI:

1. Start the application as described above
2. Open your web browser and navigate to: `http://localhost:8080/swagger-ui/index.html`

On the Swagger UI page, you can:

- See all available endpoints with detailed descriptions
- Expand each endpoint to view request parameters, response models, and example values
- Try out endpoints directly in the browser by:
    1. Clicking on an endpoint to expand it
    2. Clicking the "Try it out" button
    3. Filling in the required parameters
    4. Clicking "Execute" to make a real API call
    5. Viewing the response, including status code, headers, and response body

You can also download the OpenAPI specification in JSON format at: `http://localhost:8080/api-docs`

### API Endpoints

#### Search FDA Drug Applications

```
GET /v1/drug-application-records/search
```

Parameters:
- `manufacturerName` (required): FDA manufacturer name to search for
- `brandName` (optional): FDA brand name to filter by
- `skip` (optional, default: 0): Number of results to skip
- `limit` (optional, default: 10): Maximum number of results to return

Example:
```
GET /v1/drug-application-records/search?manufacturerName=TARO&brandName=LORATADINE&skip=0&limit=10
```

#### Store Specific Drug Application Details

```
POST /v1/drug-application-records
```

Request Body:
```json
{
  "applicationNumber": "ANDA076805",
  "manufacturerName": "TARO",
  "substanceName": "LORATADINE",
  "productNumbers": ["001", "002"]
}
```

#### Get All Stored Applications

```
GET /v1/drug-application-records
```

Parameters:
- `page` (optional, default: 0): Page number (zero-based)
- `size` (optional, default: 10): Page size

Example:
```
GET /v1/drug-application-records?page=0&size=20
```

#### Get Application by ID

```
GET /v1/drug-application-records/{applicationNumber}
```

Example:
```
GET /v1/drug-application-records/ANDA076805
```

## Database Access

The application uses an H2 in-memory database. You can access the H2 console while the application is running:

1. Navigate to `http://localhost:8080/api/h2-console`
2. Use the following connection details:
    - JDBC URL: `jdbc:h2:mem:fdarecords` (as configured in application.yml)
    - Username: `sa`
    - Password: `password`
    - Leave other settings at their defaults

This provides a web interface to directly query the database, which is useful for debugging and development purposes.

## Design Decisions

### DTOs as Java Records

Java Records were used for Data Transfer Objects (DTOs) to create immutable data carriers. This reduces boilerplate code, improves readability, and provides better performance for objects that are primarily used to carry data.

### Exception Handling

A global exception handler is implemented to provide consistent error responses across the API:

- HTTP 400: For validation errors and bad requests
- HTTP 404: For resources not found
- HTTP 503: For FDA API connectivity issues
- HTTP 500: For unexpected server errors

Each error response includes a timestamp, status code, error message, and additional details to help diagnose the issue.

### Repository Pattern

The application uses Spring Data JPA repositories to provide a clean abstraction over the database. This makes it easy to switch from H2 to another database system in the future without changing business logic.

### Service Layer Separation

Clear separation between services that interact with the FDA API and services that handle application data. This separation of concerns makes the code more maintainable and testable.

### Validation

Bean Validation with annotation-based constraints ensures that all incoming data is properly validated before processing. This helps prevent data inconsistency and improves security.

## Future Improvements

Potential future improvements to the application:

- **Authentication and Authorization**: Add user authentication and role-based access control
- **Caching**: Implement caching for FDA API responses to improve performance and reduce API calls
- **Rate Limiting**: Add rate limiting to prevent abuse of the FDA API
- **Advanced Search**: Enhance search capabilities with additional filters and search options
- **Data Export**: Add functionality to export drug application data in various formats (CSV, PDF, etc.)
- **Metrics and Monitoring**: Integrate with monitoring tools to track API usage and performance
- **Docker Containerization**: Provide Docker configuration for easy deployment
- **CI/CD Pipeline**: Set up continuous integration and continuous deployment
