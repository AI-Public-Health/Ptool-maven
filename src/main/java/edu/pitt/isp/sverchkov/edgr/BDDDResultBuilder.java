/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.edgr;

import java.util.Collection;

/**
 *
 * @author YUS24
 */
public interface BDDDResultBuilder<Variable> {
    BDDDResultBuilder<Variable> setNGroups( int n );
    BDDDResultBuilder<Variable> addVariable(Variable var, Collection<? extends Variable> unionParents, Collection<? extends Collection<? extends Variable>> groupParents, double score );
    BDDDResult<Variable> result();
}
