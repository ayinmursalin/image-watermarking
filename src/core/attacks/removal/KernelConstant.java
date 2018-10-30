package core.attacks.removal;

public class KernelConstant {

    public static double[][] GAUSSIAN_BLUR = {
        {1, 2, 1},
        {2, 4, 2},
        {1, 2, 1}
    };

    public static double[][] UNSHARP_MASKING = {
        {1, 4.00, 6.00, 4.00, 1},
        {4, 16.0, 24.0, 16.0, 4},
        {6, 24.0, -476, 24.0, 6},
        {4, 16.0, 24.0, 16.0, 4},
        {1, 4.00, 6.00, 4.00, 1}
    };

}
