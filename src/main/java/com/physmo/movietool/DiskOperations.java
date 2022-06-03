package com.physmo.movietool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.physmo.movietool.domain.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@Component
public class DiskOperations {
    private static final Logger log = LoggerFactory.getLogger(DiskOperations.class);

    final Config config;

    public DiskOperations(Config config) {
        this.config = config;
    }

    public File[] getAllFilesInDirectory(String path) {
        File f = new File(path);

        if (!f.exists()) {
            System.out.println("!!!! Path does not exist !!!! " + path);
        }

        return f.listFiles();
    }

    public void saveDataStore(String filePath, DataStore dataStore) throws IOException {
        log.info("Saving data store.");
        File f = new File(filePath);

        log.info("Checking if file exists");

        if (!f.isFile()) {
            log.info("file doesnt exist creating..." + f.getCanonicalFile());
            f.createNewFile();
            log.info("created data store file");
        }

        Writer writer = new FileWriter(filePath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(dataStore, writer);
        writer.flush(); //flush data to file   <---
        writer.close(); //close write          <---

        log.info("Saved data store.");
    }

    public void loadDataStore(String filePath, DataStore dataStore) throws FileNotFoundException {
        log.info("Loading data store.");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        DataStore deserializedDataStore = gson.fromJson(new FileReader(filePath), DataStore.class);

        dataStore.setFileListEntryList(deserializedDataStore.getFileListEntryList());
        dataStore.setMovieCollectionMap(deserializedDataStore.getMovieCollectionMap());
        dataStore.setMovieInfo(deserializedDataStore.getMovieInfo());
        dataStore.setMovieMap(deserializedDataStore.getMovieMap());
        dataStore.setGenres(deserializedDataStore.getGenres());
        dataStore.setGenreMap(deserializedDataStore.getGenreMap());
    }


}
