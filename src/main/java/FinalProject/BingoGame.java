// 單人版 Bingo：玩家 vs 電腦
package FinalProject;

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

public class BingoGame extends Application {

    private static final int SIZE = 5;

    private Label[][] playerGrid, aiGrid;
    private boolean[][] playerMarked, aiMarked;
    private final Set<Integer> calledNumbers = new HashSet<>();
    private final StackPane playerOverlay = new StackPane();
    private final StackPane aiOverlay = new StackPane();
    private final Label boxLabel = new Label("?");
    private final Label statusLabel = new Label("請選擇數字...");
    private boolean gameOver = false;
    private boolean isPlayerTurn = true;

    private List<Integer> aiAvailableNumbers;

    @Override
    public void start(Stage primaryStage) {
        playerGrid = new Label[SIZE][SIZE];
        aiGrid = new Label[SIZE][SIZE];
        playerMarked = new boolean[SIZE][SIZE];
        aiMarked = new boolean[SIZE][SIZE];

        List<Integer> playerNums = generateShuffledNumbers();
        List<Integer> aiNums = generateShuffledNumbers();
        aiAvailableNumbers = new ArrayList<>(aiNums);

        StackPane playerCard = createBingoCard("你", playerGrid, playerNums, true);
        StackPane aiCard = createBingoCard("電腦", aiGrid, aiNums, false);

        boxLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: black;");
        VBox centerBox = new VBox(10, boxLabel);
        centerBox.setAlignment(Pos.CENTER);

        HBox cardsBox = new HBox(50, playerOverlay, aiOverlay);
        cardsBox.setAlignment(Pos.CENTER);

        Button reset = new Button("重新開始");
        reset.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        reset.setOnAction(e -> resetGame(primaryStage));

        Button exit = new Button("回到大廳");  
        exit.setStyle("-fx-font-size: 20px; -fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        exit.setOnAction(e -> {
            Stage currentStage = (Stage) ((Button) e.getSource()).getScene().getWindow();
            currentStage.close(); // 關閉目前視窗

            Stage lobbyStage = new Stage();
            new BingoLobby().start(lobbyStage); // 回到遊戲大廳
        });

