package FinalProject;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class BingoLobby extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 背景圖片
        Image bgImage = new Image(getClass().getResourceAsStream("/lobby_logo.png"));
        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );

        StackPane root = new StackPane();
        root.setBackground(new Background(backgroundImage));

        // 選單 VBox（置中）
        VBox menu = new VBox(30);
        menu.setAlignment(Pos.CENTER);

        // 單人按鈕
        Button singlePlay = new Button("🎮 單人遊戲");
        singlePlay.setPrefSize(250, 60);
        singlePlay.setStyle("-fx-font-size: 20px; -fx-background-color: #FFFFFF; -fx-text-fill: black; -fx-font-weight: bold;");
        singlePlay.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            try {
                new BingoGame().start(stage);  // 在原視窗載入新畫面
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 聯機按鈕
        Button multiPlay = new Button("🌐 聯機對戰");
        multiPlay.setPrefSize(250, 60);
        multiPlay.setStyle("-fx-font-size: 20px; -fx-background-color: #FFFFFF; -fx-text-fill: black; -fx-font-weight: bold;");
        multiPlay.setOnAction(e -> {
            Stage lobbyStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            lobbyStage.close();  // 關閉大廳

            Stage onlineStage = new Stage();
            new OnlineModeSelect().start(onlineStage);  // 打開 Host/Join 選擇畫面
        });

        // 退出遊戲按鈕
        Button exitButton = new Button("🔚 退出遊戲");
        exitButton.setPrefSize(250, 60);
        exitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #FFFFFF; -fx-text-fill: black; -fx-font-weight: bold;");
        exitButton.setOnAction(e -> {
            primaryStage.close(); // 關閉主視窗
        });

        // 將按鈕加入選單
        menu.getChildren().addAll(singlePlay, multiPlay, exitButton);
        root.getChildren().add(menu);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Bingo 遊戲大廳");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}