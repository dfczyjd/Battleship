package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;

public class ServerConnectionWindow extends Application implements AlertDisplay {

    private Stage stage;
    private ServerSocket serverSocket;
    private Socket socket;
    private Button listenButton;
    boolean isConnected;

    private void listen(String username, int port) {
        try {
            while (true) {
                Socket tmpSocket = serverSocket.accept();
                if (isConnected) {
                    // We already have a partner
                    PrintWriter writer = new PrintWriter(tmpSocket.getOutputStream(), true);
                    writer.println();
                    tmpSocket.close();
                }
                isConnected = true;
                socket = tmpSocket;
                Platform.runLater(() -> {
                    new GameWindow().runGame(new Stage(), socket, username, false);
                    stage.close();
                });
            }
        }
        catch (IOException e) {
            Platform.runLater(() -> showError(String.format("Failed to launch server on port %d", port)));
            listenButton.setDisable(false);
            listenButton.setText("Start");
        }
    }

    @Override
    public void stop() throws Exception {
        if (socket != null)
            socket.close();
        if (serverSocket != null)
            serverSocket.close();
        super.stop();
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        primaryStage.setTitle("Battleship server");

        GridPane root = new GridPane();
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(20, 20, 20, 20));

        root.add(new Label("Username:"), 0, 0);
        root.add(new Label("Server port:"), 0, 1);

        TextField usernameField, portField;
        root.add(usernameField = new TextField(), 1, 0);
        root.add(portField = new TextField(), 1, 1);
        root.add(listenButton = new Button("Start"), 1, 2);
        primaryStage.close();

        listenButton.setOnAction(e -> {
            int port;
            try {
                port = Integer.parseInt(portField.getText());
            }
            catch (NumberFormatException exc) {
                showError("Port must be a number from 0 to 65535 inclusive");
                return;
            }
            if (port < 0 || port >= 65536) {
                showError("Port must be a number from 0 to 65535 inclusive");
                return;
            }
            try {
                serverSocket = new ServerSocket(port);
            }
            catch (IOException exc) {
                Platform.runLater(() -> showError(String.format("Failed to launch server on port %d", port)));
            }
            isConnected = false;
            new Thread(() -> listen(usernameField.getText(), port)).start();
            listenButton.setDisable(true);
            listenButton.setText("Listening");
        });

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    static void start(String[] args) {
        launch(args);
    }
}
