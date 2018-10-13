package ui;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MyApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = (Parent) FXMLLoader.load(getClass().getResource("home/layout_home.fxml"));
        Scene scene = new Scene(root);
        
        
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
            System.exit(0);
        });
        
        primaryStage.setTitle("Digital Iamge Watermarking");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
