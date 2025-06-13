package nl.inholland.student.controller;

import nl.inholland.student.model.Movie;
import nl.inholland.student.service.MovieService;
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

    @PostMapping("/{title}")
    public ResponseEntity<Movie> addMovie(@PathVariable String title) {
        try {
            Movie movie = movieService.addMovieToWatchlist(title);
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<Movie>> getAllMovies(Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(pageable);
        return ResponseEntity.ok(movies);
    }

    @PatchMapping("/{id}/watched")
    public ResponseEntity<Movie> updateWatchedStatus(@PathVariable Long id, @RequestBody boolean watched) {
        try {
            Movie movie = movieService.updateWatchedStatus(id, watched);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/rating")
    public ResponseEntity<Movie> updateRating(@PathVariable Long id, @RequestBody int rating) {
        try {
            Movie movie = movieService.updateRating(id, rating);
            return ResponseEntity.ok(movie);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
