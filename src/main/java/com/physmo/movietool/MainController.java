package com.physmo.movietool;

import com.physmo.movietool.domain.DataStore;
import com.physmo.movietool.domain.Link;
import com.physmo.movietool.domain.PageComposer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class MainController {
    static String BR = "<br>";

    Config config;
    DataStore dataStore;
    Reports reports;
    Operations operations;
    TMDBService tmdbService;

    PageComposer pageComposer;

    public MainController(Config config, DataStore dataStore, TMDBService tmdbService, Operations operations, Reports reports, PageComposer pageComposer) {
        this.config = config;
        this.dataStore = dataStore;
        System.out.println("Controller: datastore size=" + dataStore.getFileListEntryList().size());
        this.tmdbService = tmdbService;
        this.operations = operations;
        this.reports = reports;
        this.pageComposer = pageComposer;
    }

    // TODO: get general info to display here
    @GetMapping("/")
    public String index(Model model) {

        String summary = reports.getLibrarySummary(dataStore);

        model.addAttribute("title", "Main");
        model.addAttribute("content", summary);
        attachSidePanelCommonData(model);

        return "main";
    }

    public void attachSidePanelCommonData(Model model) {
        model.addAttribute("sidePanelLinks", buildSidePanelLinks());
        model.addAttribute("sidePanelInfo", sidePanelInfo());
    }

    public String sidePanelInfo() {
        return config.getBuildVersion();
    }

    public List<Link> buildSidePanelLinks() {
        List<Link> links = new ArrayList<>();

        links.add(new Link("Load Data Store", "/loaddatastore"));
        links.add(new Link("Scan for file changes", "/scanForChanges"));
        links.add(new Link("Retrieve Movie INFO From TMDB", "/retrievemovieinfo"));
        links.add(new Link("Show File List", "/showfilelist"));
        links.add(new Link("Show Unmatched File List", "/showunmatchedfilelist"));
        links.add(new Link("Show Duplicates", "/showduplicates"));
        links.add(new Link("Show Missing Dates", "/showmissingdates"));
        links.add(new Link("Find missing popular movies", "/findmissingpopularmovies/1999"));
        links.add(new Link("Collections Report", "/collectionsreport"));
        return links;
    }

    @GetMapping("/scanForChanges")
    public String scanLocalFilesForChanges(Model model) {

        Map<String, Set<String>> stringSetMap = operations.refreshFileList(dataStore, config.getMovieFolderPath());
        operations.saveDataStore(dataStore);

        String str = "";

        str += BR + "Movie folder: " + config.getMovieFolderPath();
        str += BR + pageComposer.scanLocalFilesForChanges(stringSetMap);

        model.addAttribute("title", "Scan For file changes");
        model.addAttribute("content", str);

        attachSidePanelCommonData(model);

        return "main";
    }

    @GetMapping("/retrievemovieinfo")
    public String retrieveMovieInfo(Model model) {

        operations.retrieveTMDBDataForFileList(dataStore, false);
        List<String> output = operations.retrieveTMDBMovieInfo(dataStore);


        operations.saveDataStore(dataStore);

        String str = "";
        if (output.size() == 0) {
            str = "Everything up-to-date";
        } else {
            for (String s : output) {
                str += BR + s;
            }
        }

        model.addAttribute("title", "Retrieve Movie Info");
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);

        return "main";
    }

    // TODO get some info about what was loaded
    @GetMapping("/loaddatastore")
    public String loadDataStore(Model model) {

        dataStore = operations.loadDataStore();

        String str = "";
        str += "Number of files: " + dataStore.getFileListEntryList().size();
        str += BR + "Movie info count: " + dataStore.getMovieMap().keySet().size();

        model.addAttribute("title", "Load Datastore");
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);

        return "main";
    }

    @GetMapping("/showfilelist")
    public String showFileList(Model model) {
        String fileListAsTable = reports.getFileList(dataStore);

        model.addAttribute("title", "File List");
        model.addAttribute("content", fileListAsTable);
        attachSidePanelCommonData(model);

        return "main";
    }

    @GetMapping("/showunmatchedfilelist")
    public String showUnmatchedFileList(Model model) {
        String fileListAsTable = reports.getUnmatchedFileList(dataStore);


        model.addAttribute("title", "Unmatched files");
        model.addAttribute("content", fileListAsTable);
        attachSidePanelCommonData(model);
        return "main";
    }

    @GetMapping("/showduplicates")
    public String showDuplicates(Model model) {
        String fileListAsTable = reports.findDuplicates(dataStore);

        model.addAttribute("title", "Duplicated files");
        model.addAttribute("content", fileListAsTable);
        attachSidePanelCommonData(model);
        return "main";
    }

    @GetMapping("/showmissingdates")
    public String showMissingDates(Model model) {
        String str = reports.getMoviesWithNoDate(dataStore);

        model.addAttribute("title", "Files with missing dates");
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);
        return "main";
    }

    @GetMapping("/findmissingpopularmovies/{year}")
    public String findMissingPopularMovies(Model model, @PathVariable String year) {
        String str = "";

        int iYear = Integer.parseInt(year);
        String prev = makeLink("" + (iYear - 1), "/findmissingpopularmovies/" + (iYear - 1));
        String next = makeLink("" + (iYear + 1), "/findmissingpopularmovies/" + (iYear + 1));

        str += prev + " " + next;

        str += BR + reports.findMissingPopularMoviesForYear(dataStore, Integer.parseInt(year));

        model.addAttribute("title", "Missing popular movies for " + year);
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);
        return "main";
    }

    public String makeLink(String text, String link) {
        String str = "<a href='" + link + "'>" + text + "</a>";
        return str;
    }

    @GetMapping("/collectionsreport")
    public String getCollectionsReport(Model model) {
        String str = reports.getCollectionsReport(dataStore);

        model.addAttribute("title", "Collections Report");
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);
        return "main";
    }

    @GetMapping("/movieinfo/{movieId}")
    public String getMovieInfo(Model model, @PathVariable String movieId) {
        String str = reports.getMovieInfo(dataStore, Integer.parseInt(movieId));

        model.addAttribute("title", "Movie Details");
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);
        return "main";
    }

}
