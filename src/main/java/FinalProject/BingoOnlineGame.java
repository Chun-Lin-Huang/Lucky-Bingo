package FinalProject;

import FinalProject.network.NetworkGameManager;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class BingoOnlineGame extends Application {

    private static final int SIZE = 5;
    private static final int CARD_COUNT = 4;

    private Label[][] leftGrid;
    private boolean[][] leftMarked;
    private final Set<Integer> calledNumbers = new HashSet<>();
    private final StackPane leftOverlay = new StackPane();
    private final Label boxLabel = new Label("?");
    private final Label turnLabel = new Label();
    private final Label statusLabel = new Label("請選擇卡牌...");
    private boolean cardSelected = false;
    private boolean peerCardSelected = false;
    private final List<StackPane> cardSlots = new ArrayList<>();

    private boolean isOnline = false;
    private boolean isHost = false;
    private boolean isMyTurn = false;
    private boolean gameOver = false;
    private boolean lastGameIWon = false;
    private boolean hasExited = false;
    private NetworkGameManager netManager;

    private HBox cardSelectionBox;

    public BingoOnlineGame() {
    }

    public BingoOnlineGame(boolean isOnline, boolean isHost, NetworkGameManager netManager) {
        this.isOnline = isOnline;
        this.isHost = isHost;
        this.netManager = netManager;
        this.isMyTurn = isHost;
    }

    @Override
    public void start(Stage primaryStage) {
        cardSelectionBox = new HBox(20);
        cardSelectionBox.setAlignment(Pos.CENTER);
        setupCardSelectionArea(cardSelectionBox);

        turnLabel.setVisible(false); // 初始不顯示「輪到你了」
        statusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #000;");
        HBox topBar = new HBox(turnLabel);
        topBar.setAlignment(Pos.CENTER);

        StackPane mysteryBox = createMysteryBox();

        Button startButton = new Button("重新開始");
        startButton.setPrefSize(200, 60);
        startButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        startButton.setOnAction(e -> {
            resetGame(cardSelectionBox);
            if (isOnline) {
                netManager.sendMessage("RESET");
            }
        });

        Button exitButton = new Button("回到大廳");
        exitButton.setPrefSize(200, 60);
        exitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        exitButton.setOnAction(e -> {
            hasExited = true;

            // 嘗試傳送退出訊息與關閉連線
            if (netManager != null) {
                try {
                    netManager.sendMessage("EXIT");
                    netManager.close();
                } catch (Exception ignored) {
                }
            }

            // 切換畫面：放入 UI 執行緒排程內
            javafx.application.Platform.runLater(() -> {
                Stage currentStage = (Stage) ((Button) e.getSource()).getScene().getWindow();
                try {
                    new BingoLobby().start(currentStage);  // ✅ 替換畫面而非關閉整個視窗
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        HBox bottomButtons = new HBox(30, startButton, exitButton);
        bottomButtons.setAlignment(Pos.CENTER);

        VBox root = new VBox(30, statusLabel, topBar, cardSelectionBox, leftOverlay, mysteryBox, bottomButtons);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));

        BackgroundImage bgImage = new BackgroundImage(
                new Image(getClass().getResource("/gamebackground.jpeg").toExternalForm()),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(100, 100, true, true, true, false)
        );
        root.setBackground(new Background(bgImage));

        Scene scene = new Scene(root, 750, 1000);
        primaryStage.setTitle("Bingo Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (isOnline) {
            new Thread(this::listenForCalls).start();
        }
    }

    private void listenForCalls() {
        try {
            while (true) {
                String msg = netManager.receiveMessage();
                if (msg.startsWith("CALL:")) {
                    int number = Integer.parseInt(msg.substring(5));
                    javafx.application.Platform.runLater(() -> {
                        if (gameOver) {
                            return;
                        }
                        boxLabel.setText(String.valueOf(number));
                        calledNumbers.add(number);
                        highlightAndMark(leftGrid, leftMarked, number);
                        if (checkBingoCount(leftMarked) >= 3 && !gameOver) {
                            gameOver = true;
                            showBingoMessage(leftOverlay);
                            netManager.sendMessage("WIN");
                        }
                    });
                } else if (msg.equals("TURN")) {
                    javafx.application.Platform.runLater(() -> {
                        isMyTurn = true;
                        updateTurnLabel();
                    });
                } else if (msg.equals("WIN")) {
                    javafx.application.Platform.runLater(() -> {
                        if (!gameOver) {
                            gameOver = true;
                            showLoseMessage(leftOverlay);
                        }
                    });
                } else if (msg.equals("CARD_SELECTED")) {
                    javafx.application.Platform.runLater(() -> {
                        peerCardSelected = true;
                        checkGameStartReady();
                    });
                } else if (msg.equals("RESET")) {
                    javafx.application.Platform.runLater(() -> {
                        resetGame(cardSelectionBox);  // 你可以選擇重新傳入原本的 cardBox 參數
                    });
                } else if (msg.equals("EXIT")) {
                    javafx.application.Platform.runLater(() -> {
                        if (!hasExited) { // 只有未主動退出的才顯示提示
                            gameOver = true;
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("通知");
                            alert.setHeaderText(null);
                            alert.setContentText("對方已退出遊戲!");
                            alert.showAndWait();
                        }
                    });
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTurnLabel() {
        turnLabel.setText(isMyTurn ? "你的回合！" : "請等待對方...");
        turnLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #333;");
    }

    private void checkGameStartReady() {
        if (!cardSelected) {
            statusLabel.setText("請選擇卡牌...");
            turnLabel.setVisible(false);
        } else if (!peerCardSelected) {
            statusLabel.setText("等待另一位玩家選擇卡片...");
            turnLabel.setVisible(false);
        } else {
            statusLabel.setText("遊戲開始！");
            updateTurnLabel();
            turnLabel.setVisible(true);
        }
    }

    private void setupCardSelectionArea(HBox cardBox) {
        cardSlots.clear();
        for (int i = 0; i < CARD_COUNT; i++) {
            StackPane backCard = createBackCard();
            cardSlots.add(backCard);
            cardBox.getChildren().add(backCard);
        }
    }

    private StackPane createBackCard() {
        ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/card_back.png")));
        backImage.setFitWidth(150);
        backImage.setFitHeight(150);

        StackPane card = new StackPane(backImage);
        card.setOnMouseClicked(e -> {
            if (!cardSelected) {
                cardSelected = true;

                leftGrid = new Label[SIZE][SIZE];
                leftMarked = new boolean[SIZE][SIZE];
                List<Integer> numbers = generateShuffledNumbers();

                StackPane bingoCard = createBingoCardWithNumbers(leftGrid, numbers);
                card.getChildren().setAll(bingoCard);

                for (StackPane c : cardSlots) {
                    c.setVisible(false);
                }
                card.setVisible(true);
                leftOverlay.getChildren().setAll(card);
                boxLabel.setText("?");
                calledNumbers.clear();

                if (isOnline) {
                    String cardData = String.join(",", numbers.stream().map(String::valueOf).toArray(String[]::new));
                    netManager.sendMessage("CARD:" + cardData);
                    netManager.sendMessage("CARD_SELECTED");
                }
                checkGameStartReady();
            }
        });
        return card;
    }

    private void resetGame(HBox cardBox) {
        cardBox.getChildren().clear();
        cardSelected = false;
        peerCardSelected = false;
        calledNumbers.clear();
        gameOver = false;
        isMyTurn = !lastGameIWon;
        boxLabel.setText("?");
        statusLabel.setText("請選擇卡牌...");
        turnLabel.setVisible(false); // ✅ 隱藏回合提示
        updateTurnLabel();
        setupCardSelectionArea(cardBox);
        leftOverlay.getChildren().clear();
    }

    private StackPane createMysteryBox() {
        Image boxImage = new Image(getClass().getResourceAsStream("/box.png"));
        ImageView imageView = new ImageView(boxImage);
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);

        boxLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: black;");

        VBox boxContent = new VBox(10, boxLabel, imageView);
        boxContent.setAlignment(Pos.CENTER);

        StackPane box = new StackPane(boxContent);
        box.setAlignment(Pos.CENTER);

        return box;
    }

    private StackPane createBingoCardWithNumbers(Label[][] grid, List<Integer> numbers) {
        ImageView background = new ImageView(new Image(getClass().getResourceAsStream("/bingo_card.png")));
        background.setFitWidth(300);
        background.setFitHeight(300);

        GridPane numberGrid = new GridPane();
        numberGrid.setAlignment(Pos.CENTER);
        numberGrid.setHgap(0);
        numberGrid.setVgap(0);
        numberGrid.setPadding(new Insets(35));

        int index = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int number = numbers.get(index++);
                Label label = new Label(String.format("%2d", number));
                label.setMinSize(45, 45);
                label.setAlignment(Pos.CENTER);
                label.setStyle("-fx-font-size: 18px; -fx-text-fill: black; -fx-font-weight: bold;");
                grid[row][col] = label;
                numberGrid.add(label, col, row);

                // ✅ 加入點擊事件
                label.setOnMouseClicked(e -> {
                    if (!cardSelected || !isMyTurn || gameOver) {
                        return;
                    }

                    int selectedNumber = Integer.parseInt(label.getText().trim());
                    if (calledNumbers.contains(selectedNumber)) {
                        boxLabel.setText("重複");
                        return;
                    }

                    calledNumbers.add(selectedNumber);
                    boxLabel.setText(String.valueOf(selectedNumber));

                    highlightAndMark(leftGrid, leftMarked, selectedNumber);

                    if (checkBingoCount(leftMarked) >= 3 && !gameOver) {
                        gameOver = true;
                        showBingoMessage(leftOverlay);
                        if (isOnline) {
                            netManager.sendMessage("WIN");
                        }
                    }

                    if (isOnline) {
                        isMyTurn = false;
                        netManager.sendMessage("CALL:" + selectedNumber);
                        netManager.sendMessage("TURN");
                        updateTurnLabel();
                    }
                });
            }
        }

        return new StackPane(background, numberGrid);
    }

    private void highlightAndMark(Label[][] grid, boolean[][] marked, int number) {
        String numberStr = String.format("%2d", number);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j].getText().equals(numberStr)) {
                    marked[i][j] = true;
                    grid[i][j].setStyle(grid[i][j].getStyle()
                            .replaceAll("-fx-background-color: #[0-9A-Fa-f]+;", "")
                            + "-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 100px;");
                }
            }
        }
    }

    private int checkBingoCount(boolean[][] marked) {
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            boolean row = true, col = true;
            for (int j = 0; j < SIZE; j++) {
                if (!marked[i][j]) {
                    row = false;
                }
                if (!marked[j][i]) {
                    col = false;
                }
            }
            if (row) {
                count++;
            }
            if (col) {
                count++;
            }
        }

        boolean diag1 = true, diag2 = true;
        for (int i = 0; i < SIZE; i++) {
            if (!marked[i][i]) {
                diag1 = false;
            }
            if (!marked[i][SIZE - 1 - i]) {
                diag2 = false;
            }
        }
        if (diag1) {
            count++;
        }
        if (diag2) {
            count++;
        }

        return count;
    }

    private List<Integer> generateShuffledNumbers() {
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            nums.add(i);
        }
        Collections.shuffle(nums);
        return nums.subList(0, SIZE * SIZE);
    }

    private void showBingoMessage(StackPane overlay) {
        lastGameIWon = true;
        Label bingoLabel = new Label("Bingo!");
        bingoLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: red; -fx-font-weight: bold;");
        bingoLabel.setAlignment(Pos.CENTER);

        ScaleTransition st = new ScaleTransition(Duration.millis(800), bingoLabel);
        st.setFromX(0.5);
        st.setFromY(0.5);
        st.setToX(1.5);
        st.setToY(1.5);
        st.setCycleCount(4);
        st.setAutoReverse(true);
        st.play();

        overlay.getChildren().add(bingoLabel);
    }

    private void showLoseMessage(StackPane overlay) {
        lastGameIWon = false;
        Label loseLabel = new Label("Lose...");
        loseLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: gray; -fx-font-weight: bold;");
        loseLabel.setAlignment(Pos.CENTER);
        overlay.getChildren().add(loseLabel);
    }

    private void showPeerExitMessage(StackPane overlay) {
        Label exitLabel = new Label("對方已退出遊戲");
        exitLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #555; -fx-font-weight: bold;");
        exitLabel.setAlignment(Pos.CENTER);
        overlay.getChildren().add(exitLabel);
    }

    public void launchGame(Stage stage) {
        try {
            start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
