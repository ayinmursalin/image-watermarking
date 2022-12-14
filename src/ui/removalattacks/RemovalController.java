package ui.removalattacks;

import core.attacks.removal.RemovalAttacker;
import core.attacks.removal.RemovalType;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXRadioButton;
import core.helper.ImageUtil;
import core.helper.PeakSignalNoiseRation;
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
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class RemovalController implements Initializable {

    @FXML
    private StackPane stackPane;
    @FXML
    private Pane paneBackgroundLeft;
    @FXML
    private JFXButton btnChooseImage;
    @FXML
    private ImageView ivPreviewImage;
    @FXML
    private Pane paneBackgroundRight;
    @FXML
    private JFXButton btnProcessAttack;
    @FXML
    private Pane paneOutput;
    @FXML
    private Label labelPSNR;
    @FXML
    private JFXButton btnSaveModifiedImage;
    @FXML
    private ToggleGroup removalattack;
    @FXML
    private JFXRadioButton rbSharpening;
    @FXML
    private JFXRadioButton rbBlurring;
    @FXML
    private JFXRadioButton rbMedianFilter;
    @FXML
    private JFXRadioButton rbNoiseAddition;
    @FXML
    private JFXProgressBar pbRemoval;

    private Text messageHeader, messageBody;
    private JFXDialogLayout dialogLayout;
    private JFXDialog dialog;

    private RemovalAttacker attacker;
    private Image originalImage, modifiedImage;
    private RemovalType type;

    private String fileName = "";
    private File savedDir;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        attacker = new RemovalAttacker();

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
        paneOutput.setVisible(false);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra");
            fileChooser.setInitialDirectory(new File("D:\\_watermarking\\saved\\2_embedding"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));

            File imageFile = fileChooser.showOpenDialog(new Stage());
            Image image = ImageUtil.fileToImage(imageFile);

            this.fileName = imageFile.getName();
            this.fileName = fileName.substring(0, fileName.indexOf('.'));

            int imageHeight = image.heightProperty().intValue();
            int imageWidth = image.widthProperty().intValue();

            if (imageHeight == imageWidth) {
                this.originalImage = image;
                ivPreviewImage.setImage(originalImage);

                modifiedImage = null;
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
    void onChooseRemovalAttack(ActionEvent event) {
        Toggle selectedRadioButton = removalattack.getSelectedToggle();

        if (selectedRadioButton == rbSharpening) {
            type = RemovalType.SHARPENING;
        } else if (selectedRadioButton == rbBlurring) {
            type = RemovalType.BLURRING;
        } else if (selectedRadioButton == rbMedianFilter) {
            type = RemovalType.MEDIAN_FILTER;
        } else if (selectedRadioButton == rbNoiseAddition) {
            type = RemovalType.NOISE_ADDITION;
        } else {
            type = null;
        }
    }

    @FXML
    void onProcessAttack(ActionEvent event) {
        if (originalImage == null) {
            messageHeader.setText("Belum Memilih Citra");
            messageBody.setText("Harap pilih citra yang akan diproses terlebih dahulu.");

            dialog.show();
        } else if (type == null) {
            messageHeader.setText("Belum Memilih Jenis Serangan");
            messageBody.setText("Harap pilih serangan yang akan dilakukan terlebih dahulu.");

            dialog.show();
        } else {
            // allow to multiple process
            Image imageToProcess = null;

            if (modifiedImage == null) {
                imageToProcess = originalImage;
            } else {
                imageToProcess = modifiedImage;
            }

            switch (type) {
                case SHARPENING:
                    sharpenImage(imageToProcess);
                    break;
                case BLURRING:
                    blurImage(imageToProcess);
                    break;
                case MEDIAN_FILTER:
                    medianFilterImage(imageToProcess);
                    break;
                case NOISE_ADDITION:
                    noiseAdditionImage(imageToProcess);
                    break;
            }
        }
    }

    private void sharpenImage(Image imageToProcess) {
        Task<Image> sharpeningTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                pbRemoval.setVisible(true);
                // run in background thread
                return attacker.sharpenImage(imageToProcess);
            }
        };
        sharpeningTask.setOnSucceeded((WorkerStateEvent event) -> {
            pbRemoval.setVisible(false);
            paneOutput.setVisible(true);

            this.modifiedImage = sharpeningTask.getValue();

            ivPreviewImage.setImage(modifiedImage);

            // calculate PSNR
            calculatePSNR();
        });

        new Thread(sharpeningTask).start();
    }

    private void blurImage(Image imageToProcess) {
        Task<Image> bluringTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                pbRemoval.setVisible(true);
                // run in background thread
                return attacker.blurImage(imageToProcess);
            }
        };
        bluringTask.setOnSucceeded((WorkerStateEvent event) -> {
            pbRemoval.setVisible(false);
            paneOutput.setVisible(true);

            this.modifiedImage = bluringTask.getValue();

            ivPreviewImage.setImage(modifiedImage);

            // calculate PSNR
            calculatePSNR();
        });

        new Thread(bluringTask).start();
    }

    private void medianFilterImage(Image imageToProcess) {
        Task<Image> medianFilterTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                pbRemoval.setVisible(true);
                // run in background thread
                return attacker.medianFilterImage(imageToProcess);
            }
        };
        medianFilterTask.setOnSucceeded((WorkerStateEvent event) -> {
            pbRemoval.setVisible(false);
            paneOutput.setVisible(true);

            this.modifiedImage = medianFilterTask.getValue();

            ivPreviewImage.setImage(modifiedImage);

            // calculate PSNR
            calculatePSNR();
        });

        new Thread(medianFilterTask).start();
    }

    private void noiseAdditionImage(Image imageToProcess) {
        Task<Image> noiseAdditionTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                pbRemoval.setVisible(true);
                // run in background thread
                return attacker.noiseAdditionImage(imageToProcess);
            }
        };
        noiseAdditionTask.setOnSucceeded((WorkerStateEvent event) -> {
            pbRemoval.setVisible(false);
            paneOutput.setVisible(true);

            this.modifiedImage = noiseAdditionTask.getValue();

            ivPreviewImage.setImage(modifiedImage);

            // calculate PSNR
            calculatePSNR();
        });

        new Thread(noiseAdditionTask).start();
    }

    private void calculatePSNR() {
        // calculate PSNR
        Task<Double> calculatePsnrTask = new Task<Double>() {
            @Override
            protected Double call() throws Exception {
                pbRemoval.setVisible(true);

                PeakSignalNoiseRation psnr = new PeakSignalNoiseRation(originalImage, modifiedImage);

                return psnr.getPsnrValue();
            }
        };
        calculatePsnrTask.setOnSucceeded((WorkerStateEvent event1) -> {
            pbRemoval.setVisible(false);
            paneOutput.setVisible(true);

            double psnr = calculatePsnrTask.getValue();

            labelPSNR.setText("PSNR : " + psnr);
        });

        new Thread(calculatePsnrTask).start();
    }

    @FXML
    void onSaveModifiedImage(ActionEvent event) {
        String newFilename = "";
        
        switch (type) {
            case SHARPENING:
                newFilename = "sharpen_" + fileName;
                decideSaveLocation("D:\\_watermarking\\saved\\7_sharpening");
                break;
            case BLURRING:
                newFilename = "blur_" + fileName;
                decideSaveLocation("D:\\_watermarking\\saved\\8_blurring");
                break;
            case MEDIAN_FILTER:
                newFilename = "medianfilter_" + fileName;
                decideSaveLocation("D:\\_watermarking\\saved\\9_medianfiltering");
                break;
            case NOISE_ADDITION:
                newFilename = "noise_" + fileName;
                decideSaveLocation("D:\\_watermarking\\saved\\10_noiseadding");
                break;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Simpan Citra Baru");
            fileChooser.setInitialDirectory(savedDir);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
            fileChooser.setInitialFileName(newFilename);

            File file = fileChooser.showSaveDialog(new Stage());

            if (file != null) {
                BufferedImage image = SwingFXUtils.fromFXImage(modifiedImage, null);
                ImageIO.write(image, "png", file);
            }
        } catch (IOException ex) {
            System.out.println("File not an image or not found");
        }
    }

    private void decideSaveLocation(String path) {
        int containerHeight = originalImage.heightProperty().intValue();
        int containerWidth = originalImage.widthProperty().intValue();

        if ((containerHeight >= 512 && containerHeight < 1024)
                && (containerWidth >= 512 && containerWidth < 1024)) {
            savedDir = new File(path + "\\small");
        } else if ((containerHeight >= 1024 && containerHeight < 2048)
                && (containerWidth >= 1024 && containerWidth < 2048)) {
            savedDir = new File(path + "\\medium");
        } else if (containerHeight >= 2048 && containerWidth >= 2048) {
            savedDir = new File(path + "\\large");
        }
    }

}
