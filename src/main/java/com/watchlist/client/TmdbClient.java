// Class responsibe for Communications with TMDB api.
package com.watchlist.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.watchlist.dto.tmdb.TmdbImageListDto;
import com.watchlist.dto.tmdb.TmdbMovieDto;
import com.watchlist.dto.tmdb.TmdbSearchListDto;

import java.util.Optional;

@Service
public class TmdbClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    // Constructor to get objects to function 
    public TmdbClient(RestTemplate restTemplate,
            @Value("${tmdb.api.baseUrl}") String baseUrl, // pecific TMDB configuration details
            @Value("${tmdb.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    // Takes title -> returns the first search result on TMDB
    public Optional<TmdbMovieDto> searchMovie(String title) {
        String url = String.format("%s/search/movie?query=%s&api_key=%s", baseUrl, title, apiKey);
        TmdbSearchListDto searchResult = restTemplate.getForObject(url, TmdbSearchListDto.class);
        
        if (searchResult != null && searchResult.getResults() != null && !searchResult.getResults().isEmpty()) {
            return Optional.of(searchResult.getResults().get(0));
        }
        return Optional.empty();
    }

    // retrieves list of similar movies
    public TmdbSearchListDto fetchSimilarMovies(long movieId) {
        String url = String.format("%s/movie/%d/similar?api_key=%s", baseUrl, movieId, apiKey);
        return restTemplate.getForObject(url, TmdbSearchListDto.class);
    }

    // retrieves images paths
    public TmdbImageListDto fetchMovieImages(long movieId) {
        String url = String.format("%s/movie/%d/images?api_key=%s", baseUrl, movieId, apiKey);
        return restTemplate.getForObject(url, TmdbImageListDto.class);
    }
}
