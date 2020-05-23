package com.company;

public class Work extends Thread {
    private int duration;
    private int timeLeft;
    private boolean complete;
    private Node workerNode;

    public Work() {
        duration = 10;
        complete = false;
    }

    public Work(int inputDuration){
        try {
            duration = inputDuration;
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
