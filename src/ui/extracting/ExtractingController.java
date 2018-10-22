package ui.extracting;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import core.Watermarker;
import core.transform.TransformUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ExtractingController implements Initializable {

    @FXML
    private StackPane stackPane;

    @FXML
    private Pane paneBackgroundLeft;

    @FXML
    private JFXButton btnChooseEmbeddedImage;

    @FXML
    private ImageView ivPreviewImageContainer;

    @FXML
    private Pane paneBackgroundRight;

    @FXML
    private ImageView ivPreviewWatermark;

    @FXML
    private JFXButton btnSaveWatermark;

    @FXML
    private JFXTextField tfSeed1;

    @FXML
    private JFXTextField tfSeed2;

    @FXML
    private JFXButton btnExtract;

    @FXML
    private Pane paneOutput;

    @FXML
    private Label labelPSNR;

    @FXML
    private JFXButton btnSaveEmbeddedImage;

    @FXML
    private JFXProgressBar pbExtract;

    private JFXDialogLayout dialogLayout;
    private JFXDialog dialog;

    private Watermarker watermarker;
    private Image embeddedImage, extractedWatermarkImage;
    private String fileName = "";
    private int seed1 = -1, seed2 = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        watermarker = new Watermarker();

        dialogLayout = new JFXDialogLayout();
        dialog = new JFXDialog(stackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);

        tfSeed1.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d{0,7}")) {
                tfSeed1.setText(oldValue);
            }
        });
        tfSeed2.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d{0,7}")) {
                tfSeed2.setText(oldValue);
            }
        });
    }

    @FXML
    void onChooseEmbeddedImage(ActionEvent event) {
        paneOutput.setVisible(false);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra Penampung");
            fileChooser.setInitialDirectory(new File("D:\\saved"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));

            File imageFile = fileChooser.showOpenDialog(new Stage());
            Image image = TransformUtil.fileToImage(imageFile);

            this.fileName = imageFile.getName();
            this.fileName = fileName.substring(0, fileName.indexOf('.'));

            int imageHeight = image.heightProperty().intValue();
            int imageWidth = image.widthProperty().intValue();

            if (imageHeight == imageWidth) {
                if (imageWidth >= 512 || imageHeight >= 512) {
                    this.embeddedImage = image;

                    ivPreviewImageContainer.setImage(embeddedImage);
                } else {
                    dialogLayout.setHeading(new Text("Ukuran Citra Penampung terlalu kecil"));
                    dialogLayout.setBody(new Text("Ukuran citra penampung minimal 512 x 512"));

                    dialog.show();
                }
            } else {
                dialogLayout.setHeading(new Text("Bukan Citra Persegi"));
                dialogLayout.setBody(new Text("Harap masukkan citra yang mempunyai ukuran panjang dan lebar yang sama"));

                dialog.show();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not an image or not found");
        }
    }

    @FXML
    void onExtractWatermark(ActionEvent event) {
        try {
            seed1 = (int) Integer.parseInt(tfSeed1.getText());
            seed2 = (int) Integer.parseInt(tfSeed2.getText());
        } catch (NumberFormatException e) {
            // error
        }

        if (embeddedImage == null) {
            dialogLayout.setHeading(new Text("Citra Penampung Kosong"));
            dialogLayout.setBody(new Text("Citra penampung belum dipilih, harap pilih citra penampung yang sudah dilakukan proses embedding watermark terlebih dahulu"));
            dialog.show();
        } else if (seed1 == -1) {
            dialogLayout.setHeading(new Text("Key 1 belum diisi"));
            dialogLayout.setBody(new Text("Belum memasukkan seed 1, harap isi terlebih dahulu seed 1"));
            dialog.show();
        } else if (seed2 == -1) {
            dialogLayout.setHeading(new Text("Key 2 belum diisi"));
            dialogLayout.setBody(new Text("Belum memasukkan seed 2, harap isi terlebih dahulu seed 2"));
            dialog.show();
        } else {
            Task<Image> extractTask = new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    pbExtract.setVisible(true);
                    // run in background
                    return watermarker.extractWatermark(embeddedImage, seed1, seed2);
                }
            };
            extractTask.setOnSucceeded((WorkerStateEvent event1) -> {
                pbExtract.setVisible(false);
                paneOutput.setVisible(true);
                
                this.extractedWatermarkImage = extractTask.getValue();
                
                ivPreviewWatermark.setImage(extractedWatermarkImage);
            });
            
            new Thread(extractTask).start();
        }
    }

    @FXML
    void onSaveExtractedWatermark(ActionEvent event) {

    }

}
