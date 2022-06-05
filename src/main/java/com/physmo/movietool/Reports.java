package com.physmo.movietool;

import com.physmo.movietool.domain.CollectionReportCollection;
import com.physmo.movietool.domain.CollectionsReportMovie;
import com.physmo.movietool.domain.DataStore;
import com.physmo.movietool.domain.FileListEntry;
import com.physmo.movietool.domain.Movie;
import com.physmo.movietool.domain.MovieCollection;
import com.physmo.movietool.domain.movieinfo.CollectionDetails;
import com.physmo.movietool.domain.movieinfo.MovieInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Reports {
    static final String BR = "<br>";

    private static final Logger log = LoggerFactory.getLogger(Reports.class);
    final Operations operations;
    final TMDBService tmdbService;

    final DataStore dataStore;

    final Config config;

    public Reports(Config config, Operations operations, TMDBService tmdbService, DataStore dataStore) {
        this.operations = operations;
        this.tmdbService = tmdbService;
        this.config = config;
        this.dataStore = dataStore;
    }

    public String getMoviesWithNoDate() {
        String ret = "";
        for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
            if (fileListEntry.getDatePart() == null || fileListEntry.getDatePart().equals("")) {
                ret += BR + "" + fileListEntry.getFileName();
            }
        }

        return ret;
    }

    public String getMovieInfo(int movieId) {
        MovieInfo movieInfo = dataStore.getMovieInfo().get(movieId);
        if (movieInfo == null) {
            return "movie not found";
        }

        String str = "";
        str += "<h2>" + movieInfo.getTitle() + " " + movieInfo.getRelease_date() + "</h2>";
        str += "<h3>" + movieInfo.getTagline() + "</h3>";

        str += "<table>";
        str += HtmlHelpers.makeTwoColumnTableEntryAsHtml("Popularity", String.valueOf(movieInfo.getPopularity()));
        str += HtmlHelpers.makeTwoColumnTableEntryAsHtml("Overview", movieInfo.getOverview());
        str += HtmlHelpers.makeTwoColumnTableEntryAsHtml("Budget", String.valueOf(movieInfo.getBudget()));
        str += HtmlHelpers.makeTwoColumnTableEntryAsHtml("Revenue", String.valueOf(movieInfo.getRevenue()));
        str += HtmlHelpers.makeTwoColumnTableEntryAsHtml("Runtime", String.valueOf(movieInfo.getRuntime()));

        str += "</table>";

        return str;

    }



    public String getLibrarySummary() {
        String ret = "";
        int numLocalFiles = dataStore.getFileListEntryList().size();
        ret += BR + "Local files: " + numLocalFiles;
        ret += BR + "Unmatched files: " + dataStore.countFilesWithNoId();
        ret += BR + "MOVIETOOL_MOVIEFOLDERPATH: " + config.getMovieFolderPath();
        ret += BR + "MOVIETOOL_TMDBAPIKEY: " + config.getTmdbApiKey();
        ret += BR + "MOVIETOOL_DATAFILEPATH: " + config.getDataFilePath();

        String fileTypes = "";
        for (String allowedFileType : config.getAllowedFileTypes()) {
            fileTypes += allowedFileType + ",";
        }

        ret += BR + "MOVIETOOL_ALLOWEDFILETYPES: " + fileTypes;


        return ret;
    }

    public String getFileList() {
        String output = "";

        output += "<table id='file-list-table' class='display compact stripe' style='width:100%'>";
        output += "<thead><tr><th>Name</th><th>Year</th><th>Rating</th><th>Collection</th><th>Run Time</th></tr></thead>";
        for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
            MovieInfo movieInfo = dataStore.getMovieInfo().get(fileListEntry.getId());

            output += "<tr>";

            List<String> fileListRow = buildFileListRow(fileListEntry);
            for (String tableData : fileListRow) {
                output += "<td>" + tableData + "</td>";
            }

            output += "</tr>";
        }
        output += "</table>";

        return output;
    }

    public List<String> buildFileListRow(FileListEntry fileListEntry) {
        List<String> entries = new ArrayList<>(5);

        for (int i = 0; i < 5; i++) {
            entries.add("");
        }

        Movie movie = dataStore.getMovieMap().get(fileListEntry.getId());
        MovieInfo movieInfo = null;
        if (movie != null) movieInfo = dataStore.getMovieInfo().get(movie.getId());

        // Movie / File name.
        if (movie != null) {
            String linkedTitle = HtmlHelpers.makeLinkAsHtml(movie.getTitle(), "/movieinfo/" + movie.getId());
            entries.set(0, linkedTitle);
        } else {
            entries.set(0, fileListEntry.getFileName());
        }

        // Year
        if (movie != null) {
            entries.set(1, "" + HtmlHelpers.getYearFromReleaseDate(movie.getRelease_date()));
        }

        // Rating
        if (movieInfo != null) {
            entries.set(2, "" + movieInfo.getVote_average());
        }

        // Collection
        if (movieInfo != null) {
            CollectionDetails belongs_to_collection = movieInfo.getBelongs_to_collection();
            if (belongs_to_collection != null) {

                int collectionId = belongs_to_collection.getId();
                String collectionLink = "<a href='/collectionsreport#"+collectionId+"'>"+belongs_to_collection.getName()+"</a>";
                entries.set(3, collectionLink);

            }
        }

        // Runtime
        if (movieInfo != null) {
            if (movieInfo.getRuntime() == null) entries.set(4, "0");
            else entries.set(4, "" + movieInfo.getRuntime());
        } else {
            entries.set(4, "0");
        }


        return entries;
    }





    public String getUnmatchedFileList() {
        String str = "";
        String table = "";

        int count = 0;

        table += "<table>";
        for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
            if (fileListEntry.getId() != 0) continue;
            count++;
            table += "<tr>";
            table += "<td>" + fileListEntry.getFileName() + "</td>";
            table += "</tr>";
        }
        table += "</table>";

        str += count + " movie names could not be found online.";

        return str + BR + table;
    }

    public String findDuplicates() {
        Map<Integer, Integer> counts = new HashMap<>();
        String str = "";
        for (FileListEntry file : dataStore.getFileListEntryList()) {
            if (file.getId() == 0) continue;

            if (!counts.containsKey(file.getId())) {
                counts.put(file.getId(), 1);
            } else {
                counts.put(file.getId(), counts.get(file.getId()) + 1);
            }
        }

        for (Integer movieId : counts.keySet()) {
            if (counts.get(movieId) < 2) continue;
            MovieInfo movieInfo = dataStore.getMovieInfo().get(movieId);
            str += BR;
            for (FileListEntry file : dataStore.getFileListEntryList()) {
                if (file.getId() == movieId) {
                    str += BR + file.getFileName() + " - TMDB ID:" + movieId + " - TMDB Title:" + movieInfo.getOriginal_title();
                }
            }
        }

        return str;

    }

    public String findMissingPopularMoviesForYear(int year) {

        Map<Integer, Movie> popularMoviesByYear = tmdbService.getPopularMoviesByYear(year);
        String str = "";
        List<Movie> ownedList = new ArrayList<>();
        List<Movie> needList = new ArrayList<>();

        for (Integer movieId : popularMoviesByYear.keySet()) {
            Movie movie = popularMoviesByYear.get(movieId);
            boolean owned = false;
            for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
                if (fileListEntry.getId() == movieId) {
                    owned = true;
                    break;
                }
            }

            if (owned) {
                ownedList.add(movie);
            } else {
                needList.add(movie);
            }
        }

        ownedList.sort(Comparator.comparing(Movie::getVote_average).reversed());
        needList.sort(Comparator.comparing(Movie::getVote_average).reversed());

        for (Movie m : ownedList) {
            Double rating = m.getVote_average();
            String fullMovieString = createMovieDetailsRow(
                    m.getId(), m.getTitle(), "" + year, true, rating, null, null);
            str += fullMovieString;
        }
        str += BR;
        for (Movie m : needList) {

            String overview = HtmlHelpers.sanitizeText(m.getOverview());
            double rating = m.getVote_average();
            String fullMovieString = createMovieDetailsRow(
                    m.getId(), m.getTitle(), "" + year, false, rating, getGenres(m), overview);
            str += fullMovieString;
        }


        return str;
    }

    public String getGenres(Movie movie) {
        String str = "";
        Map<Integer, String> genreMap = dataStore.getGenreMap();
        for (Integer genre_id : movie.getGenre_ids()) {
            String genreString = genreMap.getOrDefault(genre_id, "unknown");
            str += genreString + " ";
        }
        return "<span class='text-genres'>" + str + "</span>";
    }



    public String createMovieDetailsRow(int movieId, String name, String date, Boolean owned, Double rating, String genres, String info) {

        String str = "";
        String TICK_IMAGE = "<img src='/tick.png' width='16' height='16'>";
        String namePart = name;

        // Build it
        str += BR + name + " ";
        if (date != null) str += " <span class='text-year'>" + date + "</span>";
        if (info != null) {
            str += " " + HtmlHelpers.createTooltipIconAsHtml(info);
        }
        if (owned) str += " " + TICK_IMAGE;
        if (rating != null) str += HtmlHelpers.formatRatingAsHtml(rating);
        if (genres != null) str += " <span class='text-genres'>" + genres + "</span>";


        return str;
    }




    public String getCollectionsReport() {
        String str = "";
        log.info("Generating collections report");

        List<CollectionReportCollection> collections = new ArrayList<>();

        for (MovieCollection movieCollection : dataStore.getMovieCollectionMap().values()) {
            CollectionReportCollection newCollection = new CollectionReportCollection();
            newCollection.setName(movieCollection.getName());
            newCollection.setId(movieCollection.getId());

            for (Movie movie : movieCollection.getParts()) {
                boolean owned = false;
                for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
                    if (fileListEntry.getId() == movie.getId()) {
                        owned = true;
                        break;
                    }
                }

                CollectionsReportMovie newMovie = new CollectionsReportMovie();
                newMovie.set(movie.getTitle(), movie.getRelease_date(), owned, movie.getId());

                newCollection.getMovies().add(newMovie);

            }
            collections.add(newCollection);
        }

        // Build output.
        for (CollectionReportCollection collection : collections) {
            if (isFullSet(collection)) str += formatCollection(collection, true) + "<br><br>";
        }
        for (CollectionReportCollection collection : collections) {
            if (!isFullSet(collection)) str += formatCollection(collection, false) + "<br><br>";
        }

        log.info("Generating collections report - DONE");

        return str;
    }

    public boolean isFullSet(CollectionReportCollection collection) {
        for (CollectionsReportMovie movie : collection.getMovies()) {
            if (!movie.isOwned() || movie.getDate().isEmpty()) return false;
            if (movie.getDate().isBlank()) return false;
        }
        return true;
    }

    public String formatCollection(CollectionReportCollection collection, boolean full) {
        String anchor = "<a id='"+collection.getId()+"'></a>";
        String str = anchor+"<b>" + collection.getName() + "</b>";

        if (full) {
            str += " <span class='badge rounded-pill bg-success'>Complete</span>";
        }

        collection.getMovies().sort((o1, o2) -> {
            String date1 = o1.getDate();
            String date2 = o2.getDate();
            if (date1.isEmpty()) date1 = "9999-99-99";
            if (date2.isEmpty()) date2 = "9999-99-99";
            return date1.compareToIgnoreCase(date2);
        });

        for (CollectionsReportMovie movie : collection.getMovies()) {

            int iYear = HtmlHelpers.getYearFromReleaseDate(movie.getDate());
            String year = iYear == -1 ? "unreleased" : "" + iYear;

            MovieInfo movieInfo = dataStore.getMovieInfo().get(movie.getId());
            double rating = -1;
            if (movieInfo != null) {
                rating = movieInfo.getVote_average();
            }

            String fullMovieString = createMovieDetailsRow(
                    movie.getId(), movie.getName(), year, movie.isOwned(), rating == -1 ? null : rating, null, null);
            str += fullMovieString;
        }
        return str;
    }

    public String countMoviesPerYear() {
        Map<Integer, Integer> yearCounts = new HashMap<>();

        String str = "";

        for (Integer movieId : dataStore.getMovieMap().keySet()) {
            Movie movie = dataStore.getMovieMap().get(movieId);
            int year = HtmlHelpers.getYearFromReleaseDate(movie.getRelease_date());
            if (year != -1) {
                int count = 1;
                if (yearCounts.get(year) != null) {
                    count = yearCounts.get(year) + 1;
                }
                yearCounts.put(year, count);
            }
        }

        int maxCount = 0;
        for (Integer key : yearCounts.keySet()) {
            if (yearCounts.get(key) > maxCount) {
                maxCount = yearCounts.get(key);
            }
        }


        for (Integer key : yearCounts.keySet()) {

            int count = yearCounts.get(key);

            String yearLink = HtmlHelpers.makeLinkAsHtml(key.toString(), "findmissingpopularmovies/" + key);

            str += "<div class='row'>";
            str += "<div class='col-md-1'>";
            str += "" + yearLink + " ";
            str += "</div>";
            str += "<div class='col-md-10'>";
            str += HtmlHelpers.createBarAsHtml(count, maxCount);
            str += "</div>";
            str += "</div>";
        }


        return str;
    }



}
