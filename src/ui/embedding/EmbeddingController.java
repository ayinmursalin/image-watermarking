package ui.embedding;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import core.Watermarker;
import core.transform.TransformUtil;
import core.helper.PeakSignalNoiseRation;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
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
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

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

    private JFXDialogLayout dialogLayout;
    private JFXDialog dialog;

    private Watermarker watermarker;
    private Image containerImage, watermarkImage, embeddedImage;
    private String fileName = "";
    private int seed1 = -1, seed2 = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
    void onChooseContainerImage(ActionEvent event) {
        paneOutput.setVisible(false);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra Penampung");
            fileChooser.setInitialDirectory(new File("D:\\coding\\netbeans\\Watermarking\\src\\images"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));

            File imageFile = fileChooser.showOpenDialog(new Stage());
            Image image = TransformUtil.fileToImage(imageFile);

            this.fileName = imageFile.getName();
            this.fileName = fileName.substring(0, fileName.indexOf('.'));

            int imageHeight = image.heightProperty().intValue();
            int imageWidth = image.widthProperty().intValue();

            if (imageHeight == imageWidth) {
                if (imageWidth >= 512 || imageHeight >= 512) {
                    toGreyscaleImage(image);
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
    void onChooseWatermarkImage(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra Tanda Air");
            fileChooser.setInitialDirectory(new File("D:\\coding\\netbeans\\Watermarking\\src\\images"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));

            File imageFile = fileChooser.showOpenDialog(new Stage());
            Image image = TransformUtil.fileToImage(imageFile);

            int imageHeight = image.heightProperty().intValue();
            int imageWidth = image.widthProperty().intValue();

            if (imageHeight == 32 || imageWidth == 32) {
                this.watermarkImage = image;

                ivPreviewWatermark.setImage(watermarkImage);
            } else {
                dialogLayout.setHeading(new Text("Citra Tanda Air tidak Sesuai"));
                dialogLayout.setBody(new Text("Ukuran citra tanda air harus 32x32"));

                dialog.show();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not an image or not found");
        }
    }

    private void toGreyscaleImage(Image image) {
        int height = image.heightProperty().intValue();
        int width = image.widthProperty().intValue();

        WritableImage newGreyscaleImage = new WritableImage(width, height);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color color = image.getPixelReader().getColor(col, row);
                Color greyscaleColor = color.grayscale();

                newGreyscaleImage.getPixelWriter()
                        .setColor(col, row, greyscaleColor);
            }
        }

        this.containerImage = newGreyscaleImage;

        ivPreviewImageContainer.setImage(containerImage);
    }

    @FXML
    void onEmbeddWatemark(ActionEvent event) {
        try {
            seed1 = (int) Integer.parseInt(tfSeed1.getText());
            seed2 = (int) Integer.parseInt(tfSeed2.getText());
        } catch (NumberFormatException e) {
            // error
        }

        if (containerImage == null) {
            dialogLayout.setHeading(new Text("Citra Penampung Kosong"));
            dialogLayout.setBody(new Text("Citra penampung belum dipilih, harap pilih citra penampung terlebih dahulu"));
            dialog.show();
        } else if (watermarkImage == null) {
            dialogLayout.setHeading(new Text("Citra Tanda Air Kosong"));
            dialogLayout.setBody(new Text("Citra tanda air belum dipilih, harap pilih citra tanda air terlebih dahulu"));
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
            dialogLayout.setHeading(new Text("Informasi Penting"));
            dialogLayout.setBody(new Text("Harap diingat seed1 dan seed2 yang anda masukkan, untuk digunakan lagi saat proses ekstraksi tanda air."));

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
    }

    @FXML
    void onSaveEmebeddedImage(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Simpan Citra Baru");
            fileChooser.setInitialDirectory(new File("D:\\saved"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
            fileChooser.setInitialFileName(fileName);

            File file = fileChooser.showSaveDialog(new Stage());

            if (file != null) {
                BufferedImage image = SwingFXUtils.fromFXImage(embeddedImage, null);
                ImageIO.write(image, "png", file);

                // Save as Jpg (bad quality, dont know why)
//                BufferedImage imageRgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
//                
//                Graphics2D graphics = imageRgb.createGraphics();
//                graphics.drawImage(image, 0, 0, java.awt.Color.BLACK, null);
//                
//                ImageIO.write(imageRgb, "jpg", file);
//                
//                graphics.dispose();
            }
        } catch (IOException ex) {
            Logger.getLogger(EmbeddingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private IndexColorModel getDefaultColorModel() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        for (int i = 0; i < 256; i++) {
            r[i] = (byte) i;
            g[i] = (byte) i;
            b[i] = (byte) i;
        }
        IndexColorModel defaultColorModel = new IndexColorModel(8, 256, r, g, b);
        return defaultColorModel;
    }

}
