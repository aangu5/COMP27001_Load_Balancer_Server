package com.company;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

public class Node extends Thread {
    private InetAddress nodeIPAddress;
    private String nodeName;
    private int nodePort;
    private boolean working;
    private int maxJobs;
    private int currentJobs;

    public Node(String name, InetAddress ip, int port) {
        nodeIPAddress = ip;
        nodeName = name;
        nodePort = port;
        working = false;
        maxJobs = 10;

        System.out.println("New machine: name = " + getNodeName() + " ip = " + getNodeIPAddress().getHostAddress() + " port = " + getNodePort());
        start();
    }
    public InetAddress getNodeIPAddress(){ return nodeIPAddress; }
    public String getNodeName(){ return nodeName; }
    public int getNodePort(){ return nodePort; }
    public int getMaxJobs() { return maxJobs; }
    public int getCurrentJobs() { return currentJobs; }
    public int getCurrentUtilisation() {
        int utilisationPercent = (int) (currentJobs / maxJobs) * 100;
        if (utilisationPercent == 0) {
            return 0;
        } else if (utilisationPercent > 100) {
            return 100;
        }
        else { return utilisationPercent; }

    }
    public void setNodeIPAddress(InetAddress newIP) { nodeIPAddress = newIP; }
    public void setNodePort(int newPort) { nodePort = newPort; }
    public void setWorking(boolean working) { this.working = working; }

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
}
