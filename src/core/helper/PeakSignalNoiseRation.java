package core.helper;

import core.transform.TransformUtil;
import javafx.scene.image.Image;

public class PeakSignalNoiseRation {

    private final double[][] image1;
    private final double[][] image2;

    private final int image1Height;
    private final int image1Width;
    private final int image2Height;
    private final int image2Width;

    public PeakSignalNoiseRation(Image image1, Image image2) {
        image1Height = image1.heightProperty().intValue();
        image1Width = image1.widthProperty().intValue();
        image2Height = image2.heightProperty().intValue();
        image2Width = image2.widthProperty().intValue();

        this.image1 = TransformUtil.imageToPixelValues(image1);
        this.image2 = TransformUtil.imageToPixelValues(image2);
    }

    public double getPsnrValue() {
        if (image1Height != image2Height && image1Width != image2Width) {
            return 0.0;
        }

        // max number of pixel = 255
        double s = Math.pow(255, 2);
        double mse = calculateMSE();

        return 10 * Math.log10(s / mse);
    }

    private double calculateMSE() {
        double numberOfPixels = image1Height * image1Width;
        double sum = 0;

        for (int row = 0; row < image1Height; row++) {
            for (int col = 0; col < image1Width; col++) {
                double diff = image1[row][col] - image2[row][col];

                sum += Math.pow(diff, 2);
            }
        }

        return sum / numberOfPixels;
    }

}
