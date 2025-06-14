package com.watchlist.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDto {

    private long id;
    private String title;
}
