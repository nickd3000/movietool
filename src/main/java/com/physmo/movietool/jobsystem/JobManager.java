package com.physmo.movietool.jobsystem;

import com.physmo.movietool.Operations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobManager {

    static int idFountain = 0;
    List<Job> activeJobList = new ArrayList<>();
    List<Job> pendingJobList = new ArrayList<>();
    List<Job> completedJobList = new ArrayList<>();
    Operations operations;

    public JobManager(Operations operations) {
        this.operations = operations;
        //addJob(JOB_TYPE.dummyJob);
    }

    public void addJob(JOB_TYPE jobType) {
        Job newJob = new Job();
        newJob.setId(idFountain++);
        newJob.setJobType(jobType);
        pendingJobList.add(newJob);
    }

    @Scheduled(fixedRate = 100)
    public void tick() {
        if (activeJobList.isEmpty()) {
            if (!pendingJobList.isEmpty()) {
                Job job = pendingJobList.get(0);
                startJob(job);
                pendingJobList.remove(job);
                activeJobList.add(job);
                job.setComplete(false);
            }
        }

        if (!activeJobList.isEmpty()) {
            Job job = activeJobList.get(0);
            if (job.isComplete()) {
                activeJobList.remove(job);
                completedJobList.add(job);
            }
        }

    }


    public void startJob(Job job) {
        switch (job.getJobType()) {
            case dummyJob:
                new Thread(() -> {
                    operations.startDummyJob(job);
                }).start();
                break;
            case removeMissingFiles:
                new Thread(() -> {
                    operations.startJobRemoveMissingFiles(job);
                }).start();
                break;
            case scanMovieFolder:
                new Thread(() -> {
                    operations.startJobScanMovieFolder(job);
                }).start();
                break;
            case retrieveMovieData:
                new Thread(() -> {
                    operations.startJobRetrieveMovieData(job);
                }).start();
                break;
            case retrieveMovieInfo:
                new Thread(() -> {
                    operations.startJobRetrieveMovieInfo(job);
                }).start();
                break;
            case retrieveCollectionsData:
                new Thread(() -> {
                    operations.startJobRetrieveCollectionData(job);
                }).start();
                break;
        }
    }

    public String getJobsForDisplay() {
        String str = "<hr>";

        for (Job job : activeJobList) {
            str += "<div class='alert alert-success p-1' role='alert'>";
            str += job.getJobType().getDisplayName();
            str += "<hr>";
            str += makeProgressBarHtml(job);
            str += "</div>";
        }

        for (Job job : pendingJobList) {
            str += "<div class='alert alert-info p-1' role='alert'>" + job.getJobType().getDisplayName() + "</div>";
        }

        int completedCount = 0;
        for (int i = completedJobList.size() - 1; i > 0; i--) {
            Job job = completedJobList.get(i);
            if (job == null && job.getResultText() == null) continue;
            str += "<br>" + job.getResultText();
            completedCount++;
            if (completedCount > 16) break;
        }
//        for (Job job : completedJobList) {
//            str += "<div class='alert alert-secondary p-1' role='alert'>"+job.getJobType().name()+"</div>";
//        }

        return str;
    }

    public String makeProgressBarHtml(Job job) {
        // <div class="progress">
        //     <div class='progress-bar bg-info' role='progressbar' style='width: 50%' aria-valuenow='50' aria-valuemin='0' aria-valuemax='100'></div>
        // </div>
        double percent = 0;
        String strPc = "0";
        if (job.getCurrentProgress() > 0 && job.getMaxProgress() > 0) {
            percent = (double) job.getCurrentProgress() / (double) job.getMaxProgress();
            percent *= 100;
            percent = percent > 100 ? 100 : percent;
            strPc = "" + (int) (percent);
        }

        String str = "<div class='progress'>";
        str += "<div class='progress-bar progress-bar-striped progress-bar-animated bg-info' role='progressbar' style='width: " + strPc + "%' ></div>";
        str += "</div>";
        return str;
    }
}
