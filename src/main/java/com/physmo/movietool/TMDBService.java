package com.physmo.movietool;

import com.physmo.movietool.domain.Genres;
import com.physmo.movietool.domain.Movie;
import com.physmo.movietool.domain.MovieCollection;
import com.physmo.movietool.domain.SearchMovieResult;
import com.physmo.movietool.domain.movieinfo.MovieInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: these service functions should return plain data structures,
//  formatting should be done by the report object

@Component
public class TMDBService {

    final Config config;

    public TMDBService(Config config) {
        this.config = config;
    }

    public Movie retrieveTMDBDataForFile(String fileName, String year) {
        SearchMovieResult result = searchMovie(fileName);

        // find the first entry that matches the given year

        int yearIndex = -1;
        for (int i = 0; i < result.getResults().length; i++) {
            Movie movie = result.getResults()[i];
            if (movie.getRelease_date() != null && movie.getRelease_date().contains(year)) {
                yearIndex = i;
                break;
            }
        }

        if (yearIndex != -1) return result.getResults()[yearIndex];

        if (result.getResults().length > 0) {
            return result.getResults()[0];
        }
        return null;
    }

    public SearchMovieResult searchMovie(String movieName) {
        String url = "https://api.themoviedb.org/3/search/movie?api_key={api_key}&language={language}&query={query}&page={page}&include_adult={include_adult}";
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("language", "en-US");
        uriVariables.put("query", movieName);
        uriVariables.put("page", 1);
        uriVariables.put("include_adult", false);
        uriVariables.put("api_key", config.getTmdbApiKey());

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, SearchMovieResult.class, uriVariables);
    }


    public MovieInfo getMovieInfo(int movieId) {
        String url = "https://api.themoviedb.org/3/movie/{id}?api_key={api_key}&language={language}";
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("api_key", config.getTmdbApiKey());
        uriVariables.put("language", "en-US");
        uriVariables.put("id", movieId);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, MovieInfo.class, uriVariables);
    }

    // TODO: ability to retrieve more pages.
    public Map<Integer, Movie> getPopularMoviesByYear(int year) {
        String url = "https://api.themoviedb.org/3/discover/movie?api_key={api_key}&primary_release_year={year}&with_original_language=en&sort_by={sort}&page={page}";
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("api_key", config.getTmdbApiKey());
        uriVariables.put("year", Integer.toString(year));
        uriVariables.put("sort", "popularity.desc");
        uriVariables.put("page", "1");

        List<String> sortList = new ArrayList<>();
        sortList.add("vote_count.desc");
        sortList.add("popularity.desc");
        sortList.add("revenue.desc");


        Map<Integer, Movie> movieMap = new HashMap<>();

        int pageCount = 2;

        for (String sortBy : sortList) {
            for (int page = 1; page <= pageCount; page++) {
                RestTemplate restTemplate = new RestTemplate();
                uriVariables.put("sort", sortBy);
                uriVariables.put("page", "" + page);
                SearchMovieResult searchMovieResult = restTemplate.getForObject(url, SearchMovieResult.class, uriVariables);

                for (Movie movie : searchMovieResult.getResults()) {
                    movieMap.put(movie.getId(), movie);
                }
            }
        }

        return movieMap;
    }

    //https://api.themoviedb.org/3/collection/123?api_key=<<api_key>>&language=en-US
    public MovieCollection getMovieCollection(int collectionId) {
        String url = "https://api.themoviedb.org/3/collection/{id}?api_key={api_key}&language={language}";
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("api_key", config.getTmdbApiKey());
        uriVariables.put("language", "en-US");
        uriVariables.put("id", collectionId);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, MovieCollection.class, uriVariables);
    }

    //https://api.themoviedb.org/3/genre/movie/list?api_key=<<api_key>>&language=en-US
    public  Genres getGenres() {
        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key={api_key}&language={language}";
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("api_key", config.getTmdbApiKey());
        uriVariables.put("language", "en-US");

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, Genres.class, uriVariables);
    }
}
