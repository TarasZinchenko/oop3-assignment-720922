// public api for the application
package com.watchlist.controller;

import com.watchlist.model.Movie;
import com.watchlist.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }


    // RESTful API endpoints

    @PostMapping("/{title}")
    public ResponseEntity<Movie> addMovie(@PathVariable String title) {
        Movie movie = movieService.addMovieToWatchlist(title);
        return ResponseEntity.ok(movie);
    }

    @GetMapping
    public ResponseEntity<Page<Movie>> getAllMovies(Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(pageable);
        return ResponseEntity.ok(movies);
    }

    @PatchMapping("/{id}/watched")
    public ResponseEntity<Movie> updateWatchedStatus(@PathVariable Long id, @RequestBody boolean watched) {
        Movie movie = movieService.updateWatchedStatus(id, watched);
        return ResponseEntity.ok(movie);
    }

    @PatchMapping("/{id}/rating")
    public ResponseEntity<Movie> updateRating(@PathVariable Long id, @RequestBody int rating) {
        Movie movie = movieService.updateRating(id, rating);
        return ResponseEntity.ok(movie);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
