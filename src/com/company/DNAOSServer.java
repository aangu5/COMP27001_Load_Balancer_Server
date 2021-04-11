package com.company;

import java.util.logging.Logger;
import java.util.logging.Level;

public class DNAOSServer {
    /**
     * takes the parameters provided and either runs the system or prints a message to console
     * Expected input is an integer to be used as the server port for incoming messages
     *
     */

    private static final Logger logger = Logger.getLogger(DNAOSServer.class.getName());

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.log(Level.SEVERE, "Usage: Example3 <server port>");
        } else {
            int portNumber = Integer.parseInt(args[0]);
            if (portNumber > 65535) {
                logger.log(Level.SEVERE, "This is too large! Please enter an available port number 1 - 65535");
            } else {
                new Server(Integer.parseInt(args[0]));
            }
        }
    }
}