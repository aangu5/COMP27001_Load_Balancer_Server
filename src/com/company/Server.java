package com.company;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private final NodeManager nodeManager = new NodeManager();            //instantiates a NodeManager object to store all the Nodes
    private final WorkManager workManager = new WorkManager();            //instantiates a WorkManager object to store all the Work
    private final int serverPort;                                         //port that the server receives messages on
    private InetAddress serverIP;                                   //IP address of the server, set in the constructor using .getLocalHost()
    final boolean systemOnline;                                           //boolean flag that represents whether the system is online or not

    /**
     * Server constructor sets the port, IP and system online and then runs the system with the runSystem() method
     * @param serverPort - integer for the port that the server receives messages on
     */
    public Server(int serverPort) {
        this.serverPort = serverPort;
        systemOnline = true;
        try {
            serverIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        runSystem();
    }

    /**
     * @return ip address of server
     */
    public InetAddress getServerIP() {
        return serverIP;
    }

    /**
     * @return port of server
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * initiates a shutdown for the nodes and server if there is no work in progress or backlog
     */
    public void shutdown() {
        logger.log(Level.WARNING, "There is work in progress: {}", workManager.isWorkInProgress());
        if (workManager.isWorkInProgress() || workManager.isWorkAvailable()){
            logger.log(Level.WARNING, "Unable to shutdown due to work in progress!");
        } else {
            nodeManager.shutdownNodeConnections();
            logger.log(Level.INFO, "Turning off");
            System.exit(0);
        }
    }

    /**
     * local method to create a new node, calls the Node constructor and adds the new node into the node manager
     * @param nodeIP - IP address of new node
     * @param nodePort - port of new node
     * @param inputMaxJobs - number of max jobs the node can carry out at any one time
     * @return - Node object that is created
     */
    private Node createNewNode(InetAddress nodeIP, int nodePort, int inputMaxJobs) {
        int nodeID = nodeManager.getNextNodeID();
        Node newNode = new Node(this, nodeID, nodeIP, nodePort, inputMaxJobs);
        nodeManager.addNewNode(newNode);
        return newNode;
    }

    /**
     * local method to create a new Work object, calls the work constructor and adds the new work into the work manager
     * Then tries to assign the work to a node if there is one available, else the work goes into the backlog
     * @param duration - length of time in seconds that the job will take
     */
    public void createNewWork(int duration) {
        int workID = workManager.getNextWorkID();
        Work newWork = new Work(this, workID, duration);
        workManager.addWork(newWork);
        Node tempNode = assignWorkCreated();
        if (tempNode != null){
            logger.log(Level.INFO, "Work assigned to {}", tempNode.getNodeID());
            newWork.setWorkerNode(tempNode);
        } else {
            logger.log(Level.INFO, "Work not assignable. Adding to backlog.");
        }
    }

    /**
     * assigns Work to a Node based on whether work is available and which node is the most free node
     * Then sets the Node to working and sends it a message to start working
     * @return - the Node that the work was assigned to
     */
    private Node assignWorkCreated() {
        String messageToSend;
        if (workManager.isWorkAvailable()) {
            if (nodeManager.getMostFreeNode() == null) {
                return null;
            } else {
                Node availableNode = nodeManager.getMostFreeNode();
                Work availableWork = workManager.getAvailableWork();
                messageToSend = "WORK," + availableWork.getWorkID() + "," + availableWork.getDuration();
                workManager.startWork(availableWork);
                logger.log(Level.INFO, messageToSend);
                availableWork.setWorkerNode(availableNode);
                availableNode.sendMessageToNode(messageToSend);
                availableNode.newJob();
                return availableNode;
            }
        }
        return null;
    }

    /**
     * sets a Work object as complete, calls the setComplete method and removes the task from the node
     * @param workID - the work ID of the Work that was completed
     */
    private void workComplete(int workID){
        Work completedWork = workManager.findByID(workID);
        workManager.workComplete(completedWork);
        completedWork.setComplete(true);
        Node workerNode = completedWork.getWorkerNode();
        workerNode.jobComplete();
    }

    /**
     * Most critical method in this piece of work. Runs the system, opens the serverPort to receive messages and operates a switch case statement based on the message received.
     * Possible messages include:
     * - NEWWORK - used to create new Work objects
     * - SHUTDOWN - used to shutdown the system
     * - NEW - used to create new Node objects when a Node comes online
     * - READY - used to indicated that a Node is ready for work
     * - COMPLETE - used to indicate that work has been completed
     * - FAILEDWORK - used to indicate that work has failed
     * - ALIVE - used to indicate that a Node is alive after the server sends a STATUSCHECK message
     * - WORKING - used to indicate that a Node is working after the server sends a STATUSCHECK message
     * - DEADNODE - used to indicate that the server has detected a deadnode
     * Any other message will be printed to the console with a "I don't understand:" statement and the message
     */
    public void runSystem() {
        logger.log(Level.INFO, "Running System....");

        try (DatagramSocket socket = new DatagramSocket(serverPort)) {
            socket.setSoTimeout(0);

            while (systemOnline) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String messages = new String(buffer);
                String[] elements = messages.trim().split(",");
                String command = elements[0].trim();
                logger.log(Level.INFO, messages);
                switch (command) {
                    case "NEWWORK" -> newWork(elements);
                    case "SHUTDOWN" -> shutdown();
                    case "NEW" -> newNode(elements);
                    case "READY" -> assignWorkCreated();
                    case "COMPLETE" -> complete(elements);
                    case "FAILEDWORK" -> failedWork(elements);
                    case "ALIVE", "WORKING" -> normalStatus(elements);
                    case "DEADNODE" -> deadNode(elements);
                    default -> logger.log(Level.WARNING, "I do not understand: {}", elements[0]);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }



    private void newWork(String[] elements) {
        try {
            int tempDuration = Integer.parseInt(elements[1]);
            createNewWork(tempDuration);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
        }
    }

    private void newNode(String[] elements) {
        try {
            InetAddress tempNodeIP = InetAddress.getByName(elements[1].trim());
            int tempNodePort = Integer.parseInt(elements[2].trim());
            int tempMaxJobs = Integer.parseInt(elements[3].trim());
            Node currentNode = createNewNode(tempNodeIP, tempNodePort, tempMaxJobs);
            currentNode.sendMessageToNode("ACCEPTED");
        } catch (UnknownHostException | NumberFormatException exception) {
            exception.printStackTrace();
        }
    }

    private void complete(String[] elements) {
        try {
            int completedWorkID = Integer.parseInt(elements[1].trim());
            workComplete(completedWorkID);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
        }
        assignWorkCreated();
    }

    private void failedWork(String[] elements) {
        try {
            int tempWorkID = Integer.parseInt(elements[1].trim());
            Work tempWork = workManager.findByID(tempWorkID);
            Node badNode = tempWork.getWorkerNode();
            tempWork.setComplete(true);
            workManager.updateWork(tempWork);
            logger.log(Level.WARNING, "Removing bad node from operation");
            badNode.sendMessageToNode("SHUTDOWN");
            nodeManager.delete(badNode);
            createNewWork(tempWork.getDuration());
            tempWork.setWorkerNode(null);
            workManager.workComplete(tempWork);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
        }
    }

    private void normalStatus(String[] elements){
        try {
            InetAddress tempNodeIP = InetAddress.getByName(elements[1].trim());
            int tempNodePort = Integer.parseInt(elements[2].trim());
            Node currentNode = nodeManager.findByIPAndPort(tempNodeIP, tempNodePort);
            currentNode.checkNodeIn();
        } catch (NumberFormatException | UnknownHostException exception) {
            exception.printStackTrace();
        }
    }
    private void deadNode(String[] elements) {
        try {
            InetAddress tempNodeIP = InetAddress.getByName(elements[1].trim());
            int tempNodePort = Integer.parseInt(elements[2].trim());
            Node currentNode = nodeManager.findByIPAndPort(tempNodeIP, tempNodePort);
            nodeManager.delete(currentNode);
            logger.log(Level.WARNING, "Node deleted due to unresponsiveness!");
        } catch (NumberFormatException | UnknownHostException exception) {
            exception.printStackTrace();
        }
    }
}