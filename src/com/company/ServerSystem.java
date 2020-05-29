package com.company;

import java.net.*;

public class ServerSystem {
    private NodeManager nodeManager = new NodeManager();
    private WorkManager workManager = new WorkManager();
    private int serverPort;
    private InetAddress serverIP;

    public ServerSystem(int serverPort) {
        this.serverPort = serverPort;
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
            nodeManager.shutdownHostConnections();
            System.out.println("Turning off");
            System.exit(0);
        }
    }

    private Node createNewNode(InetAddress nodeIP, int nodePort, int inputMaxJobs) {
        int nodeID = nodeManager.getNextNodeID();
        Node newNode = new Node(this, nodeID, nodeIP, nodePort, inputMaxJobs);
        nodeManager.addNewMachine(newNode);
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
                availableNode.setWorking(true);
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
                        case "NEWWORK":
                            int tempDuration = Integer.parseInt(elements[1]);
                            createNewWork(tempDuration);
                            break;
                        case "SHUTDOWN":
                            shutdown();
                            break;
                        case "NEW":
                            InetAddress tempNodeIP = InetAddress.getByName(elements[1].trim());
                            int tempNodePort = Integer.parseInt(elements[2].trim());
                            int tempMaxJobs = Integer.parseInt(elements[3].trim());
                            currentNode = createNewNode(tempNodeIP, tempNodePort, tempMaxJobs);
                            currentNode.sendMessageToNode("ACCEPTED");
                            break;
                        case "READY":
                            assignWorkCreated();
                            break;
                        case "COMPLETE":
                            int completedWorkID = Integer.parseInt(elements[1].trim());
                            workComplete(completedWorkID);
                            assignWorkCreated();
                            break;
                        case "FAILEDWORK":
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
                            break;
                        case "ALIVE":
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = nodeManager.findByIPAndPort(tempNodeIP,tempNodePort);
                            currentNode.setWorking(false);
                            currentNode.checkNodeIn();
                            break;
                        case "WORKING":
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = nodeManager.findByIPAndPort(tempNodeIP,tempNodePort);
                            currentNode.setWorking(true);
                            currentNode.checkNodeIn();
                            break;
                        case "DEADNODE":
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = nodeManager.findByIPAndPort(tempNodeIP,tempNodePort);
                            nodeManager.delete(currentNode);
                            System.out.println("Node deleted due to unresponsiveness!");
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
