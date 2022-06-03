package com.physmo.movietool;

import com.physmo.movietool.domain.DataStore;
import com.physmo.movietool.domain.Genres;
import com.physmo.movietool.domain.Link;
import com.physmo.movietool.domain.PageComposer;
import com.physmo.movietool.jobsystem.JOB_TYPE;
import com.physmo.movietool.jobsystem.JobManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {
    static final String BR = "<br>";
    final Config config;
    final DataStore dataStore;
    final Reports reports;
    final Operations operations;
    final TMDBService tmdbService;
    final PageComposer pageComposer;

    final JobManager jobManager;

    public MainController(Config config, DataStore dataStore, TMDBService tmdbService, Operations operations, Reports reports, PageComposer pageComposer, JobManager jobManager) {
        this.config = config;
        this.dataStore = dataStore;
        System.out.println("Controller: datastore size=" + dataStore.getFileListEntryList().size());
        this.tmdbService = tmdbService;
        this.operations = operations;
        this.reports = reports;
        this.pageComposer = pageComposer;
        this.jobManager = jobManager;
    }

    // TODO: get general info to display here
    @GetMapping("/")
    public String index(Model model) {

        String summary = reports.getLibrarySummary();

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
        return "Ver: " + config.getBuildVersion();
    }

    public List<Link> buildSidePanelLinks() {
        List<Link> links = new ArrayList<>();
        links.add(new Link("File List", "/showfilelist"));
        links.add(new Link("Unmatched Files", "/showunmatchedfilelist"));
        links.add(new Link("Duplicates", "/showduplicates"));
        links.add(new Link("Missing Dates", "/showmissingdates"));
        links.add(new Link("Missing popular movies", "/findmissingpopularmovies/1999"));
        links.add(new Link("Collections Report", "/collectionsreport"));
        links.add(new Link("Movies Per Year", "/moviesperyear"));

        return links;
    }

    @GetMapping("/showfilelist")
    public String showFileList(Model model) {
        String fileListAsTable = reports.getFileList();

        model.addAttribute("title", "File List");
        model.addAttribute("content", fileListAsTable);
        attachSidePanelCommonData(model);

        return "main";
    }

    @GetMapping("/showunmatchedfilelist")
    public String showUnmatchedFileList(Model model) {
        String fileListAsTable = reports.getUnmatchedFileList();


        model.addAttribute("title", "Unmatched files");
        model.addAttribute("content", fileListAsTable);
        attachSidePanelCommonData(model);
        return "main";
    }

    @GetMapping("/showduplicates")
    public String showDuplicates(Model model) {
        String fileListAsTable = reports.findDuplicates();

        model.addAttribute("title", "Duplicated files");
        model.addAttribute("content", fileListAsTable);
        attachSidePanelCommonData(model);
        return "main";
    }

    @GetMapping("/showmissingdates")
    public String showMissingDates(Model model) {
        String str = reports.getMoviesWithNoDate();

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

        str += BR + reports.findMissingPopularMoviesForYear(Integer.parseInt(year));

        model.addAttribute("title", "Missing popular movies for " + year);
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);
        return "main";
    }

    public String makeLink(String text, String link) {
        return "<a href='" + link + "'>" + text + "</a>";
    }

    @GetMapping("/collectionsreport")
    public String getCollectionsReport(Model model) {
        String str = reports.getCollectionsReport();

        model.addAttribute("title", "Collections Report");
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);
        return "main";
    }

    @GetMapping("/movieinfo/{movieId}")
    public String getMovieInfo(Model model, @PathVariable String movieId) {
        String str = reports.getMovieInfo(Integer.parseInt(movieId));

        model.addAttribute("title", "Movie Details");
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);
        return "main";
    }

    @GetMapping("/moviesperyear")
    public String countMoviesPerYear(Model model) {
        String str = reports.countMoviesPerYear();

        model.addAttribute("title", "Movies Per Year");
        model.addAttribute("content", str);
        attachSidePanelCommonData(model);
        return "main";
    }

    @ResponseBody
    @GetMapping("/internalgetjobs")
    public String getJobs() {
        String str = jobManager.getJobsForDisplay();
        return str;
    }


    // Test creating testjob from button
    @ResponseBody
    @GetMapping("/testjob")
    public String testJob() {
        jobManager.addJob(JOB_TYPE.dummyJob);
        return "";
    }

    @ResponseBody
    @GetMapping("/action_retrievemovieinfo")
    public String actionRetrieveMovieInfo() {
        Genres genres = tmdbService.getGenres();
        System.out.println("LOADED GENRES "+genres.getGenres().length);
        dataStore.setGenres(genres);
        operations.saveDataStore(dataStore);

        jobManager.addJob(JOB_TYPE.retrieveMovieData);
        jobManager.addJob(JOB_TYPE.retrieveMovieInfo);
        jobManager.addJob(JOB_TYPE.retrieveCollectionsData);


        return "";
    }

    // Test creating testjob from button
    @ResponseBody
    @GetMapping("/action_scanmoviefolder")
    public String actionScanMovieFolder() {
        jobManager.addJob(JOB_TYPE.removeMissingFiles);
        jobManager.addJob(JOB_TYPE.scanMovieFolder);
        return "";
    }
}
