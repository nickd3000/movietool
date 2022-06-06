package com.physmo.movietool;

import com.physmo.movietool.domain.DataStore;
import com.physmo.movietool.domain.FileListEntry;
import com.physmo.movietool.domain.Movie;
import com.physmo.movietool.domain.movieinfo.MovieInfo;
import com.physmo.movietool.jobsystem.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class Operations {

    private static final Logger log = LoggerFactory.getLogger(Operations.class);

    final Config config;
    final DiskOperations diskOperations;
    final TMDBService tmdbService;
    final FileNameOperations fileNameOperations;

    final DataStore dataStore;

    public Operations(Config config, DiskOperations diskOperations, TMDBService tmdbService, FileNameOperations fileNameOperations, DataStore dataStore) {
        this.config = config;
        this.diskOperations = diskOperations;
        this.tmdbService = tmdbService;
        this.fileNameOperations = fileNameOperations;
        this.dataStore = dataStore;
    }

    public void loadDataStore(DataStore dataStore) {

        String path = config.getDataFilePath() + File.separatorChar + config.getDataFileName();

        try {
            diskOperations.loadDataStore(path, dataStore);
        } catch (FileNotFoundException e) {
            dataStore = new DataStore();
            saveDataStore(dataStore);
        }

    }

    public void saveDataStore(DataStore dataStore) {
        String path = config.getDataFilePath() + File.separatorChar + config.getDataFileName();

        try {
            diskOperations.saveDataStore(path, dataStore);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startDummyJob(Job job) {
        System.out.println("startDummyJob");
        int numIterations = 20;
        job.setMaxProgress(numIterations);
        for (int i = 0; i < numIterations; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            job.setCurrentProgress(i);
        }
        job.setComplete(true);
        job.setResultText("Completed dummy job");
    }

    public void startJobRemoveMissingFiles(Job job) {
        job.setMaxProgress(100);
        job.setCurrentProgress(50);
        Set<String> removedFileSet = removeMissingFiles(dataStore, config.getMovieFolderPath());
        job.setComplete(true);
        job.setResultText("Removed " + removedFileSet.size() + " missing files");
    }

    // Check each file in the file list, if it no longer exists remove it from the list.
    public Set<String> removeMissingFiles(DataStore dataStore, String filePath) {
        Set<String> removedFilesSet = new HashSet<>();

        List<FileListEntry> removeList = new ArrayList<>();
        for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
            String fileName = fileListEntry.getFileName();
            File f = new File(filePath + File.separator + fileName);
            if (!f.isFile()) {
                removeList.add(fileListEntry);
                removedFilesSet.add(fileName);
            }
        }
        log.info("Removing " + removeList.size() + " file entries");
        dataStore.getFileListEntryList().removeAll(removeList);

        return removedFilesSet;
    }

    public void startJobScanMovieFolder(Job job) {
        job.setMaxProgress(100);
        job.setCurrentProgress(50);
        Set<String> newFileSet = scanMovieFolder(dataStore, config.getMovieFolderPath());
        job.setComplete(true);
        job.setResultText("Discovered " + newFileSet.size() + " new files");
    }

    public Set<String> scanMovieFolder(DataStore dataStore, String filePath) {

        log.info("Movie folder: " + filePath);

        Set<String> newFilesSet = new HashSet<>();
        File[] allFilesInDirectory = diskOperations.getAllFilesInDirectory(filePath);

        for (File file : allFilesInDirectory) {
            FileListEntry fileListEntry = new FileListEntry(file.getPath(), file.getName());
            if (doesFileListEntryExist(fileListEntry, dataStore)) continue;
            if (!fileNameOperations.isMovieFileType(file.getName())) continue;

            String[] parts = fileNameOperations.splitFileName(file.getName());
            fileListEntry.setNamePart(parts[0]);
            fileListEntry.setDatePart(parts[1]);
            fileListEntry.setExtensionPart(parts[2]);

            dataStore.getFileListEntryList().add(fileListEntry);
            newFilesSet.add(file.getName());
        }

        return newFilesSet;
    }

    public boolean doesFileListEntryExist(FileListEntry fileListEntry, DataStore dataStore) {
        for (FileListEntry listEntry : dataStore.getFileListEntryList()) {
            if (listEntry.getPath().equals(fileListEntry.getPath())) {
                if (listEntry.getFileName().equals(fileListEntry.getFileName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void startJobRetrieveMovieData(Job job) {

        int count = 0;
        int retrievalCount = 0;

        List<FileListEntry> fileListEntryList = dataStore.getFileListEntryList();
        job.setMaxProgress(fileListEntryList.size());

        for (FileListEntry fileListEntry : fileListEntryList) {
            count++;
            job.setCurrentProgress(count);

            if (fileListEntry.getId() != 0) continue;


            Movie movie = tmdbService.retrieveTMDBDataForFile(fileListEntry.getNamePart(), fileListEntry.getDatePart());
            if (movie == null) {
                log.info("Movie not found on TMDB:" + fileListEntry.getNamePart());
                continue;
            }
            retrievalCount++;
            log.info("Found :" + fileListEntry.getNamePart() + " as " + movie.getTitle());

            int movieId = movie.getId();
            dataStore.getMovieMap().put(movieId, movie);
            fileListEntry.setId(movieId);

            if (retrievalCount % 10 == 0)
                saveDataStore(dataStore);
        }
        saveDataStore(dataStore);
        job.setComplete(true);
        job.setResultText("Retrieved " + retrievalCount + " movie data");
    }

    public void startJobRetrieveMovieInfo(Job job) {

        int count = 0;
        int retrievalCount = 0;
        List<String> output = new ArrayList<>();

        job.setMaxProgress(dataStore.getMovieMap().keySet().size());

        for (Integer movieId : dataStore.getMovieMap().keySet()) {
            count++;
            job.setCurrentProgress(count);
            if (dataStore.getMovieInfo().containsKey(movieId)) continue;

            String name = dataStore.getMovieMap().get(movieId).getTitle();

            MovieInfo movieInfo = tmdbService.getMovieInfo(movieId);

            if (movieInfo == null) {
                output.add("Movie Info not found for id " + movieId + ", " + name);
                continue;
            } else {
                output.add("Found Movie Info for id " + movieId + ", " + name);
            }
            retrievalCount++;
            dataStore.getMovieInfo().put(movieId, movieInfo);

            if (retrievalCount % 10 == 0)
                saveDataStore(dataStore);
        }

        saveDataStore(dataStore);
        job.setComplete(true);
        job.setResultText("Retrieved " + retrievalCount + " movie info");
    }

    public void startJobRetrieveCollectionData(Job job) {

        Set<Integer> collectionSet = new HashSet<>();
        log.info("Retrieving movie collection data from MDB");

        int count = 0;

        // Get list of movie collection IDs.
        for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {

            if (fileListEntry.getId() == 0) continue; // ID of 0 means we haven't found this movie.

            MovieInfo movieInfo = dataStore.getMovieInfo().get(fileListEntry.getId());

            if (movieInfo == null) continue;
            if (movieInfo.getBelongs_to_collection() == null) continue;

            Integer collectionId = movieInfo.getBelongs_to_collection().getId();
            collectionSet.add(collectionId);
        }

        job.setMaxProgress(collectionSet.size());

        // Retrieve any we haven't stored already.
        for (Integer collectionId : collectionSet) {

            job.setCurrentProgress(count);
            if (dataStore.getMovieCollectionMap().containsKey(collectionId)) continue;
            count++;
            dataStore.getMovieCollectionMap().put(collectionId, tmdbService.getMovieCollection(collectionId));
            if (count % 10 == 0)
                saveDataStore(dataStore);
        }
        log.info("DONE");

        saveDataStore(dataStore);
        job.setComplete(true);
        job.setResultText("Retrieved " + count + " collection data");
    }
}
