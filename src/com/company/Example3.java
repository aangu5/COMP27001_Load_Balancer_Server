package com.company;

public class Example3 {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: Example3 <server port>");
        } else {
            ServerSystem theSystem = new ServerSystem(Integer.parseInt(args[0]));
            theSystem.runSystem();
        }
    }
}