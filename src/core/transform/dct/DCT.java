package core.transform.dct;

import core.transform.TransformDirection;
import core.transform.TransformUtil;
import java.util.ArrayList;
import java.util.List;

public class DCT {

    private double[][] originalPixels = null;
    private double[][] transformedPixels = null;
    private double[][] inversedPixels = null;

    private ArrayList<double[][]> transformedBlockedPixels;
    private ArrayList<double[][]> inversedBlockedPixels;

    // block size (NxN)
    private final int blockSize;

    // constructor only need blockSize
    public DCT(int blockSize) {
        this.blockSize = blockSize;
    }

    /**
     * Set image pixels to process
     *
     * @param pixels Original pixels of Image
     */
    public void setPixels(double[][] pixels) {
        originalPixels = TransformUtil.copyPixels(pixels);
    }

    /**
     * Get the pixels that already process (after inverse)
     *
     * @return inverse pixels
     */
    public double[][] getPixels() {
        // if no inversedPixels (or not call inverse process yet)
        if (inversedPixels == null) {
            return originalPixels;
        }
        return inversedPixels;
    }

    /**
     * Transforming process (this can be access)
     *
     * @param direction direction of DCT (forward or reverse)
     */
    public void transform(TransformDirection direction) {
        switch (direction) {
            case FORWARD:
                forwardDct();
                break;
            case INVERSE:
                inverseDct();
                break;
        }
    }

    /**
     * Forward process (private, can only call by this class)
     */
    private void forwardDct() {
        // copy of pixels
        transformedPixels = TransformUtil.copyPixels(originalPixels);

        // pixels that can be process DCT transform (blockSize x blockSize)
        List<double[][]> transformablePixels = splits(transformedPixels);

        // blockedPixels after DCT transform process
        transformedBlockedPixels = new ArrayList<>();

        // do DCT transform for each blocked pixels
        for (double[][] blockedPixels : transformablePixels) {
            transformedBlockedPixels.add(dctBlockedPixels(blockedPixels));
        }

        // put blockedPixels into copy pixels
        replaces(transformedPixels, transformedBlockedPixels);
    }

    /**
     * DCT Process of each blocked pixels
     *
     * @param blockedPixels block of pixels that will be processed
     * @return transformed block of pixels
     */
    private double[][] dctBlockedPixels(double[][] blockedPixels) {
        int height = blockedPixels.length;
        int width = blockedPixels[0].length;

        double defaultM = Math.sqrt(2.0 / width);
        double defaultN = Math.sqrt(2.0 / height);
        double alphaU, alphaV, sum, newPixelValue;

        double[][] dctTransformedPixels = new double[height][width];

        // if pixel is already blocked of (blockSize x blockSize)
        if (height == blockSize && width == blockSize) {
            for (int u = 0; u < height; u++) {
                for (int v = 0; v < width; v++) {
                    // alpha u value
                    if (u == 0) {
                        alphaU = 1 / Math.sqrt(2);
                    } else {
                        alphaU = 1;
                    }
                    // alpha v value
                    if (v == 0) {
                        alphaV = 1 / Math.sqrt(2);
                    } else {
                        alphaV = 1;
                    }

                    sum = 0;
                    for (int m = 0; m < width; m++) {
                        for (int n = 0; n < height; n++) {
                            newPixelValue = blockedPixels[m][n]
                                    * Math.cos((2 * m + 1) * u * Math.PI / (2 * width))
                                    * Math.cos((2 * n + 1) * v * Math.PI / (2 * height));
                            sum = sum + newPixelValue;
                        }
                    }

                    dctTransformedPixels[u][v] = defaultM * defaultN * alphaU * alphaV * sum;
                }
            }

            return dctTransformedPixels;
        }

        return null;
    }

    /**
     * Inverse process (protected, can only call by this class)
     *
     * @param transformedPixels transformedPixels (already DCT)
     * @return original pixels
     */
    private double[][] inverseDct() {
        // copy of pixels
        inversedPixels = TransformUtil.copyPixels(transformedPixels);

        // pixels that can be process DCT transform (blockSize x blockSize)
        List<double[][]> transformablePixels = splits(inversedPixels);

        // blockedPixels after DCT transform process
        inversedBlockedPixels = new ArrayList<>();

        // do DCT transform for each blocked pixels
        for (double[][] blockedPixels : transformablePixels) {
            inversedBlockedPixels.add(inverseDctBlockedPixels(blockedPixels));
        }

        // put blockedPixels into copy pixels
        replaces(inversedPixels, inversedBlockedPixels);

        return inversedPixels;
    }

    /**
     * Inverse DCT Process of each blocked pixels
     *
     * @param blockedTransformedPixels block of pixels that already transformed
     * to reversed
     * @return original block of pixels
     */
    private double[][] inverseDctBlockedPixels(double[][] blockedTransformedPixels) {
        int height = blockedTransformedPixels.length;
        int width = blockedTransformedPixels[0].length;

        double defaultM = Math.sqrt(2.0 / width);
        double defaultN = Math.sqrt(2.0 / height);
        double alphaU, alphaV, sum, newPixelValue;

        double[][] dctTransformedPixels = new double[height][width];

        // if pixel is already blocked of (blockSize x blockSize)
        if (height == blockSize && width == blockSize) {
            for (int m = 0; m < height; m++) {
                for (int n = 0; n < width; n++) {
                    sum = 0;
                    for (int u = 0; u < width; u++) {
                        for (int v = 0; v < height; v++) {
                            // alpha u value
                            if (u == 0) {
                                alphaU = 1 / Math.sqrt(2);
                            } else {
                                alphaU = 1;
                            }
                            // alpha v value
                            if (v == 0) {
                                alphaV = 1 / Math.sqrt(2.0);
                            } else {
                                alphaV = 1;
                            }

                            newPixelValue = blockedTransformedPixels[u][v]
                                    * alphaU
                                    * alphaV
                                    * Math.cos((2 * m + 1) * u * Math.PI / (2 * width))
                                    * Math.cos((2 * n + 1) * v * Math.PI / (2 * height));
                            sum = sum + newPixelValue;
                        }
                    }

                    dctTransformedPixels[m][n] = defaultM * defaultN * sum;
                }
            }

            return dctTransformedPixels;
        }

        return null;
    }

