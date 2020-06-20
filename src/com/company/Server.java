package com.company;

import java.io.IOException;
import java.net.*;

public class Server {
    private NodeManager nodeManager = new NodeManager();
    private WorkManager workManager = new WorkManager();
    private int serverPort;
    private InetAddress serverIP;
    boolean systemOnline;

    public Server(int serverPort) {
        this.serverPort = serverPort;
        systemOnline = true;
        try {
            serverIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public InetAddress getServerIP() {
        return serverIP;
    }
    public int getServerPort() {
        return serverPort;
    }

    public void shutdown() {
        System.out.println("There is work in progress: " + workManager.isWorkInProgress());
        if (workManager.isWorkInProgress() || workManager.isWorkAvailable()){
            System.out.println("Unable to shutdown due to work in progress!");
        } else {
            nodeManager.shutdownNodeConnections();
            System.out.println("Turning off");
            System.exit(0);
        }
    }

    private Node createNewNode(InetAddress nodeIP, int nodePort, int inputMaxJobs) {
        int nodeID = nodeManager.getNextNodeID();
        Node newNode = new Node(this, nodeID, nodeIP, nodePort, inputMaxJobs);
        nodeManager.addNewNode(newNode);
        return newNode;
    }

    public void createNewWork(int duration) {
        int workID = workManager.getNextWorkID();
        Work newWork = new Work(this, workID, duration);
        workManager.addWork(newWork);
        Node tempNode = assignWorkCreated();
        if (tempNode != null){
            System.out.println("Work assigned to " + tempNode.getNodeID());
            newWork.setWorkerNode(tempNode);
        } else {
            System.out.println("Work not assignable. Adding to backlog.");
        }
    }

    private Node assignWorkCreated() {
        String messageToSend;
        if (workManager.isWorkAvailable()) {
            if (nodeManager.getMostFreeNode() == null) {
                return null;
            } else {
                Node availableNode = nodeManager.getMostFreeNode();
                Work availableWork = workManager.getAvailableWork();
                messageToSend = "WORK," + availableWork.getWorkID() + "," + availableWork.getDuration();
                availableNode.setNodeWorkingState(true);
                workManager.startWork(availableWork);
                System.out.println(messageToSend);
                availableWork.setWorkerNode(availableNode);
                availableNode.sendMessageToNode(messageToSend);
                availableNode.newJob();
                return availableNode;
            }
        }
        return null;
    }

    private void workComplete(int workID){
        Work completedWork = workManager.findByID(workID);
        workManager.workComplete(completedWork);
        completedWork.setComplete(true);
        Node workerNode = completedWork.getWorkerNode();
        workerNode.jobComplete();
    }

    public void runSystem() {
        System.out.println("Running System....");
        DatagramSocket socket = null;
        InetAddress tempNodeIP = null;
        int tempNodePort = 0;
        int tempMaxJobs = 0;
        try {
            socket = new DatagramSocket(serverPort);
            socket.setSoTimeout(0);

            while (systemOnline) {
                Node currentNode;
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String messages = new String(buffer);
                String[] elements = messages.trim().split(",");
                String command = elements[0].trim();
                System.out.println(messages);
                switch (command) {
                    case "NEWWORK":
                        try {
                            int tempDuration = Integer.parseInt(elements[1]);
                            createNewWork(tempDuration);
                        } catch (NumberFormatException exception) {
                            exception.printStackTrace();
                        }
                        break;
                    case "SHUTDOWN":
                        shutdown();
                        break;
                    case "NEW":
                        try {
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            tempMaxJobs = Integer.parseInt(elements[3].trim());
                            currentNode = createNewNode(tempNodeIP, tempNodePort, tempMaxJobs);
                            currentNode.sendMessageToNode("ACCEPTED");
                        } catch (UnknownHostException | NumberFormatException exception) {
                            exception.printStackTrace();
                        }
                        break;
                    case "READY":
                        assignWorkCreated();
                        break;
                    case "COMPLETE":
                        try {
                            int completedWorkID = Integer.parseInt(elements[1].trim());
                            workComplete(completedWorkID);
                        } catch (NumberFormatException exception) {
                            exception.printStackTrace();
                        }
                        assignWorkCreated();
                        break;
                    case "FAILEDWORK":
                        try {
                            int tempWorkID = Integer.parseInt(elements[1].trim());
                            Work tempWork = workManager.findByID(tempWorkID);
                            tempWork.setComplete(false);
                            workManager.updateWork(tempWork);
                            System.out.println("Removing bad node from operation");
                            Node badNode = tempWork.getWorkerNode();
                            badNode.sendMessageToNode("SHUTDOWN");
                            nodeManager.delete(badNode);
                            createNewWork(tempWork.getDuration());
                            tempWork.setWorkerNode(null);
                        } catch (NumberFormatException exception) {
                            exception.printStackTrace();
                        }
                        break;
                    case "ALIVE":
                        try {
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = nodeManager.findByIPAndPort(tempNodeIP, tempNodePort);
                            currentNode.setNodeWorkingState(false);
                            currentNode.checkNodeIn();
                        } catch (NumberFormatException | UnknownHostException exception) {
                            exception.printStackTrace();
                        }
                        break;
                    case "WORKING":
                        try {
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = nodeManager.findByIPAndPort(tempNodeIP, tempNodePort);
                            currentNode.setNodeWorkingState(true);
                            currentNode.checkNodeIn();
                        } catch (NumberFormatException | UnknownHostException exception) {
                            exception.printStackTrace();
                        }
                        break;
                    case "DEADNODE":
                        try {
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = nodeManager.findByIPAndPort(tempNodeIP, tempNodePort);
                            nodeManager.delete(currentNode);
                            System.out.println("Node deleted due to unresponsiveness!");
                        } catch (NumberFormatException | UnknownHostException exception) {
                            exception.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println("I don't understand: " + elements[0]);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
