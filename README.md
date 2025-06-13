# Movie Watchlist Backend

A Spring Boot REST API application for managing a personal movie watchlist. This application fetches movie data from external APIs (OMDb and TMDB), downloads movie images, and provides a complete CRUD interface for managing movies.

## Features

- **Multi-API Integration**: Fetches movie data from both OMDb and TMDB APIs
- **Parallel Processing**: Uses CompletableFuture for async API calls to improve performance
- **Image Download**: Automatically downloads and stores movie backdrop images locally
- **REST API**: Complete CRUD operations through RESTful endpoints
- **Database Storage**: H2 in-memory database for development
- **Comprehensive Testing**: Unit tests for controllers and services
- **Pagination Support**: Paginated movie listing with sorting

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.0**
- **Spring Data JPA**
- **H2 Database**
- **Lombok**
- **JUnit 5 & Mockito**
- **Maven**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- API Keys for:
  - OMDb API: [Get your key here](http://www.omdbapi.com/apikey.aspx)
  - TMDB API: [Get your key here](https://www.themoviedb.org/settings/api)

## Configuration

1. Clone the repository
2. Update `src/main/resources/application.properties` with your API keys:

```properties
# OMDb API Configuration
omdb.api.baseUrl=http://www.omdbapi.com/
omdb.api.key=YOUR_OMDB_API_KEY_HERE

# TMDB API Configuration
tmdb.api.baseUrl=https://api.themoviedb.org/3
tmdb.api.key=YOUR_TMDB_API_KEY_HERE

# H2 Database Configuration
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:watchlistdb
```

## Build and Run

### Using Maven

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### H2 Database Console

Access the H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:watchlistdb`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Base URL: `/api/watchlist`

#### 1. Add Movie to Watchlist
- **POST** `/api/watchlist/{title}`
- **Description**: Adds a movie to the watchlist by title
- **Example**: `POST /api/watchlist/Inception`
- **Response**: Created Movie object with downloaded images

```json
{
  "id": 1,
  "title": "Inception",
  "year": "2010",
  "director": "Christopher Nolan",
  "genre": "Action, Sci-Fi",
  "imagePaths": ["images/Inception_uuid.jpg", "images/Inception_uuid2.jpg"],
  "watched": false,
  "rating": 0
}
```

#### 2. Get All Movies (Paginated)
- **GET** `/api/watchlist`
- **Parameters**: 
  - `page` (optional): Page number (default: 0)
  - `size` (optional): Page size (default: 20)
  - `sort` (optional): Sort by field (e.g., `title`, `year`)
- **Example**: `GET /api/watchlist?page=0&size=10&sort=title`
- **Response**: Paginated list of movies

```json
{
  "content": [
    {
      "id": 1,
      "title": "Inception",
      "year": "2010",
      "director": "Christopher Nolan",
      "genre": "Action, Sci-Fi",
      "imagePaths": ["images/Inception_uuid.jpg"],
      "watched": false,
      "rating": 0
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

#### 3. Update Watched Status
- **PATCH** `/api/watchlist/{id}/watched`
- **Description**: Updates the watched status of a movie
- **Example**: `PATCH /api/watchlist/1/watched`
- **Request Body**: `true` or `false`
- **Response**: Updated Movie object

#### 4. Update Rating
- **PATCH** `/api/watchlist/{id}/rating`
- **Description**: Updates the rating of a movie (0-5 scale)
- **Example**: `PATCH /api/watchlist/1/rating`
- **Request Body**: Integer between 0-5
- **Response**: Updated Movie object

#### 5. Delete Movie
- **DELETE** `/api/watchlist/{id}`
- **Description**: Removes a movie from the watchlist
- **Example**: `DELETE /api/watchlist/1`
- **Response**: 204 No Content

## Project Structure

```
src/
├── main/java/nl/inholland/student/
│   ├── MovieWatchlistApplication.java          # Main Spring Boot application
│   ├── client/
│   │   ├── OmdbClient.java                     # OMDb API client
│   │   └── TmdbClient.java                     # TMDB API client
│   ├── config/
│   │   └── AppConfig.java                      # Application configuration
│   ├── controller/
│   │   └── MovieController.java                # REST API endpoints
│   ├── dto/
│   │   ├── omdb/
│   │   │   └── OmdbMovieDto.java               # OMDb response mapping
│   │   └── tmdb/
│   │       ├── TmdbMovieDto.java               # TMDB movie response
│   │       ├── TmdbSearchListDto.java          # TMDB search results
│   │       ├── TmdbImageDto.java               # TMDB image data
│   │       └── TmdbImageListDto.java           # TMDB image list
│   ├── model/
│   │   └── Movie.java                          # JPA entity
│   ├── repository/
│   │   └── MovieRepository.java                # Spring Data JPA repository
│   └── service/
│       └── MovieService.java                   # Business logic with async processing
├── test/java/nl/inholland/student/
│   ├── controller/
│   │   └── MovieControllerTest.java            # Controller unit tests
│   └── service/
│       └── MovieServiceTest.java               # Service unit tests
└── main/resources/
    └── application.properties                  # Application configuration
```

## Key Features Implementation

### Parallel API Calls
The application uses `@Async` and `CompletableFuture` to make parallel calls to OMDb and TMDB APIs, significantly reducing response time.

### Image Management
- Downloads up to 3 backdrop images from TMDB
- Stores images locally in `images/` directory
- Generates unique filenames to avoid conflicts
- Graceful error handling for failed downloads

### Error Handling
- Comprehensive exception handling in controllers
- Validation for rating values (0-5)
- Proper HTTP status codes returned

### Testing
- Unit tests for all controller endpoints using MockMvc
- Service layer tests with mocked dependencies
- Tests cover success cases, error scenarios, and edge cases

## Development Notes

- Lombok is used to reduce boilerplate code with `@Data` annotations
- Jackson handles JSON serialization/deserialization
- H2 database provides easy development setup
- Async processing is enabled with `@EnableAsync`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is developed as part of an Object-Oriented Programming 3 course assignment.
