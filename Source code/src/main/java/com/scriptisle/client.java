package com.scriptisle;

import java.io.*;
import java.net.Socket;

public class client {
    private String connectTo = null;
    private String path = null;
    private BufferedInputStream input = null;
    private DataOutputStream output = null;

    //client constructor
    public client(String connectTo, String path) {
        this.connectTo = connectTo;
        this.path = path;

        connect();
    }

    //connect to the given ip
    private void connect() {
        try {
            Socket socket = new Socket(this.connectTo, 49310);
            System.out.println("Connected to: " + this.connectTo);

            send(socket);

            closeConnection(socket);
        } catch (final Exception e) {
            System.out.println("Get an error trying to communicate to: " + this.connectTo + ".\n" + e);
        }
    }

    //send file to the connected server
    private void send(Socket socket) {
        try {
            File file = new File(this.path);
            byte[] byteArray = new byte[(int) file.length()];
            this.input = new BufferedInputStream(new FileInputStream(file));
            input.read(byteArray, 0, byteArray.length);
            this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            System.out.println("Sending " + file.getName() + "(" + byteArray.length + " bytes)");
            output.writeUTF(file.getName());
            output.writeLong(byteArray.length);
            output.write(byteArray, 0, byteArray.length);
            output.flush();
            System.out.println("Done.");
        } catch (final Exception e) {
            System.out.println("Get an error trying to send file to: " + this.connectTo + ".\n" + e);
        }
    }

    //close connection to the given ip
    private void closeConnection(Socket socket) {
        try {
            this.input.close();
        } catch (final Exception e) {
            System.out.println("Get an error trying to close input\n" + e);
        } finally {
            try {
                this.output.close();
            } catch (final Exception e) {
                System.out.println("Get an error trying to close output\n" + e);
            } finally {
                try {
                    socket.close();
                } catch (final Exception e) {
                    System.out.println("Get an error trying to close connection to: " + this.connectTo + ".\n" + e);
                    System.exit(1);
                }
            }

        }
    }
}

