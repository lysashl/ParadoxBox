package ig;

import javafx.application.Application;
import javafx.stage.Stage;

public class Game extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sokoban");
        primaryStage.setResizable(false);
        Window.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
