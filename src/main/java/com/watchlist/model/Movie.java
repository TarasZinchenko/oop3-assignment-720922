// A Data Model
package com.watchlist.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(name = "release_year")
    private String year;
    
    private String director;
    private String genre;

    @ElementCollection
    private List<String> imagePaths;

    private boolean watched = false;
    private int rating = 0;
}
