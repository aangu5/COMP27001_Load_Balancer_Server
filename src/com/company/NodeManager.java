package com.company;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.LinkedList;

public class NodeManager {

    /**
     * Three linked lists to represent the connected nodes, the nodes sorted by utilisation and all the nodes that have ever been connected
     */
    private LinkedList connectedNodes = new LinkedList();
    private LinkedList nodesByUtilisation = new LinkedList();
    private LinkedList allNodes = new LinkedList();

    /**
     * Adds a new node to the linked lists
     * @param newNode - node object representing the new node
     * @return - true or false based on the success of the nodes being added to the lists
     */
    public boolean addNewNode(Node newNode) {
        return connectedNodes.add(newNode) && nodesByUtilisation.add(newNode) && allNodes.add(newNode);
    }

    /**
     * returns a node based on a given ip address and port - both used to allow the program to be run on on machine
     * @param ipToFind - ip address of the node to find
     * @param portToFind - port used by the node to find
     * @return - returns the node object or null if not found
     */
    public Node findByIPAndPort(InetAddress ipToFind, int portToFind){
        for (int i = 0; i < connectedNodes.size(); i++) {
            Node listNode = (Node) connectedNodes.get(i);
            if (listNode.getNodeIPAddress().getHostAddress().equals(ipToFind.getHostAddress())) {
                if (listNode.getNodePort() == portToFind){
                    return listNode;
                }
            }
        }
        return null;
    }

    /**
     * Sorts the available nodes by their current utilisation - .reversed() is used to bring the most available node to the front of the list
     */
    public void sortNodesByUtilisation() {
        nodesByUtilisation.sort(Comparator.comparingDouble(Node::getCurrentUtilisation).reversed());
    }

    /**
     * Sorts the available nodes then returns the node at the front of the list if it has capacity for more work.
     * If the most free node is full or doesn't exist, the method returns null
     * @return - most free node if there is one available or null if not
     */
    public Node getMostFreeNode() {
        sortNodesByUtilisation();
        if (nodesByUtilisation.isEmpty()) {
            return null;
        } else {
            Node tempNode = (Node) nodesByUtilisation.getLast();
            if (tempNode.getCurrentUtilisation()  >= 100) {
                System.out.println("Unable to assign work - all nodes are full!");
                return null;
            } else {
                return tempNode;
            }

        }
    }

    /**
     * removes a Node object from the connectedNodes and nodesByUtilisation linked lists - if machine is not found, message printed to console
     * @param machine - node object of the node to delete
     */
    public void delete (Node machine) {
        try {
            connectedNodes.remove(machine);
            nodesByUtilisation.remove(machine);
        } catch (Exception e){
            System.out.println("Machine not found " + e);
        }
    }

    /**
     * gets the size of allNodes and returns the next integer to act as node ID
     * @return - size of allnodes + 1 for node ID
     */
    public int getNextNodeID() {
        return allNodes.size() + 1;
    }

    /**
     * sends messages to all connected nodes with a shutdown instruction before removing the nodes from the lists
     */
    public void shutdownNodeConnections() {
        for (int i = 0; i < connectedNodes.size(); i++) {
            Node listNode = (Node) connectedNodes.get(i);
            listNode.sendMessageToNode("SHUTDOWN");
            connectedNodes.remove(listNode);
            System.out.println("Node " + listNode.getNodeID() + " disconnected.");
        }
    }
}
