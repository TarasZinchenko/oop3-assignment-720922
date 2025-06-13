package nl.inholland.student.client;

import nl.inholland.student.dto.omdb.OmdbMovieDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OmdbClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public OmdbClient(RestTemplate restTemplate,
            @Value("${omdb.api.baseUrl}") String baseUrl,
            @Value("${omdb.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public OmdbMovieDto fetchMovieByTitle(String title) {
        String url = String.format("%s?t=%s&apikey=%s", baseUrl, title, apiKey);
        return restTemplate.getForObject(url, OmdbMovieDto.class);
    }
}
