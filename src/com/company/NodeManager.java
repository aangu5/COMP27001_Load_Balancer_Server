package com.company;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.LinkedList;

public class NodeManager {
    private LinkedList connectedNodes = new LinkedList();
    private LinkedList nodesByUtilisation = new LinkedList();
    private LinkedList allNodes = new LinkedList();

    public boolean addNewMachine(Node newMachine) {
        return connectedNodes.add(newMachine) && nodesByUtilisation.add(newMachine) && allNodes.add(newMachine);
    }

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


    public void sortNodesByUtilisation() {
        nodesByUtilisation.sort(Comparator.comparingDouble(Node::getCurrentUtilisation).reversed());
    }

    public Node getMostFreeNode() {
        sortNodesByUtilisation();
        if (nodesByUtilisation.isEmpty()) {
            return null;
        } else {
            return (Node) nodesByUtilisation.getLast();
        }
    }

    public void delete (Node machine) {
        try {
            connectedNodes.remove(machine);
            nodesByUtilisation.remove(machine);
        } catch (Exception e){
            System.out.println("Machine not found " + e);
        }
    }

    public int getNextNodeID() {
        return allNodes.size() + 1;
    }

    public void shutdownHostConnections() {
        for (int i = 0; i < connectedNodes.size(); i++) {
            Node listNode = (Node) connectedNodes.get(i);
            listNode.sendMessageToNode("SHUTDOWN");
            connectedNodes.remove(listNode);
            System.out.println("Node " + listNode.getNodeID() + " disconnected.");
        }
    }
}
