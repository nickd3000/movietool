package com.physmo.movietool.domain;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class PageComposer {
    static final String BR = "<br>";
//changedFiles.put("removedFileSet", removedFileSet);
//changedFiles.put("newFileSet", newFileSet);

    public String scanLocalFilesForChanges(Map<String, Set<String>> stringSetMap) {
        String str = "";

        str += "<b>Files removed:</b>" + BR;
        if (stringSetMap.get("removedFileSet").size() == 0) str += BR + "none";
        for (String file : stringSetMap.get("removedFileSet")) {
            str += BR + file;
        }

        str += BR + BR + "<b>Files added:</b>" + BR;
        if (stringSetMap.get("newFileSet").size() == 0) str += BR + "none";
        for (String file : stringSetMap.get("newFileSet")) {
            str += BR + file;
        }

        return str;
    }

}
