package com.physmo.movietool;

import com.physmo.movietool.domain.Movie;
import com.physmo.movietool.domain.MovieCollection;
import com.physmo.movietool.domain.movieinfo.MovieInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class TestTMDBService {

    @Autowired
    TMDBService tmdbService;

    @Test
    public void t1() {
        //TMDBService tmdbService = new TMDBService();
        //tmdbService.retrieveTMDBDataForFile("jexi", "2019");
    }

    @Test
    public void testGetMovieInfo() {
        //TMDBService tmdbService = new TMDBService();
//        MovieInfo movieInfo = tmdbService.getMovieInfo(2605);
//        System.out.println(movieInfo != null);
    }

    @Test
    public void testGetPopularMoviesByYear() {

        //TMDBService tmdbService = new TMDBService();
//        Map<Integer, Movie> movieMap = tmdbService.getPopularMoviesByYear(2015);
//
//        System.out.println("Done");
    }

    @Test
    public void testGetMovieCollection() {
        //TMDBService tmdbService = new TMDBService();
//        MovieCollection movieCollection = tmdbService.getMovieCollection(10);
//        System.out.println(movieCollection != null);
    }
}
