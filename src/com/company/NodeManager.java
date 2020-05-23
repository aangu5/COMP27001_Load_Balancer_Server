package com.company;

import java.net.InetAddress;
import java.util.LinkedList;

public class NodeManager {
    private LinkedList connectedNodes = new LinkedList();

    public boolean addNewMachine(Node newMachine) {
        return connectedNodes.add( newMachine );
    }

    public Node findIP(InetAddress ipToFind) {
        for (int i = 0; i < connectedNodes.size(); i++) {
            Node listNode = (Node) connectedNodes.get(i);
            if (listNode.getNodeIPAddress().equals(ipToFind)) {
                return listNode;
            } else {
                return null;
            }
        }
        return null;
    }

    public Node findName(String host) {
        for (int i = 0; i < connectedNodes.size(); i++) {
            Node listNode = (Node) connectedNodes.get(i);
            if (listNode.getNodeName().equals(host)) {
                return listNode;
            } else {
                return null;
            }
        }
        return null;
    }

    public boolean delete (Node machine) {
        for (int i = 0; i < connectedNodes.size(); i++) {
            Node listNode = (Node) connectedNodes.get(i);
            if (listNode == machine) {
                connectedNodes.remove(machine);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public int machinesOnlineNumber() {
        return connectedNodes.size();
    }
}
