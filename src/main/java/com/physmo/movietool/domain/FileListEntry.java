package com.physmo.movietool.domain;

import java.io.Serializable;

public class FileListEntry implements Serializable {
    String path = "";
    String fileName = "";
    String namePart;
    String datePart;
    String extensionPart;
    int id;

    public FileListEntry(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getNamePart() {
        return namePart;
    }

    public void setNamePart(String namePart) {
        this.namePart = namePart;
    }

    public String getDatePart() {
        return datePart;
    }

    public void setDatePart(String datePart) {
        this.datePart = datePart;
    }

    public String getExtensionPart() {
        return extensionPart;
    }

    public void setExtensionPart(String extensionPart) {
        this.extensionPart = extensionPart;
    }

}
