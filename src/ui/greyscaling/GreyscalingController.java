package ui.greyscaling;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import core.helper.ImageUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class GreyscalingController implements Initializable {

    @FXML
    private StackPane stackPane;
    @FXML
    private Pane paneBackgroundLeft;
    @FXML
    private JFXButton btnChooseImage;
    @FXML
    private ImageView ivPreviewImage;
    @FXML
    private ImageView ivPreviewGreyscaleImage;
    @FXML
    private JFXButton btnGreyscale;
    @FXML
    private JFXButton btnSaveImage;
    @FXML
    private JFXProgressBar pbGreyscale;

    private Text messageHeader, messageBody;
    private JFXDialogLayout dialogLayout;
    private JFXDialog dialog;

    private Image originalImage, greyscaleImage;

    private String fileName = "";
    private File savedDir;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // dialog
        messageHeader = new Text("");
        messageBody = new Text("");
        
        dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(messageHeader);
        dialogLayout.setBody(messageBody);
        dialog = new JFXDialog(stackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
        
        // save dir init
        savedDir = new File("D:\\_watermarking\\saved\\1_greyscaling");
    }

    @FXML
    void onChooseImage(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra");
            fileChooser.setInitialDirectory(new File("D:\\_watermarking\\source"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));

            File imageFile = fileChooser.showOpenDialog(new Stage());
            Image image = ImageUtil.fileToImage(imageFile);

            this.fileName = imageFile.getName();
            this.fileName = fileName.substring(0, fileName.indexOf('.'));

            int imageHeight = image.heightProperty().intValue();
            int imageWidth = image.widthProperty().intValue();

            if ((imageHeight == imageWidth) && (imageHeight >= 512 && imageWidth >= 512)) {
                this.originalImage = image;
                ivPreviewImage.setImage(originalImage);

                greyscaleImage = null;
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
    void onGreyscaleImage(ActionEvent event) {
        int height = originalImage.heightProperty().intValue();
        int width = originalImage.widthProperty().intValue();
        
        Task<Image> greyscaleTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                pbGreyscale.setVisible(true);
                
                WritableImage newGreyscaleImage = new WritableImage(width, height);

                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        Color color = originalImage.getPixelReader().getColor(col, row);
                        Color greyscaleColor = color.grayscale();

                        newGreyscaleImage.getPixelWriter()
                                .setColor(col, row, greyscaleColor);
                    }
                }
                
                return newGreyscaleImage;
            }
        };
        greyscaleTask.setOnSucceeded((WorkerStateEvent event1) -> {
            pbGreyscale.setVisible(false);
            
            this.greyscaleImage = greyscaleTask.getValue();

            ivPreviewGreyscaleImage.setImage(greyscaleImage);
        });
        
        new Thread(greyscaleTask).start();
    }
    
    @FXML
    void onSaveImage(ActionEvent event) {
        if (originalImage == null) {
            messageHeader.setText("Belum memasukkan citra asal");
            messageBody.setText("Harap masukkan citra yang akan diproses menjadi citra greyscale terlebih dahulu.");

            dialog.show();
        } else if (greyscaleImage == null) {
            messageHeader.setText("Belum melakukan proses greyscale image");
            messageBody.setText("Harap klik tombol Greyscale untuk menciptakan citra greyscale.");

            dialog.show();
        } else {
            this.fileName = "grey_" + this.fileName;
            decideSaveLocation();
            
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Simpan Citra Greyscale");
                fileChooser.setInitialDirectory(savedDir);
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
                fileChooser.setInitialFileName(fileName);

                File file = fileChooser.showSaveDialog(new Stage());

                if (file != null) {
                    BufferedImage image = SwingFXUtils.fromFXImage(greyscaleImage, null);
                    ImageIO.write(image, "png", file);
                }
            } catch (IOException ex) {
                System.out.println("File not an image or not found");
            }
        }
    }

    private void decideSaveLocation() {
        int containerHeight = originalImage.heightProperty().intValue();
        int containerWidth = originalImage.widthProperty().intValue();

        if ((containerHeight >= 512 && containerHeight < 1024)
                && (containerWidth >= 512 && containerWidth < 1024)) {
            savedDir = new File("D:\\_watermarking\\saved\\1_greyscaling\\small");
        } else if ((containerHeight >= 1024 && containerHeight < 2048)
                && (containerWidth >= 1024 && containerWidth < 2048)) {
            savedDir = new File("D:\\_watermarking\\saved\\1_greyscaling\\medium");
        } else if (containerHeight >= 2048 && containerWidth >= 2048) {
            savedDir = new File("D:\\_watermarking\\saved\\1_greyscaling\\large");
        }
    }

}
