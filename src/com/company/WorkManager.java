package com.company;

import sun.awt.image.ImageWatched;

import java.util.LinkedList;

public class WorkManager {
    private LinkedList allWork = new LinkedList();
    private LinkedList pendingWork = new LinkedList();
    private LinkedList currentWork = new LinkedList();

    public boolean addNewWork(Work newWork){
        boolean status = pendingWork.add(newWork) && allWork.add(newWork);
        System.out.println("There is now " + getPendingWorkLength() + " seconds of work currently in the backlog.");
        return status;
    }

    public boolean workAvailable() {
        return (!pendingWork.isEmpty());
    }

    public Work getAvailableWork() {
        try {
            return (Work) pendingWork.getFirst();
        } catch (Exception e) {
            System.out.println("No work available!");
            return null;
        }
    }

    public void startWork(Work startedWork) {
        if (currentWork.add(startedWork)) {
            pendingWork.remove(startedWork);
        } else {
            System.out.println("Unable to start work!");
        }
    }

    public int getPendingWorkLength() {
        int pendingWorkLength = 0;
        Work currentWork = null;
        for (int i = 0; i < pendingWork.size(); i++){
            currentWork = (Work) pendingWork.get(i);
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
        for (int i = 0; i < allWork.size(); i++){
            tempWork = (Work) allWork.get(i);
            if (tempWork.getWorkID() == inputID) {
                return tempWork;
            }
        }
        return null;
    }

    public void workComplete(Work completedWork) {
        pendingWork.remove(completedWork);
    }
}