        StackPane mysteryBox = createMysteryBox();
        HBox buttonBox = new HBox(20, reset, exit);
        buttonBox.setAlignment(Pos.CENTER);
        VBox root = new VBox(20, statusLabel, cardsBox, centerBox, mysteryBox, buttonBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));

        root.setBackground(new Background(new BackgroundImage(
                new Image(getClass().getResource("/gamebackground.jpeg").toExternalForm()),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, new BackgroundSize(100, 100, true, true, true, false)
        )));

        Scene scene = new Scene(root, 750, 1000);
        primaryStage.setTitle("Bingo Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private StackPane createBingoCard(String title, Label[][] grid, List<Integer> numbers, boolean isPlayer) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #333;");

        ImageView background = new ImageView(new Image(getClass().getResourceAsStream("/bingo_card.png")));
        background.setFitWidth(300);
        background.setFitHeight(300);

        GridPane numberGrid = new GridPane();
        numberGrid.setAlignment(Pos.CENTER);
        numberGrid.setHgap(0);
        numberGrid.setVgap(0);
        numberGrid.setPadding(new Insets(35, 20, 35, 20));

        int index = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int number = numbers.get(index++);
                Label label = new Label(String.format("%2d", number));
                label.setMinSize(45, 45);
                label.setAlignment(Pos.CENTER);
                label.setStyle("-fx-font-size: 18px; -fx-text-fill: black; -fx-font-weight: bold;");
                grid[i][j] = label;
                numberGrid.add(label, j, i);

                if (isPlayer) {
                    int finalNumber = number;
                    label.setOnMouseClicked(e -> {
                        if (!isPlayerTurn || gameOver || calledNumbers.contains(finalNumber)) {
                            return;
                        }
                        handleCall(finalNumber);
                    });
                }
            }
        }

        StackPane cardWithBackground = new StackPane(background, numberGrid);
        VBox cardBox = new VBox(10, titleLabel, cardWithBackground);
        cardBox.setAlignment(Pos.CENTER);

        StackPane wrapper = new StackPane(cardBox);
        if (isPlayer) {
            playerOverlay.getChildren().setAll(wrapper);
        } else {
            aiOverlay.getChildren().setAll(wrapper);
        }

        return wrapper;
    }

    private void handleCall(int number) {
        calledNumbers.add(number);
        boxLabel.setText(String.valueOf(number));
        highlight(playerGrid, playerMarked, number);
        highlight(aiGrid, aiMarked, number);
        checkWin();

        if (!gameOver) {
            isPlayerTurn = false;
            statusLabel.setText("電腦思考中...");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> aiTurn());
                }
            }, 1000);
        }
    }

    private void aiTurn() {
        int number;
        do {
            if (aiAvailableNumbers.isEmpty()) {
                return;
            }
            number = aiAvailableNumbers.remove(0);
        } while (calledNumbers.contains(number));

        calledNumbers.add(number);
        boxLabel.setText(String.valueOf(number));
        highlight(playerGrid, playerMarked, number);
        highlight(aiGrid, aiMarked, number);
        checkWin();

        if (!gameOver) {
            isPlayerTurn = true;
            statusLabel.setText("請選擇數字...");
        }
    }

    private void highlight(Label[][] grid, boolean[][] marked, int number) {
        String numStr = String.format("%2d", number);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j].getText().equals(numStr)) {
                    marked[i][j] = true;
                    grid[i][j].setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 100px;");
                }
            }
        }
    }

    private void checkWin() {
        if (bingoCount(playerMarked) >= 3) {
            gameOver = true;
            statusLabel.setText("你贏了！");
        } else if (bingoCount(aiMarked) >= 3) {
            gameOver = true;
            statusLabel.setText("電腦獲勝！");
        }
    }

    private int bingoCount(boolean[][] marked) {
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
        boolean d1 = true, d2 = true;
        for (int i = 0; i < SIZE; i++) {
            if (!marked[i][i]) {
                d1 = false;
            }
            if (!marked[i][SIZE - 1 - i]) {
                d2 = false;
            }
        }
        if (d1) {
            count++;
        }
        if (d2) {
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

    private void resetGame(Stage stage) {
        BingoGame newGame = new BingoGame();
        newGame.start(stage);
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

    private Integer findSmartChoice(Label[][] grid, boolean[][] marked) {
        // 行、列、對角線檢查
        for (int i = 0; i < SIZE; i++) {
            // 檢查第 i 行
            int markedCount = 0;
            Integer unmarkedNum = null;
            for (int j = 0; j < SIZE; j++) {
                if (marked[i][j]) {
                    markedCount++;
                } else {
                    int num = Integer.parseInt(grid[i][j].getText().trim());
                    if (!calledNumbers.contains(num)) {
                        unmarkedNum = num;
                    }
                }
            }
            if (markedCount == SIZE - 1 && unmarkedNum != null) {
                return unmarkedNum;
            }

            // 檢查第 i 列
            markedCount = 0;
            unmarkedNum = null;
            for (int j = 0; j < SIZE; j++) {
                if (marked[j][i]) {
                    markedCount++;
                } else {
                    int num = Integer.parseInt(grid[j][i].getText().trim());
                    if (!calledNumbers.contains(num)) {
                        unmarkedNum = num;
                    }
                }
            }
            if (markedCount == SIZE - 1 && unmarkedNum != null) {
                return unmarkedNum;
            }
        }

        // 左上到右下對角線
        int markedCount = 0;
        Integer unmarkedNum = null;
        for (int i = 0; i < SIZE; i++) {
            if (marked[i][i]) {
                markedCount++;
            } else {
                int num = Integer.parseInt(grid[i][i].getText().trim());
                if (!calledNumbers.contains(num)) {
                    unmarkedNum = num;
                }
            }
        }
        if (markedCount == SIZE - 1 && unmarkedNum != null) {
            return unmarkedNum;
        }

        // 右上到左下對角線
        markedCount = 0;
        unmarkedNum = null;
        for (int i = 0; i < SIZE; i++) {
            if (marked[i][SIZE - 1 - i]) {
                markedCount++;
            } else {
                int num = Integer.parseInt(grid[i][SIZE - 1 - i].getText().trim());
                if (!calledNumbers.contains(num)) {
                    unmarkedNum = num;
                }
            }
        }
        if (markedCount == SIZE - 1 && unmarkedNum != null) {
            return unmarkedNum;
        }

        return null; // 沒有明顯的連線機會
    }
}
