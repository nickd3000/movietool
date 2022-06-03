package com.physmo.movietool.jobsystem;

public class Job {
    boolean complete = false;
    int currentProgress = 0;
    int maxProgress = 0;
    private int id;
    private JOB_TYPE jobType;
    private String resultText;

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JOB_TYPE getJobType() {
        return jobType;
    }

    public void setJobType(JOB_TYPE jobType) {
        this.jobType = jobType;
    }
}
