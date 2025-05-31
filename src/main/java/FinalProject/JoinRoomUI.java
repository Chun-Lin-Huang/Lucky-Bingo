package FinalProject;

import FinalProject.network.NetworkGameManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class JoinRoomUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        
        Image bgImage = new Image(getClass().getResourceAsStream("/host_back.png"));
        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        root.setBackground(new Background(backgroundImage));

        Label prompt = new Label("請輸入主機 IP 與 Port");
        TextField inputField = new TextField("127.0.0.1:9999");
        inputField.setPrefWidth(250);

        Label status = new Label();

        Button connectButton = new Button("連線");
        connectButton.setPrefWidth(200);
        connectButton.setStyle("-fx-font-size: 18px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        connectButton.setOnAction(e -> {
            String ipAndPort = inputField.getText().trim();
            status.setText("連線中...");
            new Thread(() -> {
                try {
                    NetworkGameManager netManager = new NetworkGameManager(false);
                    netManager.startConnection(ipAndPort);  // 連線主機

                    Platform.runLater(() -> {
                        primaryStage.close();
                        BingoOnlineGame game = new BingoOnlineGame(true, false, netManager);
                        game.launchGame(new Stage());
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> status.setText("❌ 連線失敗，請確認 IP 與 Port"));
                }
            }).start();
        });

        root.getChildren().addAll(prompt, inputField, connectButton, status);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("加入房間");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}