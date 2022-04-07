/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.random;

import edu.pitt.isp.sverchkov.math.MathTools;
import java.util.Random;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 *
 * @author YUS24
 */
public class ExtendedSample {
    
    public static double[] dirichlet( double[] alphas, Random random ){
        return dirichlet( alphas, wrap( random ) );
    }
    
    public static double[] dirichlet( double[] alphas, RandomGenerator r ){
        double[] gammas = new double[alphas.length];
        for( int i = 0; i < alphas.length; i++ ){
            RealDistribution gamma = new GammaDistribution( r, alphas[i], 1, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY );
            gammas[i] = gamma.sample();
        }
        return MathTools.arrayOperate(gammas, MathTools.MULTIPLY, 1/MathTools.sum(gammas));
    }
    
    public static RandomGenerator wrap( final Random random ){
        return new RandomGenerator(){
            @Override
            public void setSeed(int seed) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setSeed(int[] seed) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setSeed(long seed) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void nextBytes(byte[] bytes) {
                random.nextBytes(bytes);
            }

            @Override
            public int nextInt() {
                return random.nextInt();
            }

            @Override
            public int nextInt(int n) {
                return random.nextInt(n);
            }

            @Override
            public long nextLong() {
                return random.nextLong();
            }

            @Override
            public boolean nextBoolean() {
                return random.nextBoolean();
            }

            @Override
            public float nextFloat() {
                return random.nextFloat();
            }

            @Override
            public double nextDouble() {
                return random.nextDouble();
            }

            @Override
            public double nextGaussian() {
                return random.nextGaussian();
            }
        };
    }
}
