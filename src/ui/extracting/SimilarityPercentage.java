package ui.extracting;

import core.helper.ImageUtil;
import javafx.scene.image.Image;

public class SimilarityPercentage {

    public double getSimilarityPercentage(Image image1, Image image2) {
        int height1 = image1.heightProperty().intValue();
        int width1 = image1.widthProperty().intValue();
        int height2 = image2.heightProperty().intValue();
        int width2 = image2.widthProperty().intValue();

        // image should have same pixel size
        if (height1 != height2 || width1 != width2) {
            return 0;
        }

        int totalPixels = height1 * width1;
        int sumSimilar = 0;

        double[][] pixelImage1 = ImageUtil.imageToPixelValues(image1);
        double[][] pixelImage2 = ImageUtil.imageToPixelValues(image2);

        for (int row = 0; row < height1; row++) {
            for (int col = 0; col < width1; col++) {
                if (pixelImage1[row][col] == pixelImage2[row][col]) {
                    sumSimilar++;
                }                
            }
        }

        return (Double.valueOf(sumSimilar) / Double.valueOf(totalPixels)) * 100;
    }

}
