package com.company;

import java.io.IOException;
import java.net.*;

public class ServerSystem {
    private NodeManager manager = new NodeManager();

    private int serverPort;

    public ServerSystem(int port) {
        serverPort = port;
    }

    public int getServerPort() {
        return serverPort;
    }

    private void shutdown() {
        System.out.println("Turning off");
        System.exit(0);
    }

    private Node createNewNode(InetAddress nodeIP, int nodePort) {
        String nodeName = String.format("Node%s", manager.machinesOnlineNumber() + 1);
        Node newNode = new Node(nodeName, nodeIP, nodePort);
        manager.addNewMachine(newNode);
        return newNode;
    }

    private int findAvailablePort(){
        try {
            ServerSocket newSocket = new ServerSocket(0);
            int freePort = newSocket.getLocalPort();
            newSocket.close();
            return newSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void runSystem() {
        Thread a = Thread.currentThread();
        System.out.println("Running System....");
        DatagramSocket socket = null;
            try {

                socket = new DatagramSocket(serverPort);
                socket.setSoTimeout(0);
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String messages = new String(buffer);
                    String[] elements = messages.trim().split(",");
                    String command = elements[0].trim();
                    InetAddress tempNodeIP = InetAddress.getByName(elements[1].trim());
                    int tempNodePort = Integer.parseInt(elements[2].trim());
                    System.out.println(messages);
                    switch(command) {
                        case "STOP":
                            shutdown();
                            break;
                        case "NEW":
                            Node createdNode = createNewNode(tempNodeIP, tempNodePort);

                            createdNode.sendMessageToNode("CONNECTIONACCEPTED", this);
                            break;

                        default:
                            System.out.println("I don't understand: " + elements[0]);
                    }
                }

            } catch (Exception error) {
                error.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception error) {
                    error.printStackTrace();
                }
        }
    }
}
