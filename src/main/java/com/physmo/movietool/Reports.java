package com.physmo.movietool;

import com.physmo.movietool.domain.DataStore;
import com.physmo.movietool.domain.FileListEntry;
import com.physmo.movietool.domain.Movie;
import com.physmo.movietool.domain.MovieCollection;
import com.physmo.movietool.domain.movieinfo.CollectionDetails;
import com.physmo.movietool.domain.movieinfo.MovieInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Reports {
    static String BR = "<br>";
    static String OWNED = "**OWNED**";

    Operations operations;
    TMDBService tmdbService;

    Config config;

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
        String str = "<tr><td>" + str1 + "</td><td>" + str2 + "</td></tr>";
        return str;
    }

    public String getLibrarySummary(DataStore dataStore) {
        String ret = "";
        int numLocalFiles = dataStore.getFileListEntryList().size();
        ret += BR + "Local files: " + numLocalFiles;
        ret += BR + "Unmatched files: " + dataStore.countFilesWithNoId();
        ret += BR + "MOVIETOOL_MOVIEFOLDERPATH: "+ config.getMovieFolderPath();
        ret += BR + "MOVIETOOL_TMDBAPIKEY: "+ config.getTmdbApiKey();
        ret += BR + "MOVIETOOL_DATAFILEPATH: "+ config.getDataFilePath();

        String fileTypes="";
        for (String allowedFileType : config.getAllowedFileTypes()) {
            fileTypes+=allowedFileType+",";
        }

        ret += BR + "MOVIETOOL_ALLOWEDFILETYPES: "+ fileTypes;


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
                String linkedTitle = makeLink(movie.getTitle(), "/movieinfo/" + movie.getId());
                output += "<td>" + linkedTitle + "</td>";
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
        String str = "<a href='" + link + "'>" + text + "</a>";
        return str;
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
        Map<Integer, Integer> counts = new HashMap();
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
            str += BR;
            for (FileListEntry file : dataStore.getFileListEntryList()) {
                if (file.getId() == movieId) {
                    str += BR + file.getFileName();
                }
            }
        }

        return str;

    }

    public String findMissingPopularMoviesForYear(DataStore dataStore, int year) {
        //TMDBService tmdbService = new TMDBService();
        Map<Integer, Movie> popularMoviesByYear = tmdbService.getPopularMoviesByYear(year);
        String str = "";
        List<String> ownedList = new ArrayList<>();
        List<String> needList = new ArrayList<>();
        for (Integer movieId : popularMoviesByYear.keySet()) {
            Movie movie = popularMoviesByYear.get(movieId);
            boolean owned = false;
            for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
                if (fileListEntry.getId() == movieId) {
                    owned = true;
                }
            }

            if (owned)
                ownedList.add(OWNED + " " + movie.getTitle());
            else
                needList.add(movie.getTitle());
        }

        for (String s : ownedList) {
            str += BR + s;
        }
        str += BR;
        for (String s : needList) {
            str += BR + s;
        }


        return str;
    }

    public String getCollectionsReport(DataStore dataStore) {
        String str = "";


        // Retrieve any collections we haven't yet received from TMDB.
        operations.retrieveMovieCollections(dataStore);
        operations.saveDataStore(dataStore);

        for (MovieCollection movieCollection : dataStore.getMovieCollectionMap().values()) {
            str += BR + "<b>" + movieCollection.getName() + "</b>";
            for (Movie movie : movieCollection.getParts()) {
                String title = movie.getTitle();
                String date = movie.getRelease_date();

                boolean owned = false;
                for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
                    if (fileListEntry.getId() == movie.getId()) {
                        owned = true;
                    }
                }

                String linkedTitle = makeLink(title, "/movieinfo/" + movie.getId());

                if (owned)
                    str += BR + OWNED + " " + title + " (" + date + ")";
                else
                    str += BR + title + " (" + date + ")";
            }
            str += BR;
        }

        return str;
    }
}
