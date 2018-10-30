package core.attacks.geometric;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class GeometricAttacker {

    public GeometricAttacker() {

    }

    public Image rotateImage(Image sourceImage, RotationDirection direction) {
        int height = sourceImage.heightProperty().intValue();
        int width = sourceImage.widthProperty().intValue();
        WritableImage modifiedImage = new WritableImage(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = sourceImage.getPixelReader().getArgb(x, y);
                switch (direction) {
                    case LEFT:
                        modifiedImage.getPixelWriter()
                                .setArgb(y, (width - 1) - x, pixelValue);
                        break;
                    case RIGHT:
                        modifiedImage.getPixelWriter()
                                .setArgb((height - 1) - y, x, pixelValue);
                        break;
                }
            }
        }

        return modifiedImage;
    }

}
