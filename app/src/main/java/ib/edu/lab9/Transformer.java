package ib.edu.lab9;

/**
 * Created by Juju on 09.01.2019.
 */

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class Transformer {
    public static double[] computeFFT(double[] input) {

        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] complexResults = transformer.transform(input, TransformType.FORWARD);//Simple Fourier Transformate
        double output[] = new double[complexResults.length]; //Results table,apparently complex
        for (int i = 0; i < complexResults.length; i++) {
            //Modulo from imaginary and real numbers
            output[i] = Math.sqrt(Math.pow(complexResults[i].getReal(), 2) + Math.pow(complexResults[i].getImaginary(), 2));
        }
        return output;
    }
}