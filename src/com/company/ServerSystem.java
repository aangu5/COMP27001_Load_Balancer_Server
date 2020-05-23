package com.company;

import java.io.IOException;
import java.net.*;

public class ServerSystem {
    GUI screen = new GUI(this);

    private NodeManager nodeManager = new NodeManager();
    private WorkManager workManager = new WorkManager();

    private int serverPort;

    public ServerSystem(int port) {
        screen.start();
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
        String nodeName = String.format("Node%s", nodeManager.machinesOnlineNumber() + 1);
        Node newNode = new Node(nodeName, nodeIP, nodePort);
        nodeManager.addNewMachine(newNode);
        return newNode;
    }

    public Work createNewWork(int duration) {
        Work newWork = new Work(workManager.getNextWorkID(),duration);
        workManager.addNewWork(newWork);
        Node tempNode = assignWorkCreated();
        if (tempNode != null){
            System.out.println("Work assigned to " + tempNode.getNodeName());
            newWork.setWorkerNode(tempNode);
            workManager.startWork(newWork);
        } else {
            System.out.println("Work not assignable. Adding to backlog.");
        }
        return newWork;
    }

    private Node assignWorkCreated() {
        String messageToSend;
        Node availableNode = nodeManager.mostFreeNode();
        Work availableWork = workManager.getAvailableWork();

        if (availableNode != null) {
            if (availableWork != null) {
                messageToSend = "WORK," + availableWork.getWorkID() + "," + availableWork.getDuration();
                availableNode.setWorking(true);
                workManager.startWork(availableWork);
                System.out.println(messageToSend);
                availableNode.sendMessageToNode(messageToSend);
                return availableNode;
            }
        }
        return null;
    }

private Node assignWorkNode(Node inputNode) {
        return null;

}
private int findAvailablePort(){
        try {
            ServerSocket newSocket = new ServerSocket(0);
            int freePort = newSocket.getLocalPort();
            newSocket.close();
            return freePort;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void workComplete(int workID){
        Work completedWork = workManager.findByID(workID);
        workManager.workComplete(completedWork);
        completedWork.setComplete(true);
    }

    public void runSystem() {
        Thread a = Thread.currentThread();
        System.out.println("Running System....");
        DatagramSocket socket = null;
            try {

                socket = new DatagramSocket(serverPort);
                socket.setSoTimeout(0);
                while (true) {
                    Node currentNode;
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String messages = new String(buffer);
                    String[] elements = messages.trim().split(",");
                    String command = elements[0].trim();
                    System.out.println(messages);
                    switch(command) {
                        case "STOP":
                            shutdown();
                            break;
                        case "NEW":
                            InetAddress tempNodeIP = InetAddress.getByName(elements[1].trim());
                            int tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = createNewNode(tempNodeIP, tempNodePort);
                            currentNode.sendMessageToNode("ACCEPTED");
                            break;
                        case "READY":
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            currentNode = nodeManager.findIP(tempNodeIP);
                            currentNode.setWorking(true);
                            assignWorkCreated();
                            break;
                        case "COMPLETE":
                            int completedWorkID = Integer.parseInt(elements[1].trim());
                            workComplete(completedWorkID);
                            assignWorkCreated();
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
