package nl.inholland.student.service;

import nl.inholland.student.client.OmdbClient;
import nl.inholland.student.client.TmdbClient;
import nl.inholland.student.dto.omdb.OmdbMovieDto;
import nl.inholland.student.dto.tmdb.TmdbImageListDto;
import nl.inholland.student.dto.tmdb.TmdbMovieDto;
import nl.inholland.student.dto.tmdb.TmdbSearchListDto;
import nl.inholland.student.model.Movie;
import nl.inholland.student.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class MovieService {

    private final OmdbClient omdbClient;
    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w780";
    private static final String IMAGE_DIR = "images/";

    @Autowired
    public MovieService(OmdbClient omdbClient, TmdbClient tmdbClient, MovieRepository movieRepository, RestTemplate restTemplate) {
        this.omdbClient = omdbClient;
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
        createImageDirectory();
    }

    private void createImageDirectory() {
        try {
            Path imageDir = Paths.get(IMAGE_DIR);
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create image directory", e);
        }
    }

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

    @Async
    public CompletableFuture<OmdbMovieDto> fetchOmdbData(String title) {
        OmdbMovieDto result = omdbClient.fetchMovieByTitle(title);
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<Optional<TmdbMovieDto>> fetchTmdbData(String title) {
        Optional<TmdbMovieDto> result = tmdbClient.searchMovie(title);
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<TmdbImageListDto> fetchTmdbImages(long movieId) {
        TmdbImageListDto result = tmdbClient.fetchMovieImages(movieId);
        return CompletableFuture.completedFuture(result);
    }

    public Movie addMovieToWatchlist(String title) {
        // Start parallel API calls
        CompletableFuture<OmdbMovieDto> omdbFuture = fetchOmdbData(title);
        CompletableFuture<Optional<TmdbMovieDto>> tmdbFuture = fetchTmdbData(title);

        // Wait for both to complete
        CompletableFuture.allOf(omdbFuture, tmdbFuture).join();

        // Get results
        OmdbMovieDto omdbMovie = omdbFuture.join();
        Optional<TmdbMovieDto> tmdbMovieOpt = tmdbFuture.join();

        // Create new Movie entity
        Movie movie = new Movie();

        // Map OMDb data
        if (omdbMovie != null) {
            movie.setTitle(omdbMovie.getTitle());
            movie.setYear(omdbMovie.getYear());
            movie.setDirector(omdbMovie.getDirector());
            movie.setGenre(omdbMovie.getGenre());
        }

        // Handle TMDB data and download images
        List<String> localImagePaths = new ArrayList<>();
        if (tmdbMovieOpt.isPresent()) {
            TmdbMovieDto tmdbMovie = tmdbMovieOpt.get();
            
            // Fetch images asynchronously
            CompletableFuture<TmdbImageListDto> imagesFuture = fetchTmdbImages(tmdbMovie.getId());
            TmdbImageListDto imageList = imagesFuture.join();
            
            if (imageList != null && imageList.getBackdrops() != null) {
                // Download first 3 images
                imageList.getBackdrops().stream()
                    .limit(3)
                    .forEach(image -> {
                        String localPath = downloadAndSaveImage(image.getFilePath(), 
                            omdbMovie != null ? omdbMovie.getTitle() : "unknown");
                        if (localPath != null) {
                            localImagePaths.add(localPath);
                        }
                    });
            }
        }

        // Set all properties
        movie.setImagePaths(localImagePaths);
        movie.setWatched(false);
        movie.setRating(0);

        return movieRepository.save(movie);
    }

    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    public Movie updateWatchedStatus(Long id, boolean watched) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie not found"));
        movie.setWatched(watched);
        return movieRepository.save(movie);
    }

    public Movie updateRating(Long id, int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie not found"));
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
