package com.physmo.movietool;

import org.springframework.stereotype.Component;

@Component
public class FileNameOperations {

    Config config;

    public FileNameOperations(Config config) {
        this.config = config;
    }

    public boolean isMovieFileType(String name) {
        //String[] allowedFileTypes = {"m4v", "avi", "mkv", "mp4"};
        for (String type : config.getAllowedFileTypes()) {
            if (name.toLowerCase().contains(type)) return true;
        }
        return false;
    }

    public String[] extractFileNameParts(String fileName) {
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

        //System.out.println(fileName + " : [" + name + "][" + extension + "][" + date + "]");

        String[] result = new String[3];
        result[0] = name;
        result[1] = date;
        result[2] = extension;

        return result;
    }

}
