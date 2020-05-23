package com.company;

public class Work extends Thread {
    private int workID;
    private int duration;
    private int timeLeft;
    private boolean complete;
    private Node workerNode;

    public int getWorkID() { return workID; }
    public int getDuration() { return duration; }
    public int getTimeLeft() { return timeLeft; }
    public boolean getComplete() { return complete; }
    public Node getWorkerNode() { return workerNode; }

    public void setTimeLeft(int inputTimeLeft) { timeLeft = inputTimeLeft; }
    public void setComplete(boolean inputComplete) { complete = inputComplete; }
    public void setWorkerNode(Node inputWorkerNode) { workerNode = inputWorkerNode; }

    public Work(int workID, int inputDuration) {
        try {
            this.workID = workID;
            duration = inputDuration;
            System.out.println("Work created! ID: " + workID + ", duration: " + duration + ".");
        } catch (Exception e) {
            System.out.println("Input not recognised: " + e.getMessage());
        }
    }

    public boolean assignWorkToNode(Node inputNode) {
        workerNode = inputNode;
        return true;
    }

    public boolean sendWorkToNode(Node inputNode) {
        return true;
    }

    @Override
    public void run() {

    }
}
