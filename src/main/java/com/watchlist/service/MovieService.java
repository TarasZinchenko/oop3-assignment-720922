// Contains core business logic
package com.watchlist.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.watchlist.client.OmdbClient;
import com.watchlist.client.TmdbClient;
import com.watchlist.dto.omdb.OmdbMovieDto;
import com.watchlist.dto.tmdb.TmdbMovieDto;
import com.watchlist.exception.InvalidArgumentException;
import com.watchlist.exception.ResourceNotFoundException;
import com.watchlist.model.Movie;
import com.watchlist.repository.MovieRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class MovieService {

    private final OmdbClient omdbClient;
    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private static final String IMAGE_DIR = "images/";
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    

    @Autowired
    public MovieService(OmdbClient omdbClient, TmdbClient tmdbClient, MovieRepository movieRepository, RestTemplate restTemplate) {
        this.omdbClient = omdbClient;
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
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
        
        // If movie found -> fetch and download images from TMDB
        if (tmdbMovie.isPresent()) {
            try {
                var imageList = tmdbClient.fetchMovieImages(tmdbMovie.get().getId());
                if (imageList != null && imageList.getBackdrops() != null) {
                    var localImagePaths = imageList.getBackdrops().stream()
                            .limit(3)
                            .map(img -> downloadAndSaveImage(img.getFilePath(), movie.getTitle() != null ? movie.getTitle() : title))
                            .filter(path -> path != null)
                            .toList();
                    movie.setImagePaths(localImagePaths);
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch movie images: " + e.getMessage());
            }
        }
        
        movie.setWatched(false);
        movie.setRating(0);
        
        return movieRepository.save(movie);
    }


    // service methods

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

    // Downloads the image and saves it locally.
    private String downloadAndSaveImage(String imagePath, String movieTitle) {
        try {
            String imageUrl = TMDB_IMAGE_BASE_URL + imagePath;
            byte[] imageData = restTemplate.getForObject(imageUrl, byte[].class);
            
            if (imageData != null) {
                String fileName = movieTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID() + ".jpg";
                Path localPath = Paths.get(IMAGE_DIR + fileName);
                Files.write(localPath, imageData);
                return localPath.toString();
            }
        } catch (Exception e) {
            // Log error but don't fail the entire operation
            System.err.println("Failed to download image: " + imagePath + " - " + e.getMessage());
        }
        return null;
    }
}
