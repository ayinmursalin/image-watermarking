package core;

import core.transform.TransformDirection;
import core.transform.TransformUtil;
import core.transform.dct.DCT;
import core.transform.dwt.DWT;
import core.transform.dwt.DWT.MotherOfWavelet;
import core.transform.dwt.DwtSubBand;
import javafx.scene.image.Image;
import core.transform.dwt.HaarDWT;
import core.helper.PearsonCorrelation;
import core.helper.Pseudorandom;
import java.util.ArrayList;
import java.util.List;

public class Watermarker {

    // now it's defined, next it might be asked on Constructor to make more choise 
    // about Wavelet Type, DWT level, etc.
    private static final MotherOfWavelet WAVELET_TYPE = MotherOfWavelet.HAAR;
    private static final DwtSubBand SELECTED_EMBED_SUBBAND = DwtSubBand.HH2;
    private static final int DWT_LEVEL = 2;
    private static final int DCT_BLOCKSIZE = 4;
    private static final int PSEUDORANDOM_BOUND = 14;
    // equals to number of mid band coefficient in DCT Blocks
    // because blocksize is 4, so mid band coefficient is 7
    private static final int MID_BAND_DCT_SIZE = 7;
    private static final double GAIN_FACTOR = 0.4;

    private final DWT dwt;
    private final DCT dct;

    private Pseudorandom pseudorandomGenerator;
    private PearsonCorrelation correlation;

    public Watermarker() {
        switch (WAVELET_TYPE) {
            case HAAR:
                dwt = new HaarDWT(DWT_LEVEL);
                break;
            default:
                this.dwt = null;
                break;
        }

        this.dct = new DCT(DCT_BLOCKSIZE);
    }

    // embeddWatermark watermark image to container image and return new embedded image
    public Image embeddWatermark(Image containerImage, Image watermarkImage, int seed1, int seed2) {
        pseudorandomGenerator = new Pseudorandom(PSEUDORANDOM_BOUND);

        // DCT with
        List<Integer> pn0 = pseudorandomGenerator.getRandomNumbers(seed1, MID_BAND_DCT_SIZE);
        List<Integer> pn1 = pseudorandomGenerator.getRandomNumbers(seed2, MID_BAND_DCT_SIZE);

        double[][] containerPixels = TransformUtil.imageToPixelValues(containerImage);
        List<Integer> watermarkBinaryPixels = TransformUtil.imageToBinaryPixelValue(watermarkImage);

        return embedd(containerPixels, watermarkBinaryPixels, pn0, pn1);
    }

    private Image embedd(double[][] container, List<Integer> watermark, List<Integer> pn0, List<Integer> pn1) {
        dwt.setPixels(container);
        // Forward DWT
        dwt.transform(TransformDirection.FORWARD);
        // get sub band
        double[][] selectedSubband = dwt.getDwtSubBand(SELECTED_EMBED_SUBBAND);
        
        dct.setPixels(selectedSubband);

        dct.transform(TransformDirection.FORWARD);
        List<double[][]> embeddablePixels = dct.getEmbeddableTransformedPixels();

        for (int watermarkIndex = 0; watermarkIndex < watermark.size(); watermarkIndex++) {
            double[][] blockedPixels = embeddablePixels.get(watermarkIndex);
            List<Double> midBand = dct.getMidbandCoefficient(blockedPixels);

            // mid band should be 7 (for blocksize 4x4)
            if (midBand.size() != MID_BAND_DCT_SIZE) {
                return null;
            }

            ArrayList<Double> newMidBand = new ArrayList();
            if (watermark.get(watermarkIndex) == 0) {
                for (int midBandIndex = 0; midBandIndex < MID_BAND_DCT_SIZE; midBandIndex++) {
                    double fPn0 = pn0.get(midBandIndex) * GAIN_FACTOR;
                    double newValue = midBand.get(midBandIndex) + fPn0;

                    newMidBand.add(newValue);
                }
            } else if (watermark.get(watermarkIndex) == 1) {
                for (int midBandIndex = 0; midBandIndex < MID_BAND_DCT_SIZE; midBandIndex++) {
                    double fPn1 = pn1.get(midBandIndex) * GAIN_FACTOR;
                    double newValue = midBand.get(midBandIndex) + fPn1;

                    newMidBand.add(newValue);
                }
            }
            
            // now mid band coefficient in blockedPixels already replaced with newMidBand
            dct.setMidbandCoefficient(blockedPixels, newMidBand);

        }
        
        // new replace list of blocked pixes with embedded pixels
        dct.setEmbeddedPixels(embeddablePixels);
        dct.transform(TransformDirection.INVERSE);

        // now replace inverse dct in subband to original pixels
        dwt.setDwtSubBand(dct.getPixels(), SELECTED_EMBED_SUBBAND);
        dwt.transform(TransformDirection.INVERSE);

        double[][] afterInverseDWT = dwt.getPixels();
        
        // reset
        reset();

        return TransformUtil.pixelValuesToImage(afterInverseDWT);
    }

    // extract watermark from image
    public Image extract(int seed1, int seed2) {

        reset();
        return null;
    }

    private void reset() {
        if (pseudorandomGenerator != null) {
            pseudorandomGenerator = null;
        }
        if (correlation != null) {
            correlation = null;
        }
    }

}
