package ui.home;

import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeController implements Initializable {

    @FXML
    private JFXButton btnEmbedding;
    @FXML
    private JFXButton btnExtracting;
    @FXML
    private JFXButton btnGeometryAttacks;
    @FXML
    private JFXButton btnRemovalAttacks;
    @FXML
    private JFXButton btnGreyscaling;

    Stage greyscaling;
    Stage embeddingStage, extractingStage;
    Stage removalAttackStage, geometricAttackStage;

    // on initialize
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        greyscaling = new Stage();
        embeddingStage = new Stage();
        extractingStage = new Stage();
        removalAttackStage = new Stage();
        geometricAttackStage = new Stage();
    }

    @FXML
    void onGreyscaling(ActionEvent event) throws IOException {
        Parent root = (Parent) FXMLLoader.load(getClass().getResource("/ui/greyscaling/layout_greyscaling.fxml"));
        openNewWindow(greyscaling, root, "Greyscaling Process");
    }

    @FXML
    void onEmbedd(ActionEvent event) throws IOException {
        Parent root = (Parent) FXMLLoader.load(getClass().getResource("/ui/embedding/layout_embedding.fxml"));
        openNewWindow(embeddingStage, root, "Embedding Process");
    }

    @FXML
    void onExtract(ActionEvent event) throws IOException {
        Parent root = (Parent) FXMLLoader.load(getClass().getResource("/ui/extracting/layout_extracting.fxml"));
        openNewWindow(extractingStage, root, "Extracting Process");
    }

    @FXML
    void onGeometryAttacks(ActionEvent event) throws IOException {
        Parent root = (Parent) FXMLLoader.load(getClass().getResource("/ui/geometricattacks/layout_geometric.fxml"));
        openNewWindow(geometricAttackStage, root, "Geometric Attacks");
    }

    @FXML
    void onRemovalAttacks(ActionEvent event) throws IOException {
        Parent root = (Parent) FXMLLoader.load(getClass().getResource("/ui/removalattacks/layout_removal.fxml"));
        openNewWindow(removalAttackStage, root, "Removal Attacks");
    }

    private void openNewWindow(Stage stage, Parent root, String title) {
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.setMaximized(true);
        stage.show();
    }

}
