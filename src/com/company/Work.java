package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.TimeUnit;

public class Work extends Thread {
    private int workID;
    private int duration;
    private int timeLeft;
    private boolean complete;
    private Node workerNode;
    private double timeoutDouble;
    private int timeoutInt;
    private Server server;

    public int getWorkID() { return workID; }
    public int getDuration() { return duration; }
    public Node getWorkerNode() { return workerNode; }

    public void setComplete(boolean inputComplete) { complete = inputComplete; }
    public void setWorkerNode(Node inputWorkerNode) { workerNode = inputWorkerNode; }

    public Work(Server server, int workID, int duration) {
        try {
            this.server = server;
            this.workID = workID;
            this.duration = duration;
            timeoutDouble = duration + ((double) duration / 2);
            System.out.println("Work created! ID: " + workID + ", duration: " + duration + ".");
        } catch (Exception e) {
            System.out.println("Input not recognised: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            timeoutInt = (int)timeoutDouble;
            TimeUnit.SECONDS.sleep(timeoutInt);
            if (!complete) {
                System.out.println("Work not completed - potential error!");
                String message = "FAILEDWORK," + workID + "," + duration;
                DatagramPacket packet = new DatagramPacket(message.getBytes(),message.getBytes().length, server.getServerIP(),server.getServerPort());
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
                socket.close();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
