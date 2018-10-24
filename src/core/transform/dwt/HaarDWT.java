package core.transform.dwt;

import core.transform.TransformDirection;
import core.helper.ImageUtil;

// HAAR-DWT
public class HaarDWT extends DWT {

    private double[][] originalPixels = null;
    private double[][] transformedPixels = null;
    private double[][] inversedPixels = null;

    private final int cycles;

    public HaarDWT(int cycles) {
        this.cycles = cycles;
    }

    @Override
    public void setPixels(double[][] pixels) {
        originalPixels = ImageUtil.copyPixels(pixels);
    }

    @Override
    public double[][] getPixels() {
        // if no inversedPixels (or not call inverse process yet)
        if (inversedPixels == null) {
            return originalPixels;
        }
        return inversedPixels;
    }

    @Override
    public void transform(TransformDirection direction) {
        switch (direction) {
            case FORWARD:
                forwardDwt();
                break;
            case INVERSE:
                inverseDwt();
                break;
        }
    }

    @Override
    protected void forwardDwt() {
        int height = originalPixels.length;
        int width = originalPixels[0].length;

        if (isCanBeCycled(cycles, getMaxCycles(width))) {
            double[][] tempTransformedPixels = new double[height][width];

            // copy pixels (don't overwrite original pixels)
            transformedPixels = ImageUtil.copyPixels(originalPixels);

            // cycling
            for (int i = 0; i < cycles; i++) {
                /**
                 * Horizontal Haar-DWT
                 */
                width /= 2;
                // iterate on column then row
                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        // pixel and its' neighbor
                        double pixel1 = transformedPixels[row][col * 2];
                        double pixel2 = transformedPixels[row][col * 2 + 1];

                        double lowFrequency = (pixel1 + pixel2) / 2;
                        double highFrequency = (pixel1 - pixel2) / 2;

                        // put low frequency on left (before half)
                        tempTransformedPixels[row][col] = lowFrequency;
                        // put high frequency on right (after half)
                        tempTransformedPixels[row][width + col] = highFrequency;
                    }
                }

                // now transformedPixels after Horizontal Haar-DWT
                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        transformedPixels[row][col] = tempTransformedPixels[row][col];
                        transformedPixels[row][col + width] = tempTransformedPixels[row][col + width];
                    }
                }

                width *= 2;

                /**
                 * Vertical Haar-DWT
                 */
                height /= 2;
                // iterate on row then column
                for (int col = 0; col < width; col++) {
                    for (int row = 0; row < height; row++) {
                        // pixel and its' bottom neighbor (because vertical)
                        double pixel1 = transformedPixels[row * 2][col];
                        double pixel2 = transformedPixels[row * 2 + 1][col];

                        double lowFrequency = (pixel1 + pixel2) / 2;
                        double highFrequency = (pixel1 - pixel2) / 2;

                        // put low frequency on top (before half)
                        tempTransformedPixels[row][col] = lowFrequency;
                        // put low frequency on bottom (after half)
                        tempTransformedPixels[row + height][col] = highFrequency;
                    }
                }

                // now transformedPixels after Horizontal + Vertical Haar-DWT
                for (int col = 0; col < width; col++) {
                    for (int row = 0; row < height; row++) {
                        transformedPixels[row][col] = tempTransformedPixels[row][col];
                        transformedPixels[row + height][col] = tempTransformedPixels[row + height][col];
                    }
                }

                width /= 2;
            }
        }
    }

    @Override
    protected void inverseDwt() {
        int height = transformedPixels.length;
        int width = transformedPixels[0].length;

        if (isCanBeCycled(cycles, getMaxCycles(width))) {
            inversedPixels = new double[height][width];
            double[][] tempOriginalPixels = new double[height][width];

            // copy pixels (don't modify original pixels)
            inversedPixels = ImageUtil.copyPixels(transformedPixels);

            // height and width per sub-band
            int height2 = height / (int) Math.pow(2, cycles);
            int width2 = width / (int) Math.pow(2, cycles);

            // cycling
            for (int i = cycles; i > 0; i--) {
                /**
                 * Vertical Inverse Haar-DWT
                 */
                width2 *= 2;
                // iterate row then column
                for (int col = 0; col < width2; col++) {
                    for (int row = 0; row < height2; row++) {
                        // pixel koeficient and after half position
                        double lowFrequency = inversedPixels[row][col];
                        double highFrequency = inversedPixels[row + height2][col];

                        double pixel1 = lowFrequency + highFrequency;
                        double pixel2 = lowFrequency - highFrequency;

                        // put the pixel side by side as vertical (pixel and its' bottom neighbor)
                        tempOriginalPixels[2 * row][col] = pixel1;
                        tempOriginalPixels[2 * row + 1][col] = pixel2;
                    }
                }

                // now inversed to originalPixel after Vertical Inverse Haar-DWT
                for (int col = 0; col < width2; col++) {
                    for (int row = 0; row < height2; row++) {
                        inversedPixels[2 * row][col] = tempOriginalPixels[2 * row][col];
                        inversedPixels[2 * row + 1][col] = tempOriginalPixels[2 * row + 1][col];
                    }
                }

                width2 /= 2;

                /**
                 * Horizontal Inverse Haar-DWT
                 */
                height2 *= 2;
                // iterate row then column
                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        // pixel koeficient and after half position
                        double lowFrequency = inversedPixels[row][col];
                        double highFrequency = inversedPixels[row][col + width2];

                        double pixel1 = lowFrequency + highFrequency;
                        double pixel2 = lowFrequency - highFrequency;

                        // put the pixel side by side as vertical (pixel and its' bottom neighbor)
                        tempOriginalPixels[row][2 * col] = pixel1;
                        tempOriginalPixels[row][2 * col + 1] = pixel2;
                    }
                }

                // now inversed to originalPixel after Horizontal Inverse Haar-DWT
                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        inversedPixels[row][2 * col] = tempOriginalPixels[row][2 * col];
                        inversedPixels[row][2 * col + 1] = tempOriginalPixels[row][2 * col + 1];
                    }
                }

                width2 *= 2;
            }
        }
    }

    @Override
    public double[][] getDwtSubBand(DwtSubBand subBand) {
        int height = transformedPixels.length;
        int width = transformedPixels[0].length;

        double[][] result = null;

        switch (subBand) {
            case LL2: {
                int height2 = height / 4;
                int width2 = width / 4;

                result = new double[height2][width2];

                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        result[row][col] = transformedPixels[row][col];
                    }
                }

                break;
            }
            case LH2: {
                int height2 = height / 4;
                int width2 = width / 4;

                result = new double[height2][width2];

                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        result[row][col] = transformedPixels[row + height2][col];
                    }
                }

                break;
            }
            case HL2: {
                int height2 = height / 4;
                int width2 = width / 4;

                result = new double[height2][width2];

                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        result[row][col] = transformedPixels[row][col + width2];
                    }
                }

                break;
            }

            case HH2: {
                int height2 = height / 4;
                int width2 = width / 4;

                result = new double[height2][width2];

                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        result[row][col] = transformedPixels[row + height2][col + width2];
                    }
                }

                break;
            }
            case LH1: {
                int height2 = height / 2;
                int width2 = width / 2;

                result = new double[height2][width2];

                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        result[row][col] = transformedPixels[row + height2][col];
                    }
                }

                break;
            }
            case HL1: {
                int height2 = height / 2;
                int width2 = width / 2;

                result = new double[height2][width2];

                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        result[row][col] = transformedPixels[row][col + width2];
                    }
                }

                break;
            }
            case HH1: {
                int height2 = height / 2;
                int width2 = width / 2;

                result = new double[height2][width2];

                for (int row = 0; row < height2; row++) {
                    for (int col = 0; col < width2; col++) {
                        result[row][col] = transformedPixels[row + height2][col + width2];
                    }
                }

                break;
            }
        }

        return result;
    }

    @Override
    public void setDwtSubBand(double[][] subBandPixels, DwtSubBand subBand) {
        int subHeight = subBandPixels.length;
        int subWidth = subBandPixels[0].length;

        switch (subBand) {
            case LL2: {
                for (int row = 0; row < subHeight; row++) {
                    for (int col = 0; col < subWidth; col++) {
                        transformedPixels[row][col] = subBandPixels[row][col];
                    }
                }

                break;
            }
            case LH2: {
                for (int row = 0; row < subHeight; row++) {
                    for (int col = 0; col < subWidth; col++) {
                        transformedPixels[row + subHeight][col] = subBandPixels[row][col];
                    }
                }

                break;
            }
            case HL2: {
                for (int row = 0; row < subHeight; row++) {
                    for (int col = 0; col < subWidth; col++) {
                        transformedPixels[row][col + subWidth] = subBandPixels[row][col];
                    }
                }

                break;
            }

            case HH2: {
                for (int row = 0; row < subHeight; row++) {
                    for (int col = 0; col < subWidth; col++) {
                        transformedPixels[row + subHeight][col + subWidth] = subBandPixels[row][col];
                    }
                }

                break;
            }
            case LH1: {
                for (int row = 0; row < subHeight; row++) {
                    for (int col = 0; col < subWidth; col++) {
                        transformedPixels[row + subHeight][col] = subBandPixels[row][col];
                    }
                }

                break;
            }
            case HL1: {
                for (int row = 0; row < subHeight; row++) {
                    for (int col = 0; col < subWidth; col++) {
                        transformedPixels[row][col + subWidth] = subBandPixels[row][col];
                    }
                }

                break;
            }
            case HH1: {
                for (int row = 0; row < subHeight; row++) {
                    for (int col = 0; col < subWidth; col++) {
                        transformedPixels[row + subHeight][col + subWidth] = subBandPixels[row][col];
                    }
                }

                break;
            }
        }
    }

    @Override
    public void clear() {
        this.originalPixels = null;
        this.inversedPixels = null;
        this.transformedPixels = null;
    }

    /**
     * get maximum cycles of an image that can be process (maximum DWT level can
     * be applied)
     *
     * @param widthHeight width or height of image
     * @return Maximum Cycles (or DWT Level) to an Image that can be process
     */
    private int getMaxCycles(int widthHeight) {
        int maxCycles = 0;
        while (widthHeight > 1) {
            maxCycles++;
            widthHeight /= 2;
        }

        return maxCycles;
    }

    /**
     * check if cycles can be applied
     *
     * @param maxCycles
     * @param cycles
     * @return true or false
     */
    private boolean isCanBeCycled(int cycles, int maxCycles) {
        return cycles <= maxCycles;
    }

}
