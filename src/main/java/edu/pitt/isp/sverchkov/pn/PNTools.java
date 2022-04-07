/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.pn;

import edu.pitt.isp.sverchkov.bn.BayesNet;
import edu.pitt.isp.sverchkov.smile.SMILEBayesNet;

/**
 *
 * @author YUS24
 */
public class PNTools {
    public static BayesNet<String,String> expectedNetwork( ProbNet<String,String> source ){
        return source.expectedNet( new SMILEBayesNet(true) );
    }
}
