package com.company;

public class DNAOSServer {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: Example3 <server port>");
        } else {
            Server server = new Server(Integer.parseInt(args[0]));
            server.runSystem();
        }
    }
}