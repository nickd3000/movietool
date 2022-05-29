package com.physmo.movietool.jobsystem;

public enum JOB_TYPE {
    retrieveMovieData("Fetch Movie Data"),
    retrieveMovieInfo("Fetch Movie Info"),
    retrieveCollectionsData("Fetch collections"),
    dummyJob("Dummy job"),
    removeMissingFiles("Update Changed Files"),
    scanMovieFolder("Scan Movie Folder");

    String displayName;

    JOB_TYPE(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
