package com.physmo.movietool;

import org.springframework.stereotype.Component;

@Component
public class FileNameOperations {

    final Config config;

    public FileNameOperations(Config config) {
        this.config = config;
    }

    public boolean isMovieFileType(String name) {
        for (String type : config.getAllowedFileTypes()) {
            if (name.toLowerCase().contains(type)) return true;
        }
        return false;
    }

    public String[] splitFileName(String fileName) {
        String extension = "";
        String date = "";
        String name = "";

        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot != -1) {
            extension = fileName.substring(lastIndexOfDot + 1);
        }

        int lastIndexOfOpen = fileName.lastIndexOf('(');
        int lastIndexOfClose = fileName.lastIndexOf(')');

        if (lastIndexOfClose != -1) {
            date = fileName.substring(lastIndexOfOpen + 1, lastIndexOfClose);
        }

        if (lastIndexOfOpen != -1) {
            name = fileName.substring(0, lastIndexOfOpen);
            name = name.trim();
        } else if (lastIndexOfDot != -1) {
            name = fileName.substring(0, lastIndexOfDot);
            name = name.trim();
        }

        return new String[]{name, date, extension};
    }

}
