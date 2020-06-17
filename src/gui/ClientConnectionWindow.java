package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientConnectionWindow extends Application implements AlertDisplay {

    private Stage stage;
    private Socket socket;
    private Button connectButton;

    private void connect(String username, String ip, int port) {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        socket = new Socket();
        try {
            socket.connect(address);
            Platform.runLater( () -> {
                if (new GameWindow().runGame(new Stage(), socket, username, true))
                    stage.close();
                else {
                    connectButton.setDisable(false);
                    connectButton.setText("Connect");
                    try {
                        socket.close();
                    }
                    catch (IOException ignored) {

                    }
                }
            });
        }
        catch (IOException e) {
            Platform.runLater(() -> showError(String.format("Failed to connect to %s:%d", ip, port)));
            connectButton.setDisable(false);
            connectButton.setText("Connect");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        primaryStage.setTitle("Battleship client");

        GridPane root = new GridPane();
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(20, 20, 10, 10));

        root.add(new Label("Username:"), 0, 0);
        root.add(new Label("Server address:"), 0, 1);
        root.add(new Label("Server port:"), 0, 2);

        TextField usernameField, IPField, portField;
        root.add(usernameField = new TextField(), 1, 0);
        root.add(IPField = new TextField(), 1, 1);
        root.add(portField = new TextField(), 1, 2);
        root.add(connectButton = new Button("Connect"), 1, 3);

        connectButton.setOnAction(e -> {
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
            new Thread(() -> connect(usernameField.getText(), IPField.getText(), port)).start();
            connectButton.setDisable(true);
            connectButton.setText("Connecting");
        });

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (socket != null)
            socket.close();
        super.stop();
    }

    static void start(String[] args) {
        launch(args);
    }
}
