/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.edgr;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author YUS24
 */
public interface BDDDResult<Variable> extends List<Variable> {
    /**
     * @param v
     * @return Posterior odds of v being different.
     */
    double getVariableScore( Variable v );
    Collection<Variable> getUnionParents( Variable v );
    Collection<Variable> getCommonGroupParents( Variable v );
    Collection<Variable> getAllGroupParents( Variable v );
    Collection<Variable> getGroupParents( Variable v, int group );
}
