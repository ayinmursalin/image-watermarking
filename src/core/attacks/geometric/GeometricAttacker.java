package core.attacks.geometric;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class GeometricAttacker {

    private AffineTransform transform;

    public GeometricAttacker() {

    }

    public Image rotateImage(Image sourceImage, RotationDirection direction) {
        double radian = 0;
        switch (direction) {
            case RIGHT:
                radian = Math.toRadians(90);
                break;
            case LEFT:
                radian = Math.toRadians(-90);
                break;
        }
        transform = new AffineTransform();

        BufferedImage image = SwingFXUtils.fromFXImage(sourceImage, null);
        int height = image.getHeight();
        int width = image.getWidth();
        BufferedImage modifiedImage = new BufferedImage(width, height, image.getType());

        transform.rotate(radian, height / 2, width / 2);

        Graphics2D graphic = modifiedImage.createGraphics();
        graphic.drawImage(image, transform, null);

        return SwingFXUtils.toFXImage(modifiedImage, null);
    }
    
}
