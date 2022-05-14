package com.physmo.movietool.domain;

import com.physmo.movietool.domain.movieinfo.MovieInfo;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;
    String nameTest = "name test";
    List<FileListEntry> fileListEntryList = new ArrayList<>();
    Map<Integer, Movie> movieMap = new HashMap<>();
    Map<Integer, MovieInfo> movieInfo = new HashMap<>();
    Map<Integer, MovieCollection> movieCollectionMap = new HashMap<>();

    public Map<Integer, MovieCollection> getMovieCollectionMap() {
        return movieCollectionMap;
    }

    public void setMovieCollectionMap(Map<Integer, MovieCollection> movieCollectionMap) {
        this.movieCollectionMap = movieCollectionMap;
    }

    public Map<Integer, MovieInfo> getMovieInfo() {
        return movieInfo;
    }

    public void setMovieInfo(Map<Integer, MovieInfo> movieInfo) {
        this.movieInfo = movieInfo;
    }

    public String getNameTest() {
        return nameTest;
    }

    public void setNameTest(String nameTest) {
        this.nameTest = nameTest;
    }

    public List<FileListEntry> getFileListEntryList() {
        return fileListEntryList;
    }

    public void setFileListEntryList(List<FileListEntry> fileListEntryList) {
        this.fileListEntryList = fileListEntryList;
    }

    public Map<Integer, Movie> getMovieMap() {
        return movieMap;
    }

    public void setMovieMap(Map<Integer, Movie> movieMap) {
        this.movieMap = movieMap;
    }

    public int countFilesWithNoId() {
        int count = 0;
        for (FileListEntry fileListEntry : fileListEntryList) {
            if (fileListEntry.getId() == 0) count++;
        }
        return count;
    }
}
