package com.company;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Node extends Thread {
    private InetAddress nodeIPAddress;
    private String nodeName;
    private int nodePort;

    public Node(String name, InetAddress ip, int port) {
        nodeIPAddress = ip;
        nodeName = name;
        nodePort = port;
        System.out.println("New machine: name = " + getNodeName() + " ip = " + getNodeIPAddress().getHostAddress() + " port = " + getNodePort());
        start();
    }
    public InetAddress getNodeIPAddress(){ return nodeIPAddress; }
    public String getNodeName(){ return nodeName; }
    public int getNodePort(){ return nodePort; }

    public void sendMessageToNode(String message, ServerSystem sendingServer) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, getNodeIPAddress(), getNodePort());
            DatagramSocket socket = new DatagramSocket(sendingServer.getServerPort());
            socket.send(packet);
            socket.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
