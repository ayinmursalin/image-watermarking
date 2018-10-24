package core.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageUtil {

    // copy pixels (do not modify original pixels)
    public static double[][] copyPixels(double[][] pixels) {
        int width = pixels[0].length;
        int height = pixels.length;

        double[][] temp = new double[height][width];

        for (int row = 0; row < height; row++) {
            // copy row by row of pixels
            System.arraycopy(pixels[row], 0, temp[row], 0, width);
        }

        return temp;
    }

    public static Image fileToImage(File file) throws FileNotFoundException {
        return new Image(new FileInputStream(file));
    }

    // greyscel, need only 1 color (because red = green = blue)
    // value in range 0~255
    public static double[][] imageToPixelValues(Image image) {
        int height = image.heightProperty().intValue();
        int width = image.widthProperty().intValue();

        double[][] pixels = new double[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                // greyscele -> red = green = blue
                int argb = image.getPixelReader().getArgb(col, row);
                int red = (argb >> 16) & 0xff;
                pixels[row][col] = red;
            }
            System.out.println();
        }

        return pixels;
    }

    // return binary value image (black white)
    // value 0 or 1
    public static List<Integer> imageToBinaryPixelValue(Image image) {
        int height = image.heightProperty().intValue();
        int width = image.widthProperty().intValue();

        ArrayList<Integer> binaryPixels = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                // black and white image
                Color color = image.getPixelReader().getColor(col, row);
                // color.getRed() return pixel value with range 0~1
                int red = (int) color.getRed();
                
                binaryPixels.add(red);
            }
        }

        return binaryPixels;
    }

    public static Image pixelValuesToImage(double[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;

        WritableImage image = new WritableImage(width, height);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Double color = (pixels[row][col] >= 0) ? pixels[row][col] : 0;
                int roundedColor = (color.intValue() <= 255) ? color.intValue() : 255;

                image.getPixelWriter()
                        .setColor(col, row, Color.rgb(roundedColor, roundedColor, roundedColor));
            }
            System.out.println();
        }

        return image;
    }

}
