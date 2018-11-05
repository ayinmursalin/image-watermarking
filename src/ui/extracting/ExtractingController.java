package ui.extracting;

import core.helper.SimilarityPercentage;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import core.Watermarker;
import core.helper.ImageUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
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
import javax.imageio.ImageIO;
import ui.embedding.EmbeddingController;

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
    private ImageView ivPreviewExtractedWatermark;
    @FXML
    private ImageView ivPreviewOriginalWatermark;
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
    private Label labelSimilarityPercentage;
    @FXML
    private JFXButton btnSaveEmbeddedImage;
    @FXML
    private JFXButton btnChooseOriginalWatermark;
    @FXML
    private JFXButton btnCalculatePercentage;
    @FXML
    private JFXProgressBar pbExtract;

    private Text messageHeader, messageBody;
    private JFXDialogLayout dialogLayout;
    private JFXDialog dialog;

    private Watermarker watermarker;
    private SimilarityPercentage similarityPercentage;
    private Image embeddedImage, extractedWatermarkImage, originalWatermarkImage;

    private String fileName = "";
    private File savedDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        watermarker = new Watermarker();
        similarityPercentage = new SimilarityPercentage();

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

        // dialog
        messageHeader = new Text("");
        messageBody = new Text("");

        dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(messageHeader);
        dialogLayout.setBody(messageBody);
        dialog = new JFXDialog(stackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);

        // save dir init
        savedDir = new File("D:\\_watermarking\\saved\\3_extracting");
    }

    @FXML
    void onChooseEmbeddedImage(ActionEvent event) {
        paneOutput.setVisible(false);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra Penampung");
            fileChooser.setInitialDirectory(new File("D:\\_watermarking\\saved"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));

            File imageFile = fileChooser.showOpenDialog(new Stage());
            Image image = ImageUtil.fileToImage(imageFile);

            this.fileName = imageFile.getName();
            this.fileName = fileName.substring(0, fileName.indexOf('.'));
            this.fileName = "watermark_" + fileName;

            int imageHeight = image.heightProperty().intValue();
            int imageWidth = image.widthProperty().intValue();

            if (imageHeight == imageWidth) {
                if (imageWidth >= 512 || imageHeight >= 512) {
                    this.embeddedImage = image;

                    ivPreviewImageContainer.setImage(embeddedImage);
                } else {
                    messageHeader.setText("Ukuran Citra Penampung terlalu kecil");
                    messageBody.setText("Ukuran citra penampung minimal 512 x 512");

                    dialog.show();
                }
            } else {
                messageHeader.setText("Bukan Citra Persegi");
                messageBody.setText("Harap masukkan citra yang mempunyai ukuran panjang dan lebar yang sama");

                dialog.show();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not an image or not found");
        }
    }

    @FXML
    void onExtractWatermark(ActionEvent event) {
        try {
            int seed1 = (int) Integer.parseInt(tfSeed1.getText());
            int seed2 = (int) Integer.parseInt(tfSeed2.getText());

            if (embeddedImage == null) {
                messageHeader.setText("Citra Penampung Kosong");
                messageBody.setText("Citra penampung belum dipilih, harap pilih citra penampung yang sudah dilakukan proses embedding watermark terlebih dahulu");

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

                    ivPreviewExtractedWatermark.setImage(extractedWatermarkImage);
                });

                new Thread(extractTask).start();
            }
        } catch (NumberFormatException e) {
            messageHeader.setText("Belum memasukkan Key 1 atau Key 2");
            messageBody.setText("Belum memasukkan key 1 atau 2, harap isi terlebih dahulu key 1 dan key 2");

            dialog.show();
        }
    }

    @FXML
    void onSaveExtractedWatermark(ActionEvent event) {
        decideSaveLocation();

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Simpan Citra Tanda Air Hasil Ekstraksi");
            fileChooser.setInitialDirectory(savedDir);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
            fileChooser.setInitialFileName(fileName);

            File file = fileChooser.showSaveDialog(new Stage());

            if (file != null) {
                BufferedImage image = SwingFXUtils.fromFXImage(extractedWatermarkImage, null);
                ImageIO.write(image, "png", file);
            }
        } catch (IOException ex) {
            Logger.getLogger(EmbeddingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void onChooseOriginalWatermark(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra Tanda Air");
            fileChooser.setInitialDirectory(new File("D:\\_watermarking\\source\\watermark"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));

            File imageFile = fileChooser.showOpenDialog(new Stage());
            Image image = ImageUtil.fileToImage(imageFile);

            int imageHeight = image.heightProperty().intValue();
            int imageWidth = image.widthProperty().intValue();

            if (imageHeight == 32 || imageWidth == 32) {
                this.originalWatermarkImage = image;

                ivPreviewOriginalWatermark.setImage(originalWatermarkImage);
            } else {
                dialogLayout.setHeading(new Text("Citra Tanda Air tidak Sesuai"));
                dialogLayout.setBody(new Text("Ukuran citra tanda air harus 32x32"));

                dialog.show();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not an image or not found");
        }
    }

    @FXML
    void onCalculatePercentage(ActionEvent event) {
        if (originalWatermarkImage == null) {
            dialogLayout.setHeading(new Text("Citra Tanda Air Asli Tidak Ada"));
            dialogLayout.setBody(new Text("Belum memilih citra tanda air asli, "
                    + "harap masukkan citra tanda air asli terlebih dahulu sebelum proses perhitungan"));
            dialog.show();
        } else {
            double percentage = similarityPercentage
                    .getSimilarityPercentage(extractedWatermarkImage, originalWatermarkImage);

            String percentageStr = String.format("%.0f", percentage);
            labelSimilarityPercentage.setText("Persentase Kesamaan : " + percentageStr + "%");
        }
    }

    private void decideSaveLocation() {
        int containerHeight = embeddedImage.heightProperty().intValue();
        int containerWidth = embeddedImage.widthProperty().intValue();

        if ((containerHeight >= 512 && containerHeight < 1024)
                && (containerWidth >= 512 && containerWidth < 1024)) {
            savedDir = new File("D:\\_watermarking\\saved\\3_extracting\\small");
        } else if ((containerHeight >= 1024 && containerHeight < 2048)
                && (containerWidth >= 1024 && containerWidth < 2048)) {
            savedDir = new File("D:\\_watermarking\\saved\\3_extracting\\medium");
        } else if (containerHeight >= 2048 && containerWidth >= 2048) {
            savedDir = new File("D:\\_watermarking\\saved\\3_extracting\\large");
        }
    }

}
