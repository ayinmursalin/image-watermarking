package core.attacks;

import core.helper.ImageUtil;
import javafx.scene.image.Image;

public class RemovalAttacker {
    
    private final MatrixConvolution convolution;

    public RemovalAttacker() {
        convolution = new MatrixConvolution();
    }
    
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

}
