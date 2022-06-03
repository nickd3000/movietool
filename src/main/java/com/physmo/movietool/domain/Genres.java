package com.physmo.movietool.domain;

import java.io.Serializable;

public class Genres implements Serializable {
    private Genre[] genres;

    public Genre[] getGenres() {
        return genres;
    }

    public void setGenres(Genre[] genres) {
        this.genres = genres;
    }
}
