package nl.inholland.student.client;

import nl.inholland.student.dto.tmdb.TmdbImageListDto;
import nl.inholland.student.dto.tmdb.TmdbMovieDto;
import nl.inholland.student.dto.tmdb.TmdbSearchListDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class TmdbClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public TmdbClient(RestTemplate restTemplate,
            @Value("${tmdb.api.baseUrl}") String baseUrl,
            @Value("${tmdb.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public Optional<TmdbMovieDto> searchMovie(String title) {
        String url = String.format("%s/search/movie?query=%s&api_key=%s", baseUrl, title, apiKey);
        TmdbSearchListDto searchResult = restTemplate.getForObject(url, TmdbSearchListDto.class);
        
        if (searchResult != null && searchResult.getResults() != null && !searchResult.getResults().isEmpty()) {
            return Optional.of(searchResult.getResults().get(0));
        }
        return Optional.empty();
    }

    public TmdbSearchListDto fetchSimilarMovies(long movieId) {
        String url = String.format("%s/movie/%d/similar?api_key=%s", baseUrl, movieId, apiKey);
        return restTemplate.getForObject(url, TmdbSearchListDto.class);
    }

    public TmdbImageListDto fetchMovieImages(long movieId) {
        String url = String.format("%s/movie/%d/images?api_key=%s", baseUrl, movieId, apiKey);
        return restTemplate.getForObject(url, TmdbImageListDto.class);
    }
}
