package com.physmo.movietool;

import com.physmo.movietool.domain.DataStore;
import com.physmo.movietool.domain.FileListEntry;
import com.physmo.movietool.domain.Movie;
import com.physmo.movietool.domain.movieinfo.MovieInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Operations {

    private static final Logger log = LoggerFactory.getLogger(Operations.class);

    final Config config;
    final DiskOperations diskOperations;
    final TMDBService tmdbService;
    final FileNameOperations fileNameOperations;

    public Operations(Config config, DiskOperations diskOperations, TMDBService tmdbService, FileNameOperations fileNameOperations) {
        this.config = config;
        this.diskOperations = diskOperations;
        this.tmdbService = tmdbService;
        this.fileNameOperations = fileNameOperations;
    }

    // check for known files that no longer exist
    // find new files
    public Map<String, Set<String>> refreshFileList(DataStore dataStore, String filePath) {
        log.info("Refreshing file list - checking for new and changed files");
        Set<String> removedFileSet = removeMissingFiles(dataStore, filePath);
        Set<String> newFileSet = retrieveFileList(dataStore, filePath);
        Map<String, Set<String>> changedFiles = new HashMap<>();
        changedFiles.put("removedFileSet", removedFileSet);
        changedFiles.put("newFileSet", newFileSet);
        log.info("DONE");
        return changedFiles;
    }

    public Set<String> retrieveFileList(DataStore dataStore, String filePath) {

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

    public void retrieveTMDBDataForFileList(DataStore dataStore, boolean override) {

        int count = 0;

        List<FileListEntry> fileListEntryList = dataStore.getFileListEntryList();
        for (FileListEntry fileListEntry : fileListEntryList) {
            if (fileListEntry.getId() != 0 && !override) continue;

            Movie movie = tmdbService.retrieveTMDBDataForFile(fileListEntry.getNamePart(), fileListEntry.getDatePart());
            if (movie == null) {
                log.info("Movie not found on TMDB:" + fileListEntry.getNamePart());
                continue;
            }
            log.info("Found :" + fileListEntry.getNamePart() + " as " + movie.getTitle());

            int movieId = movie.getId();
            dataStore.getMovieMap().put(movieId, movie);
            fileListEntry.setId(movieId);

            count++;
            int MAX_RETRIEVALS_PER_RUN = 1000;
            if (count > MAX_RETRIEVALS_PER_RUN) break;
        }

    }

    public List<String> retrieveTMDBMovieInfo(DataStore dataStore) {

        int count = 0;
        List<String> output = new ArrayList<>();

        for (Integer movieId : dataStore.getMovieMap().keySet()) {
            if (dataStore.getMovieInfo().containsKey(movieId)) continue;

            String name = dataStore.getMovieMap().get(movieId).getTitle();

            MovieInfo movieInfo = tmdbService.getMovieInfo(movieId);

            if (movieInfo == null) {
                output.add("Movie Info not found for id " + movieId + ", " + name);
                continue;
            } else {
                output.add("Found Movie Info for id " + movieId + ", " + name);
            }

            dataStore.getMovieInfo().put(movieId, movieInfo);

            count++;
            if (count > 1000) break;
        }
        return output;
    }

    public void loadDataStore(DataStore dataStore) {

        String path = config.getDataFilePath();

        //DataStore dataStore;

        try {
            diskOperations.loadDataStore(path, dataStore);
            //System.out.println("Loaded data store");
        } catch (FileNotFoundException e) {
            //System.out.println("Data store not found, creating new.");
            dataStore = new DataStore();
            saveDataStore(dataStore);
        }


        //return dataStore;
    }

    public void saveDataStore(DataStore dataStore) {
        String path = config.getDataFilePath();

        try {
            diskOperations.saveDataStore(path, dataStore);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Return list of movie collections that we have at least one movie in.
    public void retrieveMovieCollections(DataStore dataStore) {
        Set<Integer> collectionSet = new HashSet<>();
        log.info("Retrieving movie collection data from MDB");
        // Get list of movie collection id's.
        for (FileListEntry fileListEntry : dataStore.getFileListEntryList()) {
            if (fileListEntry.getId() == 0) continue; // ID of 0 means we haven't found this movie.

            MovieInfo movieInfo = dataStore.getMovieInfo().get(fileListEntry.getId());

            if (movieInfo == null) continue;
            if (movieInfo.getBelongs_to_collection() == null) continue;

            Integer collectionId = movieInfo.getBelongs_to_collection().getId();
            collectionSet.add(collectionId);
        }

        // Retrieve any we haven't stored already.
        for (Integer collectionId : collectionSet) {
            if (dataStore.getMovieCollectionMap().containsKey(collectionId)) continue;
            dataStore.getMovieCollectionMap().put(collectionId, tmdbService.getMovieCollection(collectionId));

        }
        log.info("DONE");
    }
}
