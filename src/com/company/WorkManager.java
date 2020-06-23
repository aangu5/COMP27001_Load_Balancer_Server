package com.company;

import java.util.LinkedList;

public class WorkManager {
    /**
     * Three linked lists to represent all the work, the pending work and the work taking place currently
     */
    private LinkedList<Work> allWork = new LinkedList<>();
    private LinkedList<Work> pendingWork = new LinkedList<>();
    private LinkedList<Work> currentWork = new LinkedList<>();

    /**
     * Adds Work object to pending work and all work lists and writes the backlog length to the console
     * @param newWork - Work object to be added
     * @return - true or false based on whether adding the work to the lists was successful
     */
    public boolean addWork(Work newWork){
        boolean status = false;
        if (!pendingWork.contains(newWork) && !allWork.contains(newWork)) {
            status = pendingWork.add(newWork) && allWork.add(newWork);
            System.out.println("There is now " + getPendingWorkLength() + " seconds of work currently in the backlog.");
        }
        return status;
    }


    /**
     * @return - true or false based on whether work is available
     */
    public boolean isWorkAvailable() {
        return (!pendingWork.isEmpty());
    }

    /**
     * @return - boolean value based on whether work is in progress
     */
    public boolean isWorkInProgress() {
        System.out.println("Number of jobs in progress: " + currentWork.size());
        return (!currentWork.isEmpty());
    }

    /**
     * returns an available Work object
     * @return - Work object added to the pending work list or null if no work available
     */
    public Work getAvailableWork() {
        try {
            return (Work) pendingWork.getFirst();
        } catch (Exception e) {
            System.out.println("No work available!");
            return null;
        }
    }

    /**
     * starts a given Work task by removing the task from pending work and adding it to current work then starts the work object if it hasn't already started
     * @param startedWork - the Work object that has been started
     */
    public void startWork(Work startedWork) {
        if(!currentWork.contains(currentWork)) {
            currentWork.add(startedWork);
        }
        if (pendingWork.contains(startedWork)) {
            pendingWork.remove(startedWork);
        }
        if(!startedWork.isAlive()) {
            startedWork.start();
        }
    }

    /**
     * gets the length of work available in the backlog
     * @return - time in seconds of work in the backlog
     */
    public int getPendingWorkLength() {
        int pendingWorkLength = 0;
        Work currentWork = null;
        for (Object o : pendingWork) {
            currentWork = (Work) o;
            pendingWorkLength += currentWork.getDuration();
        }
        return pendingWorkLength;
    }

    /**
     * updates the Work objects stored in the lists with an updated version of itself
     * @param updatedWork - the updated Work object
     * @return - true or false whether the object was updated
     */
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

    /**
     * gets the next work ID by counting the number of work objects and adding one
     * @return - next work ID as an int
     */
    public int getNextWorkID() { return allWork.size() + 1; }

    /**
     * finds a Work object based on an input work ID
     * @param inputID - the work ID to find
     * @return - the Work object or null if not available
     */
    public Work findByID(int inputID) {
        Work tempWork;
        for (int i = 0; i < allWork.size(); i++) {
            tempWork = (Work) allWork.get(i);
            if (tempWork.getWorkID() == inputID) {
                return tempWork;
            }
        }
        return null;
    }

    /**
     * sets a work object as complete by removing it from the current work list
     * @param completedWork - the Work object that has been completed
     */
    public void workComplete(Work completedWork) {
        if (currentWork.remove(completedWork)) {
            System.out.println("Work removed from list");
        } else {
            System.out.println("Work not removed from list!");
        }
    }
}
