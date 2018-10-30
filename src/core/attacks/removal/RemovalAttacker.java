package core.attacks.removal;

import core.helper.ImageUtil;
import java.util.ArrayList;
import java.util.Collections;
import javafx.scene.image.Image;

public class RemovalAttacker {

    private static final int BLOCK_SIZE = 3;

    private final MatrixConvolution convolution;

    public RemovalAttacker() {
        convolution = new MatrixConvolution();
    }

    /**
     * Sharpening an Image
     *
     * @param image Image to sharpen
     * @return modified image
     */
    public Image sharpenImage(Image image) {
        int height = image.heightProperty().intValue();
        int width = image.widthProperty().intValue();

        double[][] sourcePixels = ImageUtil.imageToPixelValues(image);
        double[][] modifiedPixels = new double[height][width];

        convolution.setPixels(sourcePixels);
        convolution.setKernel(KernelConstant.UNSHARP_MASKING);

        modifiedPixels = convolution.computeConvolution();

        return ImageUtil.pixelValuesToImage(modifiedPixels);
    }

    /**
     * Blurring an Image
     *
     * @param image Image to blur
     * @return modified image
     */
    public Image blurImage(Image image) {
        int height = image.heightProperty().intValue();
        int width = image.widthProperty().intValue();

        double[][] sourcePixels = ImageUtil.imageToPixelValues(image);
        double[][] modifiedPixels = new double[height][width];

        convolution.setPixels(sourcePixels);
        convolution.setKernel(KernelConstant.GAUSSIAN_BLUR);

        modifiedPixels = convolution.computeConvolution();

        return ImageUtil.pixelValuesToImage(modifiedPixels);
    }

    /**
     * Median Filtering an Image
     *
     * @param image Image to median filter
     * @return modified image
     */
    public Image medianFilterImage(Image image) {
        int squaredBlockSize = BLOCK_SIZE * BLOCK_SIZE;

        int height = image.heightProperty().intValue();
        int width = image.widthProperty().intValue();

        double[][] sourcePixels = ImageUtil.imageToPixelValues(image);
        double[][] modifiedPixels = ImageUtil.copyPixels(sourcePixels);

        for (int row = 0; row < height - BLOCK_SIZE; row += BLOCK_SIZE) {
            for (int col = 0; col < width - BLOCK_SIZE; col += BLOCK_SIZE) {
                ArrayList<Double> blockedPixels1D = new ArrayList<>();

                for (int i = 0; i < BLOCK_SIZE; i++) {
                    for (int j = 0; j < BLOCK_SIZE; j++) {
                        blockedPixels1D.add(sourcePixels[row + i][col + j]);
                    }
                }

                // Sorting values
                Collections.sort(blockedPixels1D);
                // get Median value
                double median = blockedPixels1D.get(squaredBlockSize / 2);
                // set value into middle of the BLOCK_SIZE x BLOCK_SIZE pixels
                modifiedPixels[row + (BLOCK_SIZE / 2)][col + (BLOCK_SIZE / 2)] = median;
            }
        }

        return ImageUtil.pixelValuesToImage(modifiedPixels);
    }

    /**
     * Adding random Gaussian noise to image
     *
     * @param image Image to median filter
     * @return modified image
     */
    public Image noiseAdditionImage(Image image) {
        double a = 0.0;
        double b = 0.0;

        int height = image.heightProperty().intValue();
        int width = image.widthProperty().intValue();

        double[][] sourcePixels = ImageUtil.imageToPixelValues(image);
        double[][] modifiedPixels = new double[height][width];

        double mean = calculateMean(sourcePixels);
        double standarDeviation = calculateStandarDeviation(sourcePixels, mean);
        double variance = standarDeviation * standarDeviation;

        for (int row = 0; row < height; row += BLOCK_SIZE) {
            for (int col = 0; col < width; col += BLOCK_SIZE) {
                a = Math.random();
                b = Math.random();

                double x = Math.sqrt(-2 * Math.log(a)) * Math.cos(2 * Math.PI * b);
                double noise = mean + Math.sqrt(variance) * x;

                modifiedPixels[row][col] = sourcePixels[row][col] + noise;
            }
        }

        return ImageUtil.pixelValuesToImage(modifiedPixels);
    }

    /**
     * Calculate image mean
     *
     * @param pixels image pixels
     * @return mean of an image
     */
    private double calculateMean(double[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;

        double sum = 0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                sum += pixels[row][col];
            }
        }

        return sum / (height * width);
    }

    /**
     * Calculate standard deviation of image
     *
     * @param pixels image pixels
     * @param mean mean of an image
     * @return standard deviation of image
     */
    private double calculateStandarDeviation(double[][] pixels, double mean) {
        int height = pixels.length;
        int width = pixels[0].length;

        int n = height * width;

        double var = 0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                var += (pixels[row][col] - mean) * (pixels[row][col] - mean);
            }
        }

        var = var / (n - 1);

        return Math.sqrt(var);
    }

    private double boxMuller(double u1, double u2, double mean, double st_dev) {
        return ((Math.sqrt(-2 * Math.log(u2)) * Math.cos(2 * Math.PI * u1) * st_dev) + mean);
    }

}
