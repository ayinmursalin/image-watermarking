package core.transform.dwt;

// basis class of Discrete Wavelet Transform
import core.transform.TransformDirection;

public abstract class DWT {
    
    /**
     * Mother of Wavelet
     */
    public enum MotherOfWavelet {
        HAAR;
    }
    
    /**
     * Set image pixels to process
     * 
     * @param pixels Original pixels of Image
     */
    public abstract void setPixels(double[][] pixels);
    
    /**
     * Get the pixels that already process (after inverse)
     * 
     * @return inverse pixels
     */
    public abstract double[][] getPixels();

    /**
     * Transforming process (this can be access)
     *
     * @param direction direction of DWT (forward or reverse)
     */
    public abstract void transform(TransformDirection direction);

    /**
     * Forward process (protected, only child can be implement or call)
     */
    protected abstract void forwardDwt();

    /**
     * Inverse process (protected, only child can be implement or call)
     */
    protected abstract void inverseDwt();

    /**
     * Transforming process (this can be access)
     *
     * @param subBand enum of Sub-band that available on DWT (depends on cycle)
     * @return return new transformed or reversed pixels
     */
    public abstract double[][] getDwtSubBand(DwtSubBand subBand);

    /**
     * Transforming process (this can be access)
     *
     * @param newSubbandValue sub transformed pixels (will be replace into
     * transformedFullPixels)
     * @param subBand enum of Sub-band that available on DWT (depends on cycle)
     */
    public abstract void setDwtSubBand(double[][] newSubbandValue, DwtSubBand subBand);
    
    /**
     * Clear or reset any state
     */
    public abstract void clear();

}
