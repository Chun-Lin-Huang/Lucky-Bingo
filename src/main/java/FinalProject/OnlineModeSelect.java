package FinalProject;

import FinalProject.network.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class OnlineModeSelect extends Application {

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(100));
        
        Image bgImage = new Image(getClass().getResourceAsStream("/host_back.png"));
        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        root.setBackground(new Background(backgroundImage));

        Button hostButton = new Button("建立房間（Host）");
        hostButton.setPrefSize(250, 60);
        hostButton.setStyle("-fx-font-size: 20px; -fx-background-color: #e6e6fa; -fx-text-fill: black;");
        hostButton.setOnAction(e -> {
            stage.close();
            new HostRoomUI().start(new Stage()); // 開啟房主伺服器畫面
        });

        Button joinButton = new Button("加入房間（Join）");
        joinButton.setPrefSize(250, 60);
        joinButton.setStyle("-fx-font-size: 20px; -fx-background-color: #b0c4de; -fx-text-fill: black;");
        joinButton.setOnAction(e -> {
            stage.close();
            new JoinRoomUI().start(new Stage()); // 開啟加入畫面
        });

        root.getChildren().addAll(hostButton, joinButton);
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("選擇聯機模式");
        stage.setScene(scene);
        stage.show();
    }
}