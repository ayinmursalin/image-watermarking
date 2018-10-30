package core.attacks.removal;

public class MatrixConvolution {

    private double[][] sourcePixels;
    private double[][] matrixKernel;
    private int kernelSize = 0;

    /**
     * Set pixel to process
     *
     * @param pixels pixel to process
     */
    public void setPixels(double[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;
        this.sourcePixels = new double[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                this.sourcePixels[row][col] = pixels[row][col];
            }
        }
    }

    /**
     * Set matrixKernel to operate with
     *
     * @param kernel Matrix Kernel
     */
    public void setKernel(double[][] kernel) {
        this.kernelSize = kernel.length;
        this.matrixKernel = new double[kernelSize][kernelSize];

        for (int row = 0; row < kernelSize; row++) {
            for (int col = 0; col < kernelSize; col++) {
                this.matrixKernel[row][col] = kernel[row][col];
            }
        }
    }

    /**
     * Compute Matrix convolution of sourcePixels and matrixKernel
     *
     * @return convoluted pixels
     */
    public double[][] computeConvolution() {
        if (sourcePixels == null) {
            // not set pixel to operate yet
            return null;
        }
        if (kernelSize == 0) {
            // not set matrixKernel yet
            throw new ArrayIndexOutOfBoundsException("Please provide kernel first");
        }

        int height = sourcePixels.length;
        int width = sourcePixels[0].length;

        double[][] newPixels = new double[height][width];

        // mid position of matrixKernel
        int midLength = kernelSize / 2;

        // convolution on middle pixels (edge not operate yet)
        for (int row = 0 - midLength; row < height - midLength; row++) {
            for (int col = 0 - midLength; col < width - midLength; col++) {
                // create 3x3 blockedPixels
                double[][] blockedPixels = new double[kernelSize][kernelSize];
                double[][] usedKernel = new double[kernelSize][kernelSize];

                for (int i = 0; i < kernelSize; i++) {
                    for (int j = 0; j < kernelSize; j++) {
                        try {
                            blockedPixels[i][j] = sourcePixels[row + i][col + j];
                            usedKernel[i][j] = matrixKernel[i][j];
                        } catch (IndexOutOfBoundsException e) {
                            blockedPixels[i][j] = 0;
                            usedKernel[i][j] = 0;
                        }
                    }
                }

                // put on the middle of blockedPixel position
                newPixels[row + midLength][col + midLength] = convolute(blockedPixels, usedKernel);
            }
        }

        // final convoluted pixels
        return newPixels;
    }

    /**
     * Calculate value of each pixels
     *
     * @param blockedPixels blocked pixels (should have same length with matrixKernel
 size)
     * @param usedKernel matrixKernel to use
     * @return convoluted of pixel in (x,y) position
     */
    private double convolute(double[][] blockedPixels, double[][] usedKernel) {
        double[][] newBlockedPixels = new double[kernelSize][kernelSize];

        for (int row = 0; row < kernelSize; row++) {
            for (int col = 0; col < kernelSize; col++) {
                newBlockedPixels[row][col] = blockedPixels[row][col] * matrixKernel[row][col];
            }
        }

        return countSumBlockedPixels(newBlockedPixels) / countSumBlockedPixels(usedKernel);
    }

    /**
     * Calculate sum of each blockedPixels
     *
     * @param blockedPixels blockedPixels
     * @return
     */
    private double countSumBlockedPixels(double[][] blockedPixels) {
        double sum = 0;

        for (int row = 0; row < kernelSize; row++) {
            for (int col = 0; col < kernelSize; col++) {
                sum += blockedPixels[row][col];
            }
        }

        return sum;
    }

}
