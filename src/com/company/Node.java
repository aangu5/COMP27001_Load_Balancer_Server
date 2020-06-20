package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Node extends Thread {
    private InetAddress nodeIPAddress;
    private int nodeID;
    private int nodePort;
    private boolean working;
    private int maxJobs;
    private int currentJobs;
    private long lastCheckIn;
    private boolean nodeOnline;
    private Server server;
    private final int timeout = 60;

    public Node(Server inputServer, int nodeID, InetAddress nodeIPAddress, int nodePort, int maxJobs) {
        this.nodeIPAddress = nodeIPAddress;
        this.nodeID = nodeID;
        this.nodePort = nodePort;
        this.maxJobs = maxJobs;
        working = false;
        lastCheckIn = Instant.now().getEpochSecond();
        nodeOnline = true;
        server = inputServer;
        System.out.println("New machine - ID: " + nodeID + " IP: " + getNodeIPAddress().getHostAddress() + " PORT: " + getNodePort() + " job limit: " + getMaxJobs() );
        start();
    }
    public InetAddress getNodeIPAddress(){ return nodeIPAddress; }
    public int getNodeID(){ return nodeID; }
    public int getNodePort(){ return nodePort; }
    public int getMaxJobs() { return maxJobs; }
    public double getCurrentUtilisation() {
        double utilisationPercent =  ((double)currentJobs /(double) maxJobs) * 100;
        return utilisationPercent;
    }
    public void setNodeWorkingState(boolean working) { this.working = working; }
    public void newJob() { currentJobs += 1;}
    public void jobComplete() { currentJobs -= 1; }
    public void checkNodeIn() { lastCheckIn = Instant.now().getEpochSecond(); }

    public void sendMessageToNode(String message) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, getNodeIPAddress(), getNodePort());
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (nodeOnline) {
            System.out.println("Node check in status is: " + lastCheckIn);
                if (lastCheckIn < (Instant.now().getEpochSecond() - (timeout + timeout / 2))) {
                    System.out.println("Looks like we have a node that isn't responding! Attempting to shutdown the node....");
                    sendMessageToNode("SHUTDOWN");
                    nodeOnline = false;
                    System.out.println("Node not responding - potential error!");
                    String message = "DEADNODE," + nodeIPAddress.getHostAddress() + "," + nodePort;
                    DatagramPacket packet = new DatagramPacket(message.getBytes(),message.getBytes().length, server.getServerIP(),server.getServerPort());
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket();
                        socket.send(packet);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(timeout);
                        sendMessageToNode("STATUSCHECK");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
}
