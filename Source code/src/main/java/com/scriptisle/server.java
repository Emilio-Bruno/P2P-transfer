package com.scriptisle;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class server {
    private DataInputStream input = null;
    private BufferedOutputStream output = null;
    private ServerSocket server = null;

    //server constructor
    public server() {
        try {
            server = new ServerSocket(49310);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");

            new Thread(() -> {
                acceptClient(server);
            }).start();
        } catch (final Exception e) {
            System.out.println("Get an error trying to create a server on this machine.\n" + e);
            System.exit(1);
        }
    }

    //Accept a client to connect to this device
    private void acceptClient(ServerSocket server) {
        try {
            while (true) {
                Socket socket = server.accept();
                System.out.println("Client accepted");
                new Thread(() -> {
                    receive(socket);
                }).start();
            }
        } catch (final Exception e) {
            System.out.println("Get an error trying to accept a client.\n" + e);
        }
    }

    //Read and elaborate the file received
    private void receive(Socket socket) {
        try {
            String fileName;
            long fileSize;
            int bytesRead, current = 0;

            this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            fileName = this.input.readUTF();
            fileSize = this.input.readLong();
            byte[] byteArray = new byte[1024];
            String path = Files.readString(Paths.get("directory")).trim();
            if (!path.isEmpty())
                path = path + "/";
            this.output = new BufferedOutputStream(new FileOutputStream(path + fileName));
            while (fileSize > 0
                    && (bytesRead = this.input.read(byteArray, 0, (int) Math.min(byteArray.length, fileSize))) > -1) {
                this.output.write(byteArray, 0, bytesRead);
                fileSize -= bytesRead;
                current += bytesRead;
            }

            this.output.flush();
            System.out.println("File " + fileName + " downloaded (" + current + " bytes read)");
        } catch (final Exception e) {
            System.out.println("Get an error trying to receive a file\n" + e);
        } finally {
            closeConnection(socket);
        }
    }

    //close connection
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
                    System.out.println("Get an error trying to close connection\n" + e);
                    System.exit(1);
                }
            }

        }
    }

    //close server
    public void closeServer() {
        try {
            this.server.close();
        } catch (final Exception e) {
            System.out.println("Get an error trying to close server\n" + e);
            System.exit(1);
        }
    }
}

