// Contains core business logic
package com.watchlist.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.watchlist.client.OmdbClient;
import com.watchlist.client.TmdbClient;
import com.watchlist.dto.omdb.OmdbMovieDto;
import com.watchlist.dto.tmdb.TmdbMovieDto;
import com.watchlist.exception.InvalidArgumentException;
import com.watchlist.exception.ResourceNotFoundException;
import com.watchlist.model.Movie;
import com.watchlist.repository.MovieRepository;

import java.util.Optional;

@Service
public class MovieService {

    private final OmdbClient omdbClient;
    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;

    // Constructor to get objects
    @Autowired
    public MovieService(OmdbClient omdbClient, TmdbClient tmdbClient, MovieRepository movieRepository) {
        this.omdbClient = omdbClient;
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
    }

    // Fetches data, combines it and saves in movie watcchlist 
    public Movie addMovieToWatchlist(String title) {
        // fetch data from OMDB
        OmdbMovieDto omdbMovie = null;
        try {
            omdbMovie = omdbClient.fetchMovieByTitle(title);
        } catch (Exception e) {
            // Log the error, continue
            System.err.println("Failed to fetch from OMDB: " + e.getMessage());
        }
        
        // search for movie in TMDB 
        Optional<TmdbMovieDto> tmdbMovie = Optional.empty();
        try {
            tmdbMovie = tmdbClient.searchMovie(title);
        } catch (Exception e) {
            // Log the error, continue
            System.err.println("Failed to fetch from TMDB API: " + e.getMessage());
        }
        
        // Creates a movie entity
        Movie movie = new Movie();
        
        if (omdbMovie != null) {
            movie.setTitle(omdbMovie.getTitle());
            movie.setYear(omdbMovie.getYear());
            movie.setDirector(omdbMovie.getDirector());
            movie.setGenre(omdbMovie.getGenre());
        } else {
            movie.setTitle(title);
        }
        
        // If movie found -> fetch images from TMDB
        if (tmdbMovie.isPresent()) {
            try {
                var imageList = tmdbClient.fetchMovieImages(tmdbMovie.get().getId());
                if (imageList != null && imageList.getBackdrops() != null) {
                    var imagePaths = imageList.getBackdrops().stream()
                            .map(img -> img.getFilePath())
                            .toList();
                    movie.setImagePaths(imagePaths);
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch movie images: " + e.getMessage());
            }
        }
        
        movie.setWatched(false);
        movie.setRating(0);
        
        return movieRepository.save(movie);
    }

    
    // RESTful endpoints

    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    public Movie updateWatchedStatus(Long id, boolean watched) {
        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            throw new RuntimeException("Movie not found");
        }
        
        Movie movie = movieOpt.get();
        movie.setWatched(watched);
        return movieRepository.save(movie);
    }

    public Movie updateRating(Long id, int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        
        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            throw new RuntimeException("Movie not found");
        }
        
        Movie movie = movieOpt.get();
        movie.setRating(rating);
        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie not found");
        }
        movieRepository.deleteById(id);
    }
}
