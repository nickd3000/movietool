package com.physmo.movietool;

import org.apache.commons.text.StringEscapeUtils;

public class HtmlHelpers {
    public static String createTooltipIconAsHtml(String tooltip) {
        String str = " <img src='/info.png' width='16' height='16' data-bs-toggle='tooltip' data-bs-placement='right' title='" + tooltip + "' >";
        return str;
    }
    public static String makeTwoColumnTableEntryAsHtml(String str1, String str2) {
        return "<tr><td>" + str1 + "</td><td>" + str2 + "</td></tr>";
    }

    public static String makeLinkAsHtml(String text, String link) {
        return "<a href='" + link + "'>" + text + "</a>";
    }
    public static int getYearFromReleaseDate(String releaseDate) {
        // e.g. "2004-04-13"
        if (releaseDate == null || releaseDate.length() < 6) return -1;
        int year = Integer.parseInt(releaseDate.substring(0, 4));
        return year;
    }

    public static String sanitizeText(String txt) {
        txt = txt.replace("'", "");
        return StringEscapeUtils.escapeHtml4(txt);
    }
    public static String createBarAsHtml(int count, int maxCount) {
        int maxSize = 100;
        int barLength = (int) ((double) maxSize * ((double) count) / (double) maxCount);
        if (barLength < 0) barLength = 0;
        if (barLength > 100) barLength = 100;

        String str = "<span class='progress  w-80'>";
        str += "<span class='progress-bar bg-info ' role='progressbar' style='width: " + barLength + "%' >" + count + "</span>";
        str += "</span>";
        return str;
    }
    public static String formatRatingAsHtml(double rating) {
        String str;
        String ratingClass = "text-rating-d";

        // not sure how I want the rating colors to work yet so let's do this in a crappy way for now.
        if (rating > 7.5) {
            ratingClass = "text-rating-a";
        } else if (rating > 5) {
            ratingClass = "text-rating-b";
        } else if (rating > 4) {
            ratingClass = "text-rating-c";
        }

        str = " <span class='" + ratingClass + "'>" + rating + "</span>";
        return str;
    }
}
