/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.edgr;

import edu.pitt.isp.sverchkov.collections.ArrayMap;
import java.io.Serializable;
import java.util.*;

/**
 *
 * @author YUS24
 */
public class BDDDResultImpl<Variable extends Serializable & Comparable<Variable>> extends AbstractList<Variable> implements BDDDResult<Variable>, Serializable {

    private class Helper implements Serializable {
        private final Set<Variable> unionParents;
        private final List<Set<Variable>> groupParents;
        private final double score;

        public Helper(Set<Variable> unionParents, List<Set<Variable>> groupParents, double score) {
            this.unionParents = unionParents;
            this.groupParents = groupParents;
            this.score = score;
        }
        public Helper() {
            unionParents = null;
            {
                List<Set<Variable>> list = new ArrayList<>( nGroups );
                for( int i=0; i<nGroups; i++ ) list.add( new HashSet<Variable>() );
                groupParents = Collections.unmodifiableList( list );
            }
            score = 0;
        }
    }
    
    //private final Variable groupLabel;
    private final int nGroups;
    private final ArrayMap<Variable,Helper> variables;
    
    public BDDDResultImpl(){
        nGroups = 0;
        variables = null;
    }

    public BDDDResultImpl( int n ){
        nGroups = n;
        variables = new ArrayMap<>();
    }
    
    @Override
    public Iterator<Variable> iterator() {
        return variables.keySet().iterator();
    }

    @Override
    public int size() {
        return variables.size();
    }

    @Override
    public double getVariableScore(Variable v) {
        return variables.get(v).score;
    }

    @Override
    public Collection<Variable> getUnionParents(Variable v) {
        return variables.get(v).unionParents;
    }

    @Override
    public Collection<Variable> getCommonGroupParents(Variable v) {
        Set<Variable> result = null;
        for( Set<Variable> s : variables.get(v).groupParents )
            if( null != s && !s.isEmpty() ){
                if( null == result ) result = new HashSet<>(s);
                else result.retainAll(s);
            }
        return null == result ? Collections.EMPTY_SET : result;        
    }
    
    @Override
    public Collection<Variable> getAllGroupParents(Variable v) {
        Set<Variable> result = null;
        for( Set<Variable> s : variables.get(v).groupParents )
            if( null != s && !s.isEmpty() ){
                if( null == result ) result = new HashSet<>(s);
                else result.addAll(s);
            }
        return null == result ? Collections.EMPTY_SET : result;        
    }

    @Override
    public Collection<Variable> getGroupParents(Variable v, int group) {
        return variables.get(v).groupParents.get(group);
    }    

    @Override
    public Variable get(int index) {
        return variables.get(index).getKey();
    }
    
    private Helper newHelper( Collection<? extends Variable> unionParents, Collection<? extends Collection<? extends Variable>> groupParents, double score ){
        Set<Variable> uP = new HashSet<>(unionParents);
        List<Set<Variable>> gP = new ArrayList<>(nGroups);
        for( Collection<? extends Variable> parentSet : groupParents )
            gP.add( new HashSet<>(parentSet));
        return new Helper( uP, gP, score );
    }
    
    public static class Builder<Variable extends Serializable & Comparable<Variable>> implements BDDDResultBuilder<Variable> {
    
        private BDDDResultImpl<Variable> result;

        @Override
        public BDDDResultBuilder<Variable> setNGroups( int n ) {
            result = new BDDDResultImpl<>(n);
            return this;
        }

        @Override
        public BDDDResultBuilder<Variable> addVariable(Variable var, Collection<? extends Variable> unionParents, Collection<? extends Collection<? extends Variable>> groupParents, double score) {
            result.variables.put(var, result.newHelper( unionParents, groupParents, score) );
            return this;
        }

        @Override
        public BDDDResult<Variable> result() {
            return result;
        }
    }
}
