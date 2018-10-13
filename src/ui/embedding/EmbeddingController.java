package ui.embedding;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class EmbeddingController implements Initializable {

    @FXML
    private StackPane stackPane;

    @FXML
    private Pane paneBackgroundLeft;

    @FXML
    private JFXButton btnChooseImageContainer;

    @FXML
    private ImageView ivPreviewImageContainer;

    @FXML
    private Pane paneBackgroundRight;

    @FXML
    private JFXButton btnChooseWatermark;

    @FXML
    private ImageView ivPreviewWatermark;

    @FXML
    private JFXTextField tfSeed1;

    @FXML
    private JFXTextField tfSeed2;

    @FXML
    private JFXButton btnEmbedd;

    @FXML
    private Pane paneOutput;

    @FXML
    private Label labelPSNR;

    @FXML
    private JFXButton btnSaveImage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

}
