package ui.geometricattacks;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import core.attacks.geometric.GeometricAttacker;
import core.attacks.geometric.GeometricType;
import core.attacks.geometric.RotationDirection;
import core.helper.ImageUtil;
import core.helper.PeakSignalNoiseRation;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class GeometricController implements Initializable {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
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
    private JFXRadioButton rbRotation;
    @FXML
    private ToggleGroup geometricattack;
    @FXML
    private JFXRadioButton rbScalling;
    @FXML
    private JFXRadioButton rbTranslation;
    @FXML
    private GridPane layoutRotation;
    @FXML
    private JFXButton btnRotateLeft;
    @FXML
    private JFXButton btnRotateRight;
    @FXML
    private Pane layoutTranslation;
    @FXML
    private JFXTextField tfTranslationX;
    @FXML
    private JFXTextField tfTranslationY;
    @FXML
    private Pane layoutScalling;
    @FXML
    private JFXTextField tfScalling;
    @FXML
    private JFXProgressBar pbRemoval;

    private Text messageHeader, messageBody;
    private JFXDialogLayout dialogLayout;
    private JFXDialog dialog;

    private GeometricAttacker attacker;
    private Image originalImage, modifiedImage;
    private GeometricType type;

    private String fileName = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        attacker = new GeometricAttacker();

        Pattern pattern = Pattern.compile("\\d*|\\d+\\.\\d*");
        Pattern patternMin = Pattern.compile("-*\\d*|\\d+\\,\\d*");
        TextFormatter formatterD1 = new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return pattern.matcher(change.getControlNewText()).matches() ? change : null;
        });
        TextFormatter formatterD2 = new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return patternMin.matcher(change.getControlNewText()).matches() ? change : null;
        });
        TextFormatter formatterD3 = new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return patternMin.matcher(change.getControlNewText()).matches() ? change : null;
        });

        tfScalling.setTextFormatter(formatterD1);
        tfTranslationX.setTextFormatter(formatterD2);
        tfTranslationY.setTextFormatter(formatterD3);

        // dialog
        messageHeader = new Text("");
        messageBody = new Text("");

        dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(messageHeader);
        dialogLayout.setBody(messageBody);
        dialog = new JFXDialog(stackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
    }

    @FXML
    void onChooseImage(ActionEvent event) {
        hideLayout();
        paneOutput.setVisible(false);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Citra");
            fileChooser.setInitialDirectory(new File("D:\\saved\\embedded"));
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
    void onChooseGeometricAttack(ActionEvent event) {
        hideLayout();
        Toggle selectedRadioButton = geometricattack.getSelectedToggle();

        if (selectedRadioButton == rbRotation) {
            type = GeometricType.ROTATION;
            layoutRotation.setVisible(true);
        } else if (selectedRadioButton == rbTranslation) {
            type = GeometricType.TRANSLATION;
            layoutTranslation.setVisible(true);
            btnProcessAttack.setVisible(true);
        } else if (selectedRadioButton == rbScalling) {
            type = GeometricType.SCALLING;
            layoutScalling.setVisible(true);
            btnProcessAttack.setVisible(true);
        } else {
            type = null;
        }
    }

    @FXML
    void onRotateLeft(ActionEvent event) {
        if (originalImage == null) {
            messageHeader.setText("Belum Memilih Citra");
            messageBody.setText("Harap pilih citra yang akan diproses terlebih dahulu.");

            dialog.show();
        } else {
            // allow to multiple process
            Image imageToProcess = null;

            if (modifiedImage == null) {
                imageToProcess = originalImage;
            } else {
                imageToProcess = modifiedImage;
            }

            rotateImage(imageToProcess, RotationDirection.LEFT);
        }
    }

    @FXML
    void onRotateRight(ActionEvent event) {
        if (originalImage == null) {
            messageHeader.setText("Belum Memilih Citra");
            messageBody.setText("Harap pilih citra yang akan diproses terlebih dahulu.");

            dialog.show();
        } else {
            // allow to multiple process
            Image imageToProcess = null;

            if (modifiedImage == null) {
                imageToProcess = originalImage;
            } else {
                imageToProcess = modifiedImage;
            }

            rotateImage(imageToProcess, RotationDirection.RIGHT);
        }
    }

    @FXML
    void onProcessAttack(ActionEvent event) {
        if (originalImage == null) {
            messageHeader.setText("Belum Memilih Citra");
            messageBody.setText("Harap pilih citra yang akan diproses terlebih dahulu.");

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
                case TRANSLATION:
                    translateImage(imageToProcess);
                    break;
                case SCALLING:
                    scaleImage(imageToProcess);
                    break;
            }
        }
    }

    private void rotateImage(Image imageToProcess, RotationDirection direction) {
        Task<Image> rotateTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                pbRemoval.setVisible(true);
                // run in background thread
                return attacker.rotateImage(imageToProcess, direction);
            }
        };
        rotateTask.setOnSucceeded((WorkerStateEvent event) -> {
            pbRemoval.setVisible(false);
            paneOutput.setVisible(true);

            this.modifiedImage = rotateTask.getValue();

            ivPreviewImage.setImage(modifiedImage);

            // calculate PSNR
            calculatePSNR();
        });

        new Thread(rotateTask).start();
    }

    private void translateImage(Image imageToProcess) {
        try {
            double addX = Double.parseDouble(tfTranslationX.getText());
            double addY = Double.parseDouble(tfTranslationY.getText());

            Task<Image> translateTask = new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    pbRemoval.setVisible(true);
                    // run in background thread
                    return attacker.translateImage(imageToProcess, addX, addY);
                }
            };
            translateTask.setOnSucceeded((WorkerStateEvent event) -> {
                pbRemoval.setVisible(false);
                paneOutput.setVisible(true);

                this.modifiedImage = translateTask.getValue();

                ivPreviewImage.setImage(modifiedImage);

                // calculate PSNR
                calculatePSNR();
            });

            new Thread(translateTask).start();
        } catch (NumberFormatException e) {
            messageHeader.setText("Belum Menentukan nilai tranlasi X atau Y");
            messageBody.setText("Harap masukkan besaran translasi terlebih dahulu.");

            dialog.show();
        }
    }

    private void scaleImage(Image imageToProcess) {
        try {
            double scaleParam = Double.parseDouble(tfScalling.getText());

            Task<Image> scaleTask = new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    pbRemoval.setVisible(true);
                    // run in background thread
                    return attacker.scaleImage(imageToProcess, scaleParam);
                }
            };
            scaleTask.setOnSucceeded((WorkerStateEvent event) -> {
                pbRemoval.setVisible(false);
                paneOutput.setVisible(true);

                this.modifiedImage = scaleTask.getValue();

                ivPreviewImage.setImage(modifiedImage);

                // calculate PSNR
                calculatePSNR();
            });

            new Thread(scaleTask).start();
        } catch (NumberFormatException e) {
            messageHeader.setText("Belum Menentukan besaran scalling");
            messageBody.setText("Harap masukkan besaran scaling terhadap ukuran asli terlebih dahulu.");

            dialog.show();
        }
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
        switch (type) {
            case ROTATION:
                this.fileName = "rotation_" + this.fileName;
                break;
            case TRANSLATION:
                this.fileName = "translation_" + this.fileName;
                break;
            case SCALLING:
                this.fileName = "scalling_" + this.fileName;
                break;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Simpan Citra Baru");
            fileChooser.setInitialDirectory(new File("D:\\saved\\modified"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
            fileChooser.setInitialFileName(fileName);

            File file = fileChooser.showSaveDialog(new Stage());

            if (file != null) {
                BufferedImage image = SwingFXUtils.fromFXImage(modifiedImage, null);
                ImageIO.write(image, "png", file);
            }
        } catch (IOException ex) {
            System.out.println("File not an image or not found");
        }
    }

    private void hideLayout() {
        layoutRotation.setVisible(false);
        layoutTranslation.setVisible(false);
        layoutScalling.setVisible(false);
        btnProcessAttack.setVisible(false);
    }

}
