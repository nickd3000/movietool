package com.physmo.movietool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = Config.class)
@TestPropertySource("classpath:application.properties")
public class TestFileNameOperations {

    @Autowired
    Config config;

    @Test
    public void testConfig() {
        Assertions.assertTrue(Arrays.asList(config.getAllowedFileTypes()).contains("m4v"));
        Assertions.assertTrue(Arrays.asList(config.getAllowedFileTypes()).contains("avi"));
        Assertions.assertTrue(Arrays.asList(config.getAllowedFileTypes()).contains("mkv"));
        Assertions.assertTrue(Arrays.asList(config.getAllowedFileTypes()).contains("mp4"));
    }

    @Test
    public void testExtractFileNameParts() {
        FileNameOperations fileNameOperations = new FileNameOperations(config);

        String[] strings = fileNameOperations.extractFileNameParts("test (1990).mov");

        Assertions.assertEquals("test", strings[0]);
        Assertions.assertEquals("1990", strings[1]);
        Assertions.assertEquals("mov", strings[2]);

    }
}
