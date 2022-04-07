/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.edgr;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author yus24
 */
public class BDDDResultMk2<Variable extends Serializable> extends AbstractList<Variable> implements BDDDResult<Variable>, Serializable {
    
    private final List<Variable> vars;
    private final double[] scores;
    private final Helper[] data;
    private final int nGroups;
    private final int nVars;
    
    private static class Helper implements Serializable {
        final BitSet[] groupParentPtrs;
        final BitSet unionParentPtrs;
        Helper( final int nGroups, final int nVars ){
            groupParentPtrs = new BitSet[nGroups];
            for( int g = 0; g < nGroups; g++ )
                groupParentPtrs[g] = new BitSet(nVars);
            unionParentPtrs = new BitSet(nVars);
        }
    }
    
    private BDDDResultMk2( final int nGroups, final int nVars ){
        vars = new ArrayList<>(nVars);
        this.nGroups = nGroups;
        this.nVars = nVars;
        scores = new double[nVars];
        data = new Helper[nVars];
        for( int v = 0; v < nVars; v++ )
            data[v] = new Helper(nGroups, nVars);
    }

    @Override
    public Variable get(int index) {
        return vars.get(index);
    }

    @Override
    public int size() {
        return vars.size();
    }

    @Override
    public double getVariableScore(Variable v) {
        return scores[indexOf(v)];
    }

    @Override
    public Collection<Variable> getUnionParents(Variable v) {
        return varsFromBits( data[indexOf(v)].unionParentPtrs );
    }

    @Override
    public Collection<Variable> getCommonGroupParents(Variable v) {
        Helper h = data[indexOf(v)];
        BitSet common = null;
        for( BitSet bs : h.groupParentPtrs )
            if( null == common ) common = (BitSet) bs.clone();
            else common.and(bs);
        return varsFromBits( common );
    }

    @Override
    public Collection<Variable> getAllGroupParents(Variable v) {
        Helper h = data[indexOf(v)];
        BitSet all = null;
        for( BitSet bs : h.groupParentPtrs )
            if( null == all ) all = (BitSet) bs.clone();
            else all.or(bs);
        return varsFromBits( all );
    }

    @Override
    public Collection<Variable> getGroupParents(Variable v, int group) {
        return varsFromBits( data[indexOf(v)].groupParentPtrs[group] );
    }
    
    @Override
    public int indexOf( Object v ){
        return vars.indexOf( v );
    }
    
    private List<Variable> varsFromBits( BitSet bs ){
        List<Variable> result = new ArrayList<>(bs.cardinality());
        for( int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1) )
            result.add(vars.get(i));
        return result;
    }
    
    public static class Builder<Variable extends Serializable> implements BDDDResultBuilder<Variable> {
        private int nGroups;
        private List<BuildHelper> vars = new ArrayList<>();
        @Override
        public BDDDResultBuilder<Variable> setNGroups(int n) {
            nGroups = n;
            return this;
        }

        @Override
        public BDDDResultBuilder<Variable> addVariable(Variable var, Collection<? extends Variable> unionParents, Collection<? extends Collection<? extends Variable>> groupParents, double score) {
            BuildHelper h = new BuildHelper();
            h.var = var;
            h.unionParents = unionParents;
            if( groupParents.size() != nGroups ) throw new IllegalArgumentException();
            h.groupParents = new ArrayList<>(groupParents);
            h.score = score;
            vars.add(h);
            return this;
        }

        @Override
        public BDDDResult<Variable> result() {
            BDDDResultMk2<Variable> result = new BDDDResultMk2<>( nGroups, vars.size() );
            for( BuildHelper h : vars )
                result.vars.add(h.var);
            for( int i = 0; i < result.nVars; i++ ){
                BuildHelper h = vars.get(i);
                result.scores[i] = h.score;
                for( int j = 0; j < result.nVars; j++ ){
                    Variable other = result.get(j);
                    if( h.unionParents.contains( other ) )
                        result.data[i].unionParentPtrs.set(j);
                    for( int g = 0; g < nGroups; g++ )
                        if( h.groupParents.get(g).contains( other ) )
                            result.data[i].groupParentPtrs[g].set(j);
                }
            }
            return result;
        }
        private class BuildHelper{
            Variable var;
            Collection<? extends Variable> unionParents;
            List<? extends Collection<? extends Variable>> groupParents;
            double score;
        }
    }
}
