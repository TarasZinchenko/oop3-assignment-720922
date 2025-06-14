package com.watchlist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.watchlist.client.OmdbClient;
import com.watchlist.client.TmdbClient;
import com.watchlist.dto.omdb.OmdbMovieDto;
import com.watchlist.dto.tmdb.TmdbImageDto;
import com.watchlist.dto.tmdb.TmdbImageListDto;
import com.watchlist.dto.tmdb.TmdbMovieDto;
import com.watchlist.exception.InvalidArgumentException;
import com.watchlist.exception.ResourceNotFoundException;
import com.watchlist.model.Movie;
import com.watchlist.repository.MovieRepository;
import com.watchlist.service.MovieService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private OmdbClient omdbClient;

    @Mock
    private TmdbClient tmdbClient;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RestTemplate restTemplate;

    private MovieService movieService;

    private OmdbMovieDto omdbMovieDto;
    private TmdbMovieDto tmdbMovieDto;
    private TmdbImageListDto imageListDto;
    private Movie savedMovie;

    @BeforeEach
    void setUp() {
        // Create MovieService manually with mocks
        movieService = new MovieService(omdbClient, tmdbClient, movieRepository);

        // Setup OMDb response
        omdbMovieDto = new OmdbMovieDto();
        ReflectionTestUtils.setField(omdbMovieDto, "title", "Inception");
        ReflectionTestUtils.setField(omdbMovieDto, "year", "2010");
        ReflectionTestUtils.setField(omdbMovieDto, "director", "Christopher Nolan");
        ReflectionTestUtils.setField(omdbMovieDto, "genre", "Action, Sci-Fi");

        // Setup TMDB movie response
        tmdbMovieDto = new TmdbMovieDto();
        ReflectionTestUtils.setField(tmdbMovieDto, "id", 27205L);
        ReflectionTestUtils.setField(tmdbMovieDto, "title", "Inception");

        // Setup TMDB images response
        TmdbImageDto image1 = new TmdbImageDto();
        ReflectionTestUtils.setField(image1, "filePath", "/backdrop1.jpg");
        TmdbImageDto image2 = new TmdbImageDto();
        ReflectionTestUtils.setField(image2, "filePath", "/backdrop2.jpg");

        imageListDto = new TmdbImageListDto();
        ReflectionTestUtils.setField(imageListDto, "backdrops", Arrays.asList(image1, image2));

        // Setup saved movie
        savedMovie = new Movie();
        ReflectionTestUtils.setField(savedMovie, "id", 1L);
        ReflectionTestUtils.setField(savedMovie, "title", "Inception");
        ReflectionTestUtils.setField(savedMovie, "year", "2010");
        ReflectionTestUtils.setField(savedMovie, "director", "Christopher Nolan");
        ReflectionTestUtils.setField(savedMovie, "genre", "Action, Sci-Fi");
        ReflectionTestUtils.setField(savedMovie, "watched", false);
        ReflectionTestUtils.setField(savedMovie, "rating", 0);
    }

    @Test
    void addMovieToWatchlist_ShouldSaveMovie_WhenValidTitle() {
        // Given
        when(omdbClient.fetchMovieByTitle("Inception")).thenReturn(omdbMovieDto);
        when(tmdbClient.searchMovie("Inception")).thenReturn(Optional.of(tmdbMovieDto));
        when(tmdbClient.fetchMovieImages(27205L)).thenReturn(imageListDto);
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // When
        Movie result = movieService.addMovieToWatchlist("Inception");

        // Then
        assertNotNull(result);
        assertEquals("Inception", result.getTitle());
        assertEquals("2010", result.getYear());
        assertEquals("Christopher Nolan", result.getDirector());
        assertEquals("Action, Sci-Fi", result.getGenre());
        assertFalse(result.isWatched());
        assertEquals(0, result.getRating());

        verify(omdbClient, times(1)).fetchMovieByTitle("Inception");
        verify(tmdbClient, times(1)).searchMovie("Inception");
        verify(tmdbClient, times(1)).fetchMovieImages(27205L);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void addMovieToWatchlist_ShouldHandleNullOmdbResponse() {
        // Given
        when(omdbClient.fetchMovieByTitle("UnknownMovie")).thenReturn(null);
        when(tmdbClient.searchMovie("UnknownMovie")).thenReturn(Optional.empty());
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // When
        Movie result = movieService.addMovieToWatchlist("UnknownMovie");

        // Then
        assertNotNull(result);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void addMovieToWatchlist_ShouldHandleEmptyTmdbResponse() {
        // Given
        when(omdbClient.fetchMovieByTitle("Inception")).thenReturn(omdbMovieDto);
        when(tmdbClient.searchMovie("Inception")).thenReturn(Optional.empty());
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // When
        Movie result = movieService.addMovieToWatchlist("Inception");

        // Then
        assertNotNull(result);
        verify(tmdbClient, never()).fetchMovieImages(anyLong());
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void getAllMovies_ShouldReturnPagedResults() {
        // Given
        List<Movie> movies = Arrays.asList(savedMovie);
        Page<Movie> page = new PageImpl<>(movies);
        Pageable pageable = PageRequest.of(0, 10);
        when(movieRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<Movie> result = movieService.getAllMovies(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Inception", result.getContent().get(0).getTitle());
        verify(movieRepository, times(1)).findAll(pageable);
    }

    @Test
    void updateWatchedStatus_ShouldUpdateAndSave_WhenMovieExists() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(savedMovie));
        savedMovie.setWatched(true);
        when(movieRepository.save(savedMovie)).thenReturn(savedMovie);

        // When
        Movie result = movieService.updateWatchedStatus(1L, true);

        // Then
        assertNotNull(result);
        assertTrue(result.isWatched());
        verify(movieRepository, times(1)).findById(1L);
        verify(movieRepository, times(1)).save(savedMovie);
    }

    @Test
    void updateWatchedStatus_ShouldThrowException_WhenMovieNotFound() {
        // Given
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> movieService.updateWatchedStatus(999L, true));
        
        assertEquals("Movie not found", exception.getMessage());
        verify(movieRepository, times(1)).findById(999L);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void updateRating_ShouldUpdateAndSave_WhenValidRating() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(savedMovie));
        savedMovie.setRating(4);
        when(movieRepository.save(savedMovie)).thenReturn(savedMovie);

        // When
        Movie result = movieService.updateRating(1L, 4);

        // Then
        assertNotNull(result);
        assertEquals(4, result.getRating());
        verify(movieRepository, times(1)).findById(1L);
        verify(movieRepository, times(1)).save(savedMovie);
    }

    @Test
    void updateRating_ShouldThrowException_WhenInvalidRating() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> movieService.updateRating(1L, 10));
        
        assertEquals("Rating must be between 0 and 5", exception.getMessage());
        verify(movieRepository, never()).findById(anyLong());
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void updateRating_ShouldThrowException_WhenMovieNotFound() {
        // Given
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> movieService.updateRating(999L, 3));
        
        assertEquals("Movie not found", exception.getMessage());
        verify(movieRepository, times(1)).findById(999L);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void deleteMovie_ShouldDelete_WhenMovieExists() {
        // Given
        when(movieRepository.existsById(1L)).thenReturn(true);

        // When
        movieService.deleteMovie(1L);

        // Then
        verify(movieRepository, times(1)).existsById(1L);
        verify(movieRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteMovie_ShouldThrowException_WhenMovieNotFound() {
        // Given
        when(movieRepository.existsById(999L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> movieService.deleteMovie(999L));
        
        assertEquals("Movie not found", exception.getMessage());
        verify(movieRepository, times(1)).existsById(999L);
        verify(movieRepository, never()).deleteById(anyLong());
    }
}
