package FinalProject;

import FinalProject.network.NetworkGameManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HostRoomUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
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

        Label statusLabel = new Label("等待玩家連線中...");

        root.getChildren().addAll(statusLabel);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("建立房間");
        primaryStage.setScene(scene);
        primaryStage.show();
        // 開始非同步等待連線
        new Thread(() -> {
            try {
                NetworkGameManager netManager = new NetworkGameManager(true);
                netManager.startConnection("9999"); // 開啟伺服器

                javafx.application.Platform.runLater(() -> {
                    primaryStage.close();
                    BingoOnlineGame game = new BingoOnlineGame(true, true, netManager);
                    game.launchGame(new Stage());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
