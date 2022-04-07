/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.pn;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.math3.random.RandomGenerator;

/**
 *
 * @author YUS24
 */
public interface Distribution<Value> extends Serializable {
    Map<Value, Double> sample( RandomGenerator random );
    Map<Value, Double> mean();    
}
