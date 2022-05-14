package com.physmo.movietool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.physmo.movietool.domain.DataStore;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@Component
public class DiskOperations {
    Config config;

    public DiskOperations(Config config) {
        this.config = config;
    }

    public File[] getAllFilesInDirectory(String path) {
        File f = new File(path);
        File[] files = f.listFiles();

        return files;
    }

    public void saveDataStore(String filePath, DataStore dataStore) throws IOException {
        File f = new File(filePath);
        System.out.println("Checking if file exists");
        if (!f.isFile()) {
            System.out.println("file doesnt exist creating..." + f.getCanonicalFile());
            f.createNewFile();
            System.out.println("created");
        }

        Writer writer = new FileWriter(filePath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(dataStore, writer);
        writer.flush(); //flush data to file   <---
        writer.close(); //close write          <---
    }

    public DataStore loadDataStore(String filePath) throws FileNotFoundException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        DataStore deserializedDataStore = gson.fromJson(new FileReader(filePath), DataStore.class);
        return deserializedDataStore;
    }


}
