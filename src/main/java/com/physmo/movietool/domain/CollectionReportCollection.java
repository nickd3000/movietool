package com.physmo.movietool.domain;

import java.util.ArrayList;
import java.util.List;

public class CollectionReportCollection {
    String name;
    List<CollectionsReportMovie> movies = new ArrayList<>();

    public List<CollectionsReportMovie> getMovies() {
        return movies;
    }

    public void setMovies(List<CollectionsReportMovie> movies) {
        this.movies = movies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