    /**
     * get blocks that can be embedded
     *
     * @return list of embeddable transformed pixels
     */
    public List<double[][]> getEmbeddableTransformedPixels() {
        return splits(transformedPixels);
    }

    /**
     * set list of embedded pixels into DCT coefficient (before inverse)
     *
     * @param embeddedTransformedPixels list of block pixels that already
     * embedded
     */
    public void setEmbeddedPixels(List<double[][]> embeddedTransformedPixels) {
        replaces(transformedPixels, embeddedTransformedPixels);
    }

    /**
     * get Mid band coefficient from blocked Pixels
     *
     * @param blockedPixels blocked pixels with blockSize x blockSize
     * @return
     */
    public List<Double> getMidbandCoefficient(double[][] blockedPixels) {
        ArrayList<Double> midband = new ArrayList<>();

        midband.add(blockedPixels[0][2]);
        midband.add(blockedPixels[0][3]);

        midband.add(blockedPixels[1][1]);
        midband.add(blockedPixels[1][2]);

        midband.add(blockedPixels[2][0]);
        midband.add(blockedPixels[2][1]);

        midband.add(blockedPixels[3][0]);

        return midband;
    }

    /**
     * get Mid band coefficient from blocked Pixels
     *
     * @param blockedPixels blocked pixels with blockSize x blockSize
     * @param newMidBand modified mid band coefficient
     */
    public void setMidbandCoefficient(double[][] blockedPixels, List<Double> newMidBand) {
        if (newMidBand.size() != 7) {
            return;
        }

        blockedPixels[0][2] = newMidBand.get(0);
        blockedPixels[0][3] = newMidBand.get(1);

        blockedPixels[1][1] = newMidBand.get(2);
        blockedPixels[1][2] = newMidBand.get(3);

        blockedPixels[2][0] = newMidBand.get(4);
        blockedPixels[2][1] = newMidBand.get(5);

        blockedPixels[3][0] = newMidBand.get(6);
    }

    /**
     * Clear or reset any state
     */
    public void clear() {
        this.originalPixels = null;
        this.transformedPixels = null;
        this.inversedPixels = null;

        this.transformedBlockedPixels = null;
        this.inversedBlockedPixels = null;
    }

    /**
     * dividing pixels by block size that can be process DCT transform
     *
     * @param pixels image pixels to split
     * @return List of blocked pixels (BlockSize x BlockSize)
     * @throws ArrayIndexOutOfBoundsException
     * @throws NullPointerException
     */
    private List<double[][]> splits(double[][] pixels) throws
            ArrayIndexOutOfBoundsException, NullPointerException {
        int height = pixels.length;
        int width = pixels[0].length;

        if (blockSize <= 0) {
            throw new ArrayIndexOutOfBoundsException("Blocksize must be atleast 1x1");
        }

        // number of pixels that can be divided for blockedPixels (bs x bs)
        int colSize = (width / blockSize);
        int rowSize = (height / blockSize);

        // list of blocked pixels (BlockSize x BlockSize)
        List<double[][]> blockedPixelList = new ArrayList<>();

        int startRowIndex = 0;
        int startColIndex = 0;

        for (int u = 0; u < rowSize; u++) {
            startRowIndex += (u * blockSize);

            for (int v = 0; v < colSize; v++) {
                startColIndex += (v * blockSize);
                double[][] blockedPixels = new double[blockSize][blockSize];

                for (int row = 0; row < blockSize; row++) {
                    for (int col = 0; col < blockSize; col++) {
                        blockedPixels[row][col] = pixels[startRowIndex + row][col + startColIndex];
                    }
                }

                startColIndex -= (v * blockSize);
                blockedPixelList.add(blockedPixels);
            }
            startRowIndex -= (u * blockSize);
        }

        return blockedPixelList;
    }

    /**
     * combine transformedBlocked into originalPixels
     *
     * @param targetPixels target of pixels to replace with new Blocked Pixels
     * @param newBlockedPixels new Blocked Pixels that already modify
     * @throws ArrayIndexOutOfBoundsException
     * @throws NullPointerException
     */
    private void replaces(double[][] targetPixels, List<double[][]> newBlockedPixels) throws
            ArrayIndexOutOfBoundsException, NullPointerException {
        int height = targetPixels.length;
        int width = targetPixels[0].length;

        if (blockSize <= 0) {
            throw new ArrayIndexOutOfBoundsException("Blocksize must be atleast 1x1");
        }

        int transformedColSize = (width / blockSize);
        int transformedRowSize = (height / blockSize);

        int transformedIndex = 0;
        int startRowIndex = 0;
        int startColIndex = 0;

        for (int u = 0; u < transformedRowSize; u++) {
            startRowIndex += (u * blockSize);

            for (int v = 0; v < transformedColSize; v++) {
                startColIndex += (v * blockSize);

                // copy row by row of newBlockedPixels into targetPixels
                for (int row = 0; row < blockSize; row++) {
                    System.arraycopy(newBlockedPixels.get(transformedIndex)[row], 0,
                            targetPixels[startRowIndex + row], startColIndex, blockSize);
                }

                startColIndex -= (v * blockSize);
                transformedIndex++;
            }
            startRowIndex -= (u * blockSize);
        }
    }

}
