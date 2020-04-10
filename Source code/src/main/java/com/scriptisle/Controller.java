package com.scriptisle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public TextField myIp;
    public GridPane ipAddressesTable;
    public ScrollPane ScrollView;
    public VBox progressesFindIpContainer;
    public ProgressIndicator progressesFindIp;
    public TextField fileToSendPath;
    public TextField pathForFile;
    private Stage primaryStage;
    private IpFinder ip = null;
    private IpFinder otherIp = null;
    private server serverConnection = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ip = new IpFinder();

        //create a server on this machine
        serverConnection = new server();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                otherIp = new IpFinder(ip.getMyIp());

                fillInTable();

                progressesFindIpContainer.setVisible(false);
                ScrollView.setVisible(true);

                return null;
            }
        };

        //Bind the progress indicator to the task
        progressesFindIp.progressProperty().bind(task.progressProperty());

        //Thread that call the task above
        Thread thread = new Thread(task);
        thread.start();

        myIp.setText(ip.getMyIp());
        try {
            String path = Files.readString(Paths.get("directory"));
            pathForFile.setText(path);
        } catch (final Exception e) {
            System.out.println("Get an error trying to get directory path.\n" + e);
            System.exit(1);
        }
    }

    // Fill in the table with the devices connected to your router
    private void fillInTable() {

        Platform.runLater(() -> {
            ipAddressesTable.getChildren().clear();

            Label Name = new Label("Name");
            Label Ip = new Label("Ip");
            Label SelectTitle = new Label("Select");

            GridPane.setHalignment(SelectTitle, HPos.CENTER);
            GridPane.setHalignment(Name, HPos.CENTER);
            GridPane.setHalignment(Ip, HPos.CENTER);

            SelectTitle.setStyle("-fx-padding: 10 0 10 0;");

            ipAddressesTable.add(SelectTitle, 0, 0, 1, 1);
            ipAddressesTable.add(Name, 1, 0, 1, 1);
            ipAddressesTable.add(Ip, 2, 0, 1, 1);

            int i = 1;
            for (Map.Entry<InetAddress, String> entry : otherIp.getOtherIp().entrySet()) {
                CheckBox Select = new CheckBox();
                Name = new Label(entry.getValue());
                Ip = new Label(entry.getKey().toString());

                if (!Ip.getText().equals("/" + ip.getMyIp())) {
                    GridPane.setHalignment(Name, HPos.CENTER);
                    GridPane.setHalignment(Ip, HPos.CENTER);
                    GridPane.setHalignment(Select, HPos.CENTER);

                    Select.setStyle("-fx-padding: 10 0 10 0;");

                    ipAddressesTable.add(Select, 0, i, 1, 1);
                    ipAddressesTable.add(Name, 1, i, 1, 1);
                    ipAddressesTable.add(Ip, 2, i, 1, 1);

                    i++;
                }
            }
        });
    }

    //Find absolute path for the file on the computer
    public void findFileToSend() {
        FileChooser chooser = new FileChooser();

        File absolutePath = chooser.showOpenDialog(primaryStage);

        if (absolutePath != null)
            fileToSendPath.setText(absolutePath.getAbsolutePath());
    }

    //Find absolute path for the directory on the computer
    public void findDirectoryForFile() {
        DirectoryChooser chooser = new DirectoryChooser();

        File absolutePath = chooser.showDialog(primaryStage);

        if (absolutePath != null) {
            pathForFile.setText(absolutePath.getAbsolutePath());
            try {
                FileWriter file = new FileWriter("directory");
                PrintWriter writer = new PrintWriter(file);
                writer.write("");
                writer.append(absolutePath.getAbsolutePath());
                writer.close();
            } catch (final Exception e) {
                System.out.println("Get an error trying to save directory position for storing file.\n" + e);
                System.exit(1);
            }
        }
    }

    //Function used to send file to other devices
    public void sendFile() {
        int i = 0;
        boolean flag = false;

        for (Node child : ipAddressesTable.getChildren()) {
            if (child instanceof CheckBox && ((CheckBox) child).isSelected()) {
                flag = true;
                i = 0;
            } else if (i == 2 && flag == true) {
                new Thread(() -> {
                    String receiver = ((Label) child).getText().substring(1);
                    if ((new File(fileToSendPath.getText())).exists()) {
                        client Client = new client(receiver, fileToSendPath.getText());
                    } else {
                        System.out.println("Get an error because you didn't choose the file to send.");
                    }
                }).start();
                flag = false;
            }
            i++;
        }
    }

    //reload table with other ip addresses
    public void reloadOtherIp() {
        progressesFindIpContainer.setVisible(true);
        ScrollView.setVisible(false);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                otherIp = new IpFinder(ip.getMyIp());

                fillInTable();

                progressesFindIpContainer.setVisible(false);
                ScrollView.setVisible(true);

                return null;
            }
        };

        //Thread that call the task above
        Thread thread = new Thread(task);
        thread.start();
    }

    //close server before closing the app
    public void shutdown() {
        serverConnection.closeServer();
    }
}