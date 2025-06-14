# Movie Whachlist App for OOP3

A Spring Boot (REST API) application that fetches data from the OMDb and TMDB APIs and manages persolan whachlist.

## Features

* Fetches both movie data from OMDb & TMDB.
* Provides CRUD REST API for management.
* Downloads and stores 3 movie images.
* Uses H2 database (in memmory) with a web console.
* Includes unit testing with JUnit 5 and Mockito.

## Setup

**Build:** Java 17 & Maven.


## Installation and utilisation

1.  **Add your API Keys** to `src/main/resources/application.properties`.
    ```properties
    omdb.api.key=OMDB_key
    tmdb.api.key=TMDB_key
    ```
    **Or use deafolt keys(mine).**

2.  **Run the application.**
    ```
    mvn spring-boot:run
    ```
    The app will be available at `http://localhost:8080`.

## Commands

Base URL: `http://localhost:8080/api/watchlist`

* **`POST /{title}`** - Adds a movie to the watchlist.
    ```
    curl -X POST http://localhost:8080/api/watchlist/Inception
    ```
* **`GET /`** - Gets a paginated list of all movies.
    ```
    curl http://localhost:8080/api/watchlist?size=5
    ```
* **`PATCH /{id}/watched`** - Updates the watched status.
    ```
    curl -X PATCH -H "Content-Type: application/json" -d "true" http://localhost:8080/api/watchlist/1/watched
    ```
* **`PATCH /{id}/rating`** - Updates the movie rating (0-5).
    ```
    curl -X PATCH -H "Content-Type: application/json" -d "5" http://localhost:8080/api/watchlist/1/rating
    ```
* **`DELETE /{id}`** - Deletes a movie.
    ```
    curl -X DELETE http://localhost:8080/api/watchlist/1
    ```

## Check the Database

* **H2 Database Console**:
    * **url**: `http://localhost:8080/h2-console`
    * **JDBC url**: `jdbc:h2:mem:watchlistdb`
    * **Username**: `sa` (no password)



*install lobok anotations support if the red underlines bother you as well <'_'>
