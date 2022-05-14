package com.physmo.movietool.domain;

public class CollectionsReportMovie {
    private String name;
    private String date;
    private boolean owned;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void set(String name, String date, boolean owned, int id) {
        this.name = name;
        this.date = date;
        this.owned = owned;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isOwned() {
        return owned;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }
}
