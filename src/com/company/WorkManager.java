package com.company;

import java.util.LinkedList;

public class WorkManager {
    private LinkedList allWork = new LinkedList();
    private LinkedList pendingWork = new LinkedList();
    private LinkedList currentWork = new LinkedList();

    public WorkManager() {

    }

    public boolean addWork(Work newWork){
        boolean status = false;
        if (!pendingWork.contains(newWork) && !allWork.contains(newWork)) {
            status = pendingWork.add(newWork) && allWork.add(newWork);
            System.out.println("There is now " + getPendingWorkLength() + " seconds of work currently in the backlog.");
        }
        return status;
    }

    public boolean isWorkAvailable() {
        return (!pendingWork.isEmpty());
    }

    public boolean isWorkInProgress() {
        System.out.println("Number of jobs in progress: " + currentWork.size());
        return (!currentWork.isEmpty()); }

    public Work getAvailableWork() {
        try {
            return (Work) pendingWork.getFirst();
        } catch (Exception e) {
            System.out.println("No work available!");
            return null;
        }
    }

    public void startWork(Work startedWork) {
        if(!currentWork.contains(currentWork)) {
            currentWork.add(startedWork);
        }
        if (pendingWork.contains(startedWork)) {
            pendingWork.remove(startedWork);
        }
        if(startedWork.isAlive()) {

        } else {
            startedWork.start();
        }
    }

    public int getPendingWorkLength() {
        int pendingWorkLength = 0;
        Work currentWork = null;
        for (Object o : pendingWork) {
            currentWork = (Work) o;
            pendingWorkLength += currentWork.getDuration();
        }
        return pendingWorkLength;
    }

    public boolean updateWork(Work updatedWork) {
        for (int i = 0; i < allWork.size(); i++) {
            Work listWork = (Work) allWork.get(i);
            if (listWork.getWorkID() == updatedWork.getWorkID()) {
                allWork.set(i, updatedWork);
                return true;
            }
        }
        for (int i = 0; i < pendingWork.size(); i++) {
            Work listWork = (Work) allWork.get(i);
            if (listWork.getWorkID() == updatedWork.getWorkID()) {
                allWork.set(i, updatedWork);
                return true;
            }
        }
        for (int i = 0; i < currentWork.size(); i++) {
            Work listWork = (Work) allWork.get(i);
            if (listWork.getWorkID() == updatedWork.getWorkID()) {
                allWork.set(i, updatedWork);
                return true;
            }
        }
        return false;
    }
    public int getNextWorkID() { return allWork.size() + 1; }

    public Work findByID(int inputID) {
        Work tempWork;
        for (Object o : allWork) {
            tempWork = (Work) o;
            if (tempWork.getWorkID() == inputID) {
                return tempWork;
            }
        }
        return null;
    }

    public void workComplete(Work completedWork) {
        if (currentWork.remove(completedWork)) {
            System.out.println("Work removed from list");
        } else {
            System.out.println("Work not removed from list!");
        }
    }
}
