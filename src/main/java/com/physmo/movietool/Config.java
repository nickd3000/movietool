package com.physmo.movietool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("movietool")
public class Config {
    @Value("${movietool.allowed-file-types}")
    private String[] allowedFileTypes;
    @Value("${movietool.data-file-path}")
    private String dataFilePath;
    @Value("${movietool.data-file-name}")
    private String dataFileName;
    @Value("${movietool.movie-folder-path}")
    private String movieFolderPath;
    @Value("${application.name}")
    private String applicationName;
    @Value("${build.version}")
    private String buildVersion;
    @Value("${build.timestamp}")
    private String buildTimestamp;
    @Value("${movietool.tmdb-api-key}")
    private String tmdbApiKey;

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public String getTmdbApiKey() {
        return tmdbApiKey;
    }

    public void setTmdbApiKey(String tmdbApiKey) {
        this.tmdbApiKey = tmdbApiKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    public void setBuildTimestamp(String buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    public String getMovieFolderPath() {
        return movieFolderPath;
    }

    public void setMovieFolderPath(String movieFolderPath) {
        this.movieFolderPath = movieFolderPath;
    }

    public String getDataFilePath() {
        return dataFilePath;
    }

    public void setDataFilePath(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }

    public String[] getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public void setAllowedFileTypes(String[] allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }


}
