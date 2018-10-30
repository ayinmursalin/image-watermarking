package ui.embedding;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import core.Watermarker;
import core.helper.ImageUtil;
import core.helper.PeakSignalNoiseRation;
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
    @FXML
    private JFXProgressBar pbEmbedd;

    Text messageHeader, messageBody;
    private JFXDialogLayout dialogLayout;
    private JFXDialog dialog;

    private Watermarker watermarker;
    private Image containerImage, watermarkImage, embeddedImage;

    private String fileName = "";
    private File savedDir;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        watermarker = new Watermarker();

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
        messageHeader = new Text();
        messageBody = new Text();

        dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(messageHeader);
        dialogLayout.setBody(messageBody);
        dialog = new JFXDialog(stackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);

        // save dir init
        savedDir = new File("D:\\_watermarking\\saved\\2_embedding");
    }

    @FXML
    void onChooseContainerImage(ActionEvent event) {
        paneOutput.setVisible(false);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra Penampung");
            fileChooser.setInitialDirectory(new File("D:\\_watermarking\\saved\\1_greyscaling"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));

            File imageFile = fileChooser.showOpenDialog(new Stage());
            Image image = ImageUtil.fileToImage(imageFile);

            this.fileName = imageFile.getName();
            this.fileName = fileName.substring(0, fileName.indexOf('.'));
            this.fileName = "embedded_" + fileName;

            int imageHeight = image.heightProperty().intValue();
            int imageWidth = image.widthProperty().intValue();

            if (imageHeight == imageWidth) {
                if (imageWidth >= 512 || imageHeight >= 512) {
                    this.containerImage = image;

                    ivPreviewImageContainer.setImage(containerImage);
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
    void onChooseWatermarkImage(ActionEvent event) {
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
                this.watermarkImage = image;

                ivPreviewWatermark.setImage(watermarkImage);
            } else {
                messageHeader.setText("Citra Tanda Air tidak Sesuai");
                messageBody.setText("Ukuran citra tanda air harus berukuran 32x32");

                dialog.show();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not an image or not found");
        }
    }

    @FXML
    void onEmbeddWatemark(ActionEvent event) {
        try {
            int seed1 = (int) Integer.parseInt(tfSeed1.getText());
            int seed2 = (int) Integer.parseInt(tfSeed2.getText());

            if (containerImage == null) {
                messageHeader.setText("Citra Penampung Kosong");
                messageBody.setText("Citra penampung belum dipilih, harap pilih citra penampung terlebih dahulu");

                dialog.show();
            } else if (watermarkImage == null) {
                messageHeader.setText("Citra Tanda Air Kosong");
                messageBody.setText("Citra tanda air belum dipilih, harap pilih citra tanda air terlebih dahulu");

                dialog.show();
            } else {
                messageHeader.setText("Informasi Penting");
                messageBody.setText("Harap diingat seed1 dan seed2 yang anda masukkan, untuk digunakan lagi saat proses ekstraksi tanda air.");

                dialog.show();

                // calculate PSNR
                Task<Double> calculatePsnrTask = new Task<Double>() {
                    @Override
                    protected Double call() throws Exception {
                        pbEmbedd.setVisible(true);

                        PeakSignalNoiseRation psnr = new PeakSignalNoiseRation(containerImage, embeddedImage);

                        return psnr.getPsnrValue();
                    }
                };
                calculatePsnrTask.setOnSucceeded((WorkerStateEvent event1) -> {
                    pbEmbedd.setVisible(false);
                    paneOutput.setVisible(true);

                    double psnr = calculatePsnrTask.getValue();

                    labelPSNR.setText("PSNR : " + psnr);
                });

                // embeddWatermark
                Task<Image> embeddTask = new Task<Image>() {
                    @Override
                    protected Image call() throws Exception {
                        pbEmbedd.setVisible(true);
                        // run in background thread
                        return watermarker.embeddWatermark(containerImage, watermarkImage, seed1, seed2);
                    }
                };
                embeddTask.setOnSucceeded((WorkerStateEvent event1) -> {
                    pbEmbedd.setVisible(false);
                    dialog.close();
                    this.embeddedImage = embeddTask.getValue();

                    ivPreviewImageContainer.setImage(embeddedImage);

                    // start Calculate PSNR task after finisihing embedd image
                    new Thread(calculatePsnrTask).start();
                });

                new Thread(embeddTask).start();
            }
        } catch (NumberFormatException e) {
            messageHeader.setText("Belum memasukkan Key 1 atau Key 2");
            messageBody.setText("Belum memasukkan key 1 atau 2, harap isi terlebih dahulu key 1 dan key 2");

            dialog.show();
        }
    }

    @FXML
    void onSaveEmebeddedImage(ActionEvent event) {
        decideSaveLocation();

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Simpan Citra Baru");
            fileChooser.setInitialDirectory(savedDir);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
            fileChooser.setInitialFileName(fileName);

            File file = fileChooser.showSaveDialog(new Stage());

            if (file != null) {
                BufferedImage image = SwingFXUtils.fromFXImage(embeddedImage, null);
                ImageIO.write(image, "png", file);
            }
        } catch (IOException ex) {
            Logger.getLogger(EmbeddingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void decideSaveLocation() {
        int containerHeight = containerImage.heightProperty().intValue();
        int containerWidth = containerImage.widthProperty().intValue();

        if ((containerHeight >= 512 && containerHeight < 1024)
                && (containerWidth >= 512 && containerWidth < 1024)) {
            savedDir = new File("D:\\_watermarking\\saved\\2_embedding\\small");
        } else if ((containerHeight >= 1024 && containerHeight < 2048)
                && (containerWidth >= 1024 && containerWidth < 2048)) {
            savedDir = new File("D:\\_watermarking\\saved\\2_embedding\\medium");
        } else if (containerHeight >= 2048 && containerWidth >= 2048) {
            savedDir = new File("D:\\_watermarking\\saved\\2_embedding\\large");
        }
    }

}
