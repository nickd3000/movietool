package com.physmo.movietool;

import com.physmo.movietool.domain.CollectionReportCollection;
import com.physmo.movietool.domain.CollectionsReportMovie;
import com.physmo.movietool.domain.DataStore;
import com.physmo.movietool.domain.FileListEntry;
import com.physmo.movietool.domain.Movie;
import com.physmo.movietool.domain.MovieCollection;
import com.physmo.movietool.domain.movieinfo.CollectionDetails;
import com.physmo.movietool.domain.movieinfo.MovieInfo;
import org.apache.commons.text.StringEscapeUtils;
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
    static final String INFO = "&#x2139;";
    static final String OWNED = "<span class='badge rounded-pill bg-primary'>Owned</span>";
    private static final Logger log = LoggerFactory.getLogger(Reports.class);
    final Operations operations;
    final TMDBService tmdbService;

    final Config config;

    public Reports(Config config, Operations operations, TMDBService tmdbService) {
        this.operations = operations;
        this.tmdbService = tmdbService;
        this.config = config;
    }

    public String getLocalList() {
        return "";
    }

    public String getMoviesWithNoDate(DataStore dataStore) {
        String ret = "";
        for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
            if (fileListEntry.getDatePart() == null || fileListEntry.getDatePart().equals("")) {
                ret += BR + "" + fileListEntry.getFileName();
            }
        }

        return ret;
    }

    public String getMovieInfo(DataStore dataStore, int movieId) {
        MovieInfo movieInfo = dataStore.getMovieInfo().get(movieId);
        if (movieInfo == null) {
            return "movie not found";
        }

        String str = "";
        str += "<h2>" + movieInfo.getTitle() + " " + movieInfo.getRelease_date() + "</h2>";
        str += "<h3>" + movieInfo.getTagline() + "</h3>";

        str += "<table>";
        str += makeTwoColumnTableEntry("Popularity", String.valueOf(movieInfo.getPopularity()));
        str += makeTwoColumnTableEntry("Overview", movieInfo.getOverview());
        str += makeTwoColumnTableEntry("Budget", String.valueOf(movieInfo.getBudget()));
        str += makeTwoColumnTableEntry("Revenue", String.valueOf(movieInfo.getRevenue()));
        str += makeTwoColumnTableEntry("Runtime", String.valueOf(movieInfo.getRuntime()));

        str += "</table>";

        return str;

    }

    public String makeTwoColumnTableEntry(String str1, String str2) {
        return "<tr><td>" + str1 + "</td><td>" + str2 + "</td></tr>";
    }

    public String getLibrarySummary(DataStore dataStore) {
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

    public String getFileList(DataStore dataStore) {
        String output = "";

        boolean includeCollection = true;

        output += "<table>";
        for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
            output += "<tr>";
            output += "<td>" + fileListEntry.getFileName() + "</td>";
            output += "<td>" + fileListEntry.getId() + "</td>";
            if (fileListEntry.getId() != 0) {
                Movie movie = dataStore.getMovieMap().get(fileListEntry.getId());
                if (movie == null) {
                    output += "<td>" + "movie not found in map" + "</td>";
                } else {
                    String linkedTitle = makeLink(movie.getTitle(), "/movieinfo/" + movie.getId());
                    output += "<td>" + linkedTitle + "</td>";
                }
            } else {
                output += "<td>" + "unmatched" + "</td>";
            }

            if (includeCollection) {
                output += "<td>" + getCollectionNameForMovieId(dataStore, fileListEntry.getId()) + "</td>";
            }

            output += "</tr>";
        }
        output += "</table>";

        return output;
    }

    public String getCollectionNameForMovieId(DataStore dataStore, int movieId) {
        if (movieId == 0) return "";

        MovieInfo movieInfo = dataStore.getMovieInfo().get(movieId);
        if (movieInfo == null) return "";

        CollectionDetails belongs_to_collection = movieInfo.getBelongs_to_collection();
        if (belongs_to_collection == null) return "";

        return belongs_to_collection.getName();
    }

    public String makeLink(String text, String link) {
        return "<a href='" + link + "'>" + text + "</a>";
    }

    public String getUnmatchedFileList(DataStore dataStore) {
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

    public String findDuplicates(DataStore dataStore) {
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

    public String findMissingPopularMoviesForYear(DataStore dataStore, int year) {

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
            String title = sanitizeText(m.getTitle());
            str += BR + title + "  " + m.getVote_average() + " " + OWNED;
        }
        str += BR;
        for (Movie m : needList) {
            String title = sanitizeText(m.getTitle());
            String overview = sanitizeText(m.getOverview());
            str += BR + addTooltipToText(title + " " + year, overview) + "  " + m.getVote_average();
        }


        return str;
    }

    public String sanitizeText(String txt) {
        txt = txt.replace("'", "");
        return StringEscapeUtils.escapeHtml4(txt);
    }

    public String addTooltipToText(String text, String tooltip) {
        String str = text + " <span  data-bs-toggle='tooltip' data-bs-placement='right' title='" + tooltip + "' ><span class='badge text-bg-primary'>info</span></span>";
        return str;
    }

    public String getCollectionsReport(DataStore dataStore) {
        String str = "";
        log.info("Generating collections report");

        // Retrieve any collections we haven't yet received from TMDB.
        // REMOVED: this should be done during scan
        //operations.retrieveMovieCollections(dataStore);
        //operations.saveDataStore(dataStore);

        List<CollectionReportCollection> collections = new ArrayList<>();

        for (MovieCollection movieCollection : dataStore.getMovieCollectionMap().values()) {
            CollectionReportCollection newCollection = new CollectionReportCollection();
            newCollection.setName(movieCollection.getName());

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

                //String linkedTitle = makeLink(title, "/movieinfo/" + movie.getId());

                newCollection.getMovies().add(newMovie);

            }
            collections.add(newCollection);
        }

        // Build output.

        for (CollectionReportCollection collection : collections) {
            if (isFullSet(collection)) str += formatCollection(collection);
        }
        for (CollectionReportCollection collection : collections) {
            if (!isFullSet(collection)) str += formatCollection(collection);
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

    public String formatCollection(CollectionReportCollection collection) {
        String str = BR + BR + "<b>" + collection.getName() + "</b>";

        collection.getMovies().sort((o1, o2) -> {
            String date1 = o1.getDate();
            String date2 = o2.getDate();
            if (date1.isEmpty()) date1 = "9999-99-99";
            if (date2.isEmpty()) date2 = "9999-99-99";
            return date1.compareToIgnoreCase(date2);
        });

        for (CollectionsReportMovie movie : collection.getMovies()) {
            if (movie.isOwned()) {
                str += BR + movie.getName() + " (" + movie.getDate() + ") " + OWNED;
            } else {
                str += BR + movie.getName() + " (" + movie.getDate() + ")";
            }
        }
        return str;
    }

    public String countMoviesPerYear(DataStore dataStore) {
        Map<Integer, Integer> yearCounts = new HashMap<>();

        String str = "";

        for (Integer movieId : dataStore.getMovieMap().keySet()) {
            Movie movie = dataStore.getMovieMap().get(movieId);
            int year = getYearFromReleaseDate(movie.getRelease_date());
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

            String yearLink = makeLink(key.toString(), "findmissingpopularmovies/" + key);

            str += "<div class='row'>";
            str += "<div class='col-md-1'>";
            str += "" + yearLink + " ";
            str += "</div>";
            str += "<div class='col-md-10'>";
            str+=createHtmlBar(count,maxCount);
            str += "</div>";
            str += "</div>";
        }


        return str;
    }

    public int getYearFromReleaseDate(String releaseDate) {
        // e.g. "2004-04-13"
        if (releaseDate == null || releaseDate.length() < 6) return -1;
        int year = Integer.parseInt(releaseDate.substring(0, 4));
        return year;
    }

    public String createHtmlBar(int count, int maxCount) {
        int maxSize = 100;
        int barLength = (int) ((double) maxSize * ((double) count) / (double) maxCount);
        if (barLength < 0) barLength = 0;
        if (barLength > 100) barLength = 100;

        String str = "<span class='progress  w-80'>";
        str += "<span class='progress-bar bg-info ' role='progressbar' style='width: " + barLength + "%' >"+count+"</span>";
        str += "</span>";
        return str;
    }
}
