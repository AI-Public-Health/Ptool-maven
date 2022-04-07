/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.pn;

import edu.pitt.isp.sverchkov.collections.ArrayTools;
import edu.pitt.isp.sverchkov.collections.CollectionTools;
import edu.pitt.isp.sverchkov.math.MathTools;
import edu.pitt.isp.sverchkov.random.ExtendedSample;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.math3.random.RandomGenerator;

/**
 *
 * @author YUS24
 */
public class Dirichlet<Value> implements Distribution<Value> {
    
    private final List<Value> values;
    private final double[] alphas;
    
    public Dirichlet( List<Value> values, List<Double> alphas ){
        this.values = values;
        this.alphas = ArrayTools.primitiveArray(alphas);
    }

    @Override
    public Map<Value,Double> sample( RandomGenerator random ){
        return CollectionTools.zipToMap(
                values,
                ArrayTools.wrapperList( ExtendedSample.dirichlet(alphas, random) ),
                values.size() );
    }
    
    public Map<Value,Double> mean(){
        return CollectionTools.zipToMap(
                values,
                ArrayTools.wrapperList( MathTools.arrayOperate(
                        alphas,
                        MathTools.MULTIPLY,
                        1/MathTools.sum(alphas) ) ),
                values.size() );
    }
}
