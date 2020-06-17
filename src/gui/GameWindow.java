package gui;

import core.Ocean;
import core.Ship;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

public class GameWindow implements AlertDisplay {
    private enum GameState {
        Connect,    // Connection
        Setup,      // Setting up the fleet
        WaitSetup,  // Wait for partner to setup
        Game,       // Game itself
        EndOfGame   // Game has ended
    }

    private final int CELL_SIZE = 30;   // Size of battlefield cell on screen
    private final double WIDTH = 400,   // Width of battlefield canvas
            HEIGHT = 400;   // Height of battlefield canvas

    // Bounds of battlefield itself relative to canvas
    private double leftBound, rightBound, topBound, bottomBound;
    private Stage stage;                    // Main stage (required for closing)
    private Canvas myCanvas, partnerCanvas;                  // Canvas to draw the battlefield on
    private Ocean ocean;                    // Ocean object for performing the game logic
    private TextArea logDisplay;            // TextArea for logs
    private boolean isMyTurn;
    private Ocean.CellStatus[][] shotResults;
    private String myName, partnerName;
    private int myShots, partnerShots, partnerShipsDestroyed;
    private GameState state;
    private int shipSize;
    private boolean shipDirection;
    private Button playButton;

    // Images
    private Image background,       // Background for battlefield
            missed,         // Missed shot
            damaged,        // Damaged part of the ship
            destroyedHor,       // Part of destroyed horizontal ship
            destroyedVer,       // Part of destroyed vertical ship
            shipHor,
            shipVer;

    private Scanner socketReader;
    private PrintWriter socketWriter;

    /**
     * Initialize the game
     */
    private void initializeGame() {
        ocean = new Ocean();
        logDisplay.clear();
        shotResults = new Ocean.CellStatus[ocean.OCEAN_SIZE][ocean.OCEAN_SIZE];
        for (int i = 0; i < ocean.OCEAN_SIZE; ++i)
            for (int j = 0; j < ocean.OCEAN_SIZE; ++j)
                shotResults[i][j] = Ocean.CellStatus.Unknown;
        myShots = partnerShots = 0;
    }

    /**
     * Draw the ocean
     */
    private void drawMyOcean() {
        GraphicsContext gc = myCanvas.getGraphicsContext2D();
        gc.drawImage(background, 0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < ocean.OCEAN_SIZE; ++i) {
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(i),
                    leftBound + CELL_SIZE * i + CELL_SIZE / 2 - 3,
                    topBound - 5);
            gc.fillText(String.valueOf(i),
                    leftBound - 12,
                    topBound + CELL_SIZE * i + CELL_SIZE / 2 + 4);

            for (int j = 0; j < ocean.OCEAN_SIZE; ++j) {
                Image toDraw = null;
                if (ocean.hasShipAt(i, j)) {
                    gc.drawImage(ocean.isHorizontalAt(i, j) ? shipHor : shipVer,
                            leftBound + j * CELL_SIZE,
                            topBound + i * CELL_SIZE,
                            CELL_SIZE, CELL_SIZE);
                }
                switch (ocean.getCellStatus(i, j)) {
                    case Missed:
                        toDraw = missed;
                        break;

                    case Damaged:
                        toDraw = damaged;
                        break;

                    case DestroyedHor:
                        toDraw = destroyedHor;
                        break;

                    case DestroyedVer:
                        toDraw = destroyedVer;
                        break;
                }
                if (toDraw != null) {
                    gc.drawImage(toDraw,
                            leftBound + j * CELL_SIZE,
                            topBound + i * CELL_SIZE,
                            CELL_SIZE, CELL_SIZE);
                }
            }
        }

