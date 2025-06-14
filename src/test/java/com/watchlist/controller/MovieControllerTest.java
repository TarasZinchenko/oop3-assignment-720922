package com.watchlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchlist.controller.MovieController;
import com.watchlist.exception.InvalidArgumentException;
import com.watchlist.exception.ResourceNotFoundException;
import com.watchlist.model.Movie;
import com.watchlist.service.MovieService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = new Movie();
        ReflectionTestUtils.setField(testMovie, "id", 1L);
        ReflectionTestUtils.setField(testMovie, "title", "Inception");
        ReflectionTestUtils.setField(testMovie, "year", "2010");
        ReflectionTestUtils.setField(testMovie, "director", "Christopher Nolan");
        ReflectionTestUtils.setField(testMovie, "genre", "Action, Sci-Fi");
        ReflectionTestUtils.setField(testMovie, "watched", false);
        ReflectionTestUtils.setField(testMovie, "rating", 0);
    }

    @Test
    void addMovie_ShouldReturnMovie_WhenValidTitle() throws Exception {
        when(movieService.addMovieToWatchlist("Inception")).thenReturn(testMovie);

        mockMvc.perform(post("/api/watchlist/Inception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.director").value("Christopher Nolan"));

        verify(movieService, times(1)).addMovieToWatchlist("Inception");
    }

    @Test
    void addMovie_ShouldReturnBadRequest_WhenServiceThrowsException() throws Exception {
        when(movieService.addMovieToWatchlist("InvalidMovie")).thenThrow(new RuntimeException("Movie not found"));

        mockMvc.perform(post("/api/watchlist/InvalidMovie"))
                .andExpect(status().isInternalServerError());

        verify(movieService, times(1)).addMovieToWatchlist("InvalidMovie");
    }

    @Test
    void getAllMovies_ShouldReturnPagedMovies() throws Exception {
        List<Movie> movies = Arrays.asList(testMovie);
        Page<Movie> moviePage = new PageImpl<>(movies, PageRequest.of(0, 10), 1);

        when(movieService.getAllMovies(any(Pageable.class))).thenReturn(moviePage);

        mockMvc.perform(get("/api/watchlist")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Inception"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(movieService, times(1)).getAllMovies(any(Pageable.class));
    }

    @Test
    void updateWatchedStatus_ShouldReturnUpdatedMovie() throws Exception {
        Movie updatedMovie = new Movie();
        ReflectionTestUtils.setField(updatedMovie, "id", 1L);
        ReflectionTestUtils.setField(updatedMovie, "title", "Inception");
        ReflectionTestUtils.setField(updatedMovie, "watched", true);
        when(movieService.updateWatchedStatus(1L, true)).thenReturn(updatedMovie);

        mockMvc.perform(patch("/api/watchlist/1/watched")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watched").value(true));

        verify(movieService, times(1)).updateWatchedStatus(1L, true);
    }

    @Test
    void updateWatchedStatus_ShouldReturnNotFound_WhenMovieNotExists() throws Exception {
        when(movieService.updateWatchedStatus(999L, true)).thenThrow(new ResourceNotFoundException("Movie not found with id: 999"));

        mockMvc.perform(patch("/api/watchlist/999/watched")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("true"))
                .andExpect(status().isNotFound());

        verify(movieService, times(1)).updateWatchedStatus(999L, true);
    }

    @Test
    void updateRating_ShouldReturnUpdatedMovie() throws Exception {
        Movie updatedMovie = new Movie();
        ReflectionTestUtils.setField(updatedMovie, "id", 1L);
        ReflectionTestUtils.setField(updatedMovie, "title", "Inception");
        ReflectionTestUtils.setField(updatedMovie, "rating", 4);
        when(movieService.updateRating(1L, 4)).thenReturn(updatedMovie);

        mockMvc.perform(patch("/api/watchlist/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));

        verify(movieService, times(1)).updateRating(1L, 4);
    }

    @Test
    void updateRating_ShouldReturnBadRequest_WhenInvalidRating() throws Exception {
        when(movieService.updateRating(1L, 10)).thenThrow(new InvalidArgumentException("Rating must be between 0 and 5, received: 10"));

        mockMvc.perform(patch("/api/watchlist/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10"))
                .andExpect(status().isBadRequest());

        verify(movieService, times(1)).updateRating(1L, 10);
    }

    @Test
    void deleteMovie_ShouldReturnNoContent() throws Exception {
        doNothing().when(movieService).deleteMovie(1L);

        mockMvc.perform(delete("/api/watchlist/1"))
                .andExpect(status().isNoContent());

        verify(movieService, times(1)).deleteMovie(1L);
    }

    @Test
    void deleteMovie_ShouldReturnNotFound_WhenMovieNotExists() throws Exception {
        doThrow(new ResourceNotFoundException("Movie not found with id: 999")).when(movieService).deleteMovie(999L);

        mockMvc.perform(delete("/api/watchlist/999"))
                .andExpect(status().isNotFound());

        verify(movieService, times(1)).deleteMovie(999L);
    }
}
