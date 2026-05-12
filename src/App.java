import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        Button btn = new Button("Click Me");

        StackPane root = new StackPane(btn);

        Scene scene = new Scene(root, 800, 500);

        scene.getStylesheets().add(
            getClass().getResource("/styles/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Test CSS");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}