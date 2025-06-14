// This class responible for the communications with OMDB api.
package com.watchlist.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.watchlist.dto.omdb.OmdbMovieDto;

@Service
public class OmdbClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    // Constructor to get objects to function 
    public OmdbClient(RestTemplate restTemplate,
            @Value("${omdb.api.baseUrl}") String baseUrl,
            @Value("${omdb.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    // Takes title -> resurns data from omdb api as a stuctured java object
    public OmdbMovieDto fetchMovieByTitle(String title) {
        String url = String.format("%s?t=%s&apikey=%s", baseUrl, title, apiKey);
        return restTemplate.getForObject(url, OmdbMovieDto.class);
    }
}