        gc.setFill(Color.BLACK);
        gc.setLineWidth(1.5);
        for (int i = 0; i <= ocean.OCEAN_SIZE; ++i) {
            gc.strokeLine(leftBound, topBound + CELL_SIZE * i,
                    rightBound, topBound + CELL_SIZE * i);
            gc.strokeLine(leftBound + CELL_SIZE * i, topBound,
                    leftBound + CELL_SIZE * i, bottomBound);
        }
    }

    private void drawPartnerOcean() {
        GraphicsContext gc = partnerCanvas.getGraphicsContext2D();
        gc.drawImage(background, 0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < ocean.OCEAN_SIZE; ++i) {
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(i),
                    leftBound + CELL_SIZE * i + CELL_SIZE / 2 - 3,
                    topBound - 5);
            gc.fillText(String.valueOf(i),
                    leftBound - 12,
                    topBound + CELL_SIZE * i + CELL_SIZE / 2 + 4);

            for (int j = 0; j < ocean.OCEAN_SIZE; ++j) {
                Image toDraw = null;
                switch (shotResults[i][j]) {
                    case Missed:
                        toDraw = missed;
                        break;

                    case Damaged:
                        toDraw = damaged;
                        break;

                    case DestroyedHor:
                        toDraw = destroyedHor;
                        break;

                    case DestroyedVer:
                        toDraw = destroyedVer;
                        break;
                }
                if (toDraw != null) {
                    gc.drawImage(toDraw,
                            leftBound + j * CELL_SIZE,
                            topBound + i * CELL_SIZE,
                            CELL_SIZE, CELL_SIZE);
                }
            }
        }

        gc.setFill(Color.BLACK);
        gc.setLineWidth(1.5);
        for (int i = 0; i <= ocean.OCEAN_SIZE; ++i) {
            gc.strokeLine(leftBound, topBound + CELL_SIZE * i,
                    rightBound, topBound + CELL_SIZE * i);
            gc.strokeLine(leftBound + CELL_SIZE * i, topBound,
                    leftBound + CELL_SIZE * i, bottomBound);
        }
    }

    private void endGame(boolean isDisconnect, boolean didIWon) {
        if (isDisconnect) {
            showError("Connection to your partner is lost. Game is closing.");
            stage.close();
        }
        else {
            showInfo(String.format("Game over! Player %s wins.\n" +
                    "%s (you) has made %d shots\n" +
                    "%s (partner) has made %d shots",
                    (didIWon ? myName : partnerName), myName, myShots, partnerName, partnerShots));
        }
        state = GameState.EndOfGame;
    }

    /**
     * Receive shot from partner
     */
    private void getPartnerTurn() {
        if (state != GameState.Game) {
            return;
        }
        if (isMyTurn) {
            return;
        }
        int row = 0, column = 0;
        try {
            row = socketReader.nextInt();
            column = socketReader.nextInt();
        }
        catch (Exception e) {
            // Socket is closed, notify the player and end the game
            Platform.runLater(() -> endGame(true, false));
        }
        if (ocean.isGameOver())
            return;
        if (ocean.hasShotAt(row, column)){
            logDisplay.appendText(String.format("%s: (%d, %d) = %s\n", partnerName, row, column, "duplicate shot"));
            socketWriter.println(Ocean.CellStatus.Duplicate.getValue());
            return;
        }
        String resString;
        if (ocean.shootAt(row, column)) {
            var ship = ocean.getShipArray()[row][column];
            resString = ship.isSunk() ? "ship destroyed" : "ship damaged";
        }
        else
            resString = "missed";
        logDisplay.appendText(String.format("%s: (%d, %d) = %s\n", partnerName, row, column, resString));
        ++partnerShots;
        drawMyOcean();
        socketWriter.println(ocean.getCellStatus(row, column).getValue());
        if (ocean.isGameOver()) {
            logDisplay.appendText("Game over!\n");
            Platform.runLater(() -> endGame(false, false));
        }
        isMyTurn = true;
    }

    private void updateDestroyed(int row, int column) {
        if (shotResults[row][column] == Ocean.CellStatus.DestroyedVer) {
            for (int i = row - 1; i >= 0 && shotResults[i][column] == Ocean.CellStatus.Damaged; --i)
                shotResults[i][column] = Ocean.CellStatus.DestroyedVer;
            for (int i = row + 1; i < ocean.OCEAN_SIZE && shotResults[i][column] == Ocean.CellStatus.Damaged; ++i)
                shotResults[i][column] = Ocean.CellStatus.DestroyedVer;
        }
        else {
            for (int i = column - 1; i >= 0 && shotResults[row][i] == Ocean.CellStatus.Damaged; --i)
                shotResults[row][i] = Ocean.CellStatus.DestroyedHor;
            for (int i = column + 1; i < ocean.OCEAN_SIZE && shotResults[row][i] == Ocean.CellStatus.Damaged; ++i)
                shotResults[row][i] = Ocean.CellStatus.DestroyedHor;
        }
        if (++partnerShipsDestroyed >= ocean.FLEET_SIZE) {
            // We destroyed all partner's ships, end game
            drawPartnerOcean();
            endGame(false, true);
        }
    }

    private void performShot(int row, int column) {
        if (state == GameState.WaitSetup)
            showInfo("Your partner has not placed all of their ships yet");
        if (state != GameState.Game)
            return;
        if (isMyTurn) {
            isMyTurn = false;
            socketWriter.printf("%d %d\n", row, column);
            Ocean.CellStatus result = Ocean.CellStatus.values()[socketReader.nextInt()];
            shotResults[row][column] = result;
            String resString;
            switch (result) {
                case Missed:
                    resString = "missed";
                    break;

                case Damaged:
                    resString = "ship damaged";
                    break;

                case Duplicate:
                    resString = "duplicate shot";
                    break;

                default:
                    resString = "ship destroyed";
            }
            logDisplay.appendText(String.format("%s: (%d, %d) = %s\n", myName, row, column, resString));
            ++myShots;
            if (result.isDestroyed()) {
                updateDestroyed(row, column);
            }
            drawPartnerOcean();
            new Thread(this::getPartnerTurn).start();
        }
        else {
            showInfo("Please wait for your partner's turn");
        }
    }

    /**
     * Load images from resource files
     */
    private void loadImages() {
        background = new Image(gui.GameWindow.class.getResourceAsStream("/background.jpg"));
        missed = new Image(gui.GameWindow.class.getResourceAsStream("/missed.png"));
        damaged = new Image(gui.GameWindow.class.getResourceAsStream("/damaged.png"));
        destroyedHor = new Image(gui.GameWindow.class.getResourceAsStream("/destroyed_hor.png"));
        destroyedVer = new Image(gui.GameWindow.class.getResourceAsStream("/destroyed_ver.png"));
        shipHor = new Image(gui.GameWindow.class.getResourceAsStream("/ship_hor.png"));
        shipVer = new Image(gui.GameWindow.class.getResourceAsStream("/ship_ver.png"));
    }

    /**
     * Create menu bar
     * @return created menu bar
     */
    private MenuBar createMenu() {
        MenuBar menu = new MenuBar();

        Menu game = new Menu("Game"),
                about = new Menu("About");
        menu.getMenus().addAll(game, about);

        MenuItem exitItem = new MenuItem("Exit"),
                aboutItem = new MenuItem("About");
        game.getItems().add(exitItem);
        about.getItems().add(aboutItem);

        exitItem.setOnAction(e -> {
            Dialog<Object> dlg = new Dialog<>();
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.APPLY);
            dlg.getDialogPane().getButtonTypes().add(okButton);

            dlg.setTitle("Stop game");
            dlg.setContentText(myName + ": \"Stop game! OK?\"");

            Optional<Object> res = dlg.showAndWait();
            if (res.isPresent()) {
                stage.close();
            }
        });

        aboutItem.setOnAction(e -> {
            Dialog<Object> dialog = new Dialog<>();
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.APPLY);
            dialog.getDialogPane().getButtonTypes().add(okButton);

            dialog.setTitle("About");
            dialog.setContentText("Battleship game\n©2020, Vasily Novak, BSE184");
            dialog.show();
        });

        return menu;
    }

    private void waitForPartner() {
        try {
            socketReader.nextLine();
            state = GameState.Game;
        }
        catch (Exception e) {
            // Partner disconnected
            Platform.runLater(() -> endGame(true, false));
        }
        if (!isMyTurn) {
            new Thread(this::getPartnerTurn).start();
        }
    }

    private GridPane createAllocatorPane() {
        GridPane allocatePane = new GridPane();
        allocatePane.setHgap(10);

        Button[] place = new Button[4];
        Button edit, rotate, allocate;

        for (int i = 0; i < 4; ++i) {
            allocatePane.add(place[i] = new Button(String.valueOf(i + 1)), i, 0);
            final int iCopy = i + 1;
            place[i].setDisable(true);
            place[i].setOnAction(e -> shipSize = iCopy);
        }
        allocatePane.add(edit = new Button("Edit"), 4, 0);
        allocatePane.add(rotate = new Button("Rotate"), 5, 0);
        allocatePane.add(allocate = new Button("Allocate"), 6, 0);
        allocatePane.add(playButton = new Button("Play"), 7, 0);
        playButton.setDisable(true);
        rotate.setDisable(true);
        edit.setDisable(true);

        edit.setOnAction(e -> shipSize = 0);
        rotate.setOnAction(e -> shipDirection = !shipDirection);

        allocate.setOnAction(e -> {
            state = GameState.Setup;
            allocate.setDisable(true);
            shipSize = 0;
            for (int i = 0; i < 4; ++i)
                place[i].setDisable(false);
            edit.setDisable(false);
            rotate.setDisable(false);
            drawMyOcean();
        });

        playButton.setOnAction( e-> {
            state = GameState.WaitSetup;
            for (int i = 0; i < 4; ++i)
                place[i].setDisable(true);
            rotate.setDisable(true);
            edit.setDisable(true);
            playButton.setDisable(true);
            socketWriter.println(); // Send an empty message just to notify
            new Thread(this::waitForPartner).start();
        });

        return allocatePane;
    }

    private void editPlaceShip(int row, int column) {
        if (state != GameState.Setup)
            return;
        if (shipSize == 0) {
            // Try to select (= remove) ship at cell
            if (!ocean.hasShipAt(row, column))
                return;
            Ship removed = ocean.removeShipFrom(row, column);
            shipDirection = removed.isHorizontal();
            shipSize = removed.getLength();
        }
        else {
            // Try to place selected ship
            playButton.setDisable(!ocean.tryPlaceShipAt(row, column, shipDirection, shipSize));
        }
        drawMyOcean();
    }

    public boolean runGame(Stage primaryStage, Socket socket, String myName, boolean isClient) {
        stage = primaryStage;
        try {
            socketReader = new Scanner(socket.getInputStream());
            socketWriter = new PrintWriter(socket.getOutputStream(), true);
            if (isClient) {
                if (socket.getInputStream().available() > 0) {
                    // Server is busy
                    showError("Server is busy");
                    stage.close();
                    return false;
                }
                socketWriter.println(myName);
                partnerName = socketReader.nextLine();
            } else {
                partnerName = socketReader.nextLine();
                socketWriter.println(myName);
            }
        }
        catch (IOException e) {
            showError(e.toString());
            endGame(true, false);
        }
        this.myName = myName;
        isMyTurn = isClient;

        primaryStage.setTitle(isClient ? "Battleship client" : "Battleship server");
        loadImages();

        BorderPane root = new BorderPane();
        GridPane game = new GridPane();

        root.setCenter(game);
        game.setPadding(new Insets(20, 20, 20, 20));
        game.setHgap(10);
        game.setVgap(10);

        root.setTop(createMenu());

        myCanvas = new Canvas(WIDTH, HEIGHT);
        game.add(new Label("me"), 0, 0);
        game.add(myCanvas, 0, 1);

        myCanvas.setOnMouseClicked(event -> {
            int x = (int)(event.getX() - leftBound) / CELL_SIZE,
                    y = (int)(event.getY() - topBound) / CELL_SIZE;
            if (x < 0 || x >= ocean.OCEAN_SIZE)
                return;
            if (y < 0 || y >= ocean.OCEAN_SIZE)
                return;
            editPlaceShip(y, x);
        });

        GridPane allocPane = createAllocatorPane();
        game.add(allocPane, 0, 2);

        logDisplay = new TextArea();
        logDisplay.setEditable(false);
        root.setBottom(logDisplay);
        BorderPane.setMargin(logDisplay, new Insets(20, 20, 20, 20));

        partnerCanvas = new Canvas(WIDTH, HEIGHT);
        game.add(new Label("partner"), 1, 0);
        game.add(partnerCanvas, 1, 1);

        TextField noMouseInput = new TextField();
        game.add(noMouseInput, 1, 2);
        noMouseInput.setOnAction(e -> {
            if (state == GameState.EndOfGame)
                return;
            String[] input = noMouseInput.getText().split(" ");
            int row, column;
            try {
                row = Integer.parseInt(input[0]);
                column = Integer.parseInt(input[1]);
            }
            catch (NumberFormatException | ArrayIndexOutOfBoundsException exc) {
                showError("You should enter 2 numbers separated by one space\n");
                return;
            }
            if (row < 0 || row >= ocean.OCEAN_SIZE || column < 0 || column >= ocean.OCEAN_SIZE) {
                showError("Coordinates should be from 0 to 9 inclusive\n");
                return;
            }
            noMouseInput.clear();
            if (state == GameState.Setup) {
                editPlaceShip(row, column);
                drawMyOcean();
            }
            else if (state == GameState.Game) {
                performShot(row, column);
                drawPartnerOcean();
            }
        });

        topBound = HEIGHT / 2 - 5 * CELL_SIZE;
        bottomBound = HEIGHT / 2 + 5 * CELL_SIZE;
        leftBound = WIDTH / 2 - 5 * CELL_SIZE;
        rightBound = WIDTH / 2 + 5 * CELL_SIZE;

        initializeGame();
        drawMyOcean();
        drawPartnerOcean();

        partnerCanvas.setOnMouseClicked(event -> {
            int x = (int)(event.getX() - leftBound) / CELL_SIZE,
                    y = (int)(event.getY() - topBound) / CELL_SIZE;
            if (x < 0 || x >= ocean.OCEAN_SIZE)
                return;
            if (y < 0 || y >= ocean.OCEAN_SIZE)
                return;
            performShot(y, x);
        });

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        state = GameState.Connect;
        return true;
    }
}