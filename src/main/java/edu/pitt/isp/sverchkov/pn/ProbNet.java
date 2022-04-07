package edu.pitt.isp.sverchkov.pn;

import edu.pitt.isp.sverchkov.bn.BayesNet;
import edu.pitt.isp.sverchkov.bn.MutableBayesNet;
import edu.pitt.isp.sverchkov.collections.CollectionTools;
import edu.pitt.isp.sverchkov.collections.Pair;
import edu.pitt.isp.sverchkov.collections.Triple;
import edu.pitt.isp.sverchkov.combinatorics.Assignments;
import edu.pitt.isp.sverchkov.graph.ValueDAG;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author YUS24
 */
public class ProbNet<Variable,Value> implements ValueDAG<Variable,Value>, Serializable {
    
    private final Map<Variable, NodeHelper> nodes;
    //private final List<Pair<Distribution<Value>, List<Pair<Variable, Map<Variable,Value>>>>> distributions;
    
    public ProbNet(){
        nodes = new HashMap<>();
        //distributions = new LinkedList<>();
    }
    
    public void addNode( Variable var, Collection<? extends Value> vals ){
        nodes.put(var, new NodeHelper(vals) );
    }
    
    public void addArc( Variable parent, Variable child ){
        nodes.get( child ).add(parent);
    }
    
    public void setDistribution( Variable node, Map<Variable,Value> parentAssignment, Distribution<Value> distribution ){
        nodes.get(node).set(parentAssignment, distribution);        
    }
    
    BayesNet<Variable,Value> expectedNet( MutableBayesNet<Variable,Value> net ){
        // Assume net is empty
        for( Variable v : this )
            net.addNode(v, values(v));
        for( Map.Entry<Variable,NodeHelper> node : nodes.entrySet() ){
            for( Variable p : node.getValue().parents )
                net.addArc(p, node.getKey());
            for( Map.Entry<Map<Variable,Value>,Distribution<Value>> dist : node.getValue().distributions.entrySet() ){
                
                Assignments<Variable,Value> free;
                {
                    List<Variable> parents = new ArrayList<>(node.getValue().parents);
                    parents.removeAll(dist.getKey().keySet());
                    List<List<Value>> values = new ArrayList<>(parents.size());
                    for( Variable p : parents ) values.add( values(p) );
                    free = new Assignments(parents, values);
                }
                for( Map<Variable,Value> f : free )
                    net.setCPT(node.getKey(), CollectionTools.immutableMapUnion( f, dist.getKey() ), dist.getValue().mean() );        
            }
        }
        /*for( Pair<Distribution<Value>,List<Pair<Variable,Map<Variable,Value>>>> distribution : distributions ){
            Map<Value, Double> cpt = distribution.first.mean();
            for( Pair<Variable,Map<Variable,Value>> row : distribution.second )
                net.setCPT(row.first, row.second, cpt);
        }*/
        return net;
    }
    
    BayesNet<Variable,Value> expectedNet( final MutableBayesNet<Variable,Value> net, ExecutorService e ) throws InterruptedException{
        // Assume net is empty
        for( Variable v : this )
            net.addNode(v, values(v));
        List<Callable<Object>> tasks = new ArrayList<>( nodes.size() );
        for( final Map.Entry<Variable,NodeHelper> node : nodes.entrySet() )
            tasks.add( new Callable<Object>(){
                @Override
                public Object call(){
                    synchronized( net ){
                        for( Variable p : node.getValue().parents )
                            net.addArc(p, node.getKey());
                    }
                    for( Map.Entry<Map<Variable,Value>,Distribution<Value>> dist : node.getValue().distributions.entrySet() ){

                        Assignments<Variable,Value> free;
                        {
                            List<Variable> parents = new ArrayList<>(node.getValue().parents);
                            parents.removeAll(dist.getKey().keySet());
                            List<List<Value>> values = new ArrayList<>(parents.size());
                            for( Variable p : parents ) values.add( values(p) );
                            free = new Assignments(parents, values);
                        }
                        for( Map<Variable,Value> f : free ){
                            Variable n = node.getKey();
                            Map<Variable,Value> pa = CollectionTools.immutableMapUnion( f, dist.getKey() );
                            Map<Value, Double> pmf = dist.getValue().mean();
                            synchronized( net ){
                                net.setCPT(n, pa, pmf );
                            }
                        }
                    }
                    return null;
                }
            } );
        e.invokeAll(tasks);
        /*for( Pair<Distribution<Value>,List<Pair<Variable,Map<Variable,Value>>>> distribution : distributions ){
            Map<Value, Double> cpt = distribution.first.mean();
            for( Pair<Variable,Map<Variable,Value>> row : distribution.second )
                net.setCPT(row.first, row.second, cpt);
        }*/
        return net;
    }
    
    @Override
    public List<Value> values(Variable node) {
        return Collections.unmodifiableList( nodes.get(node).values );
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public Set<Variable> parents(Variable node) {
        return Collections.unmodifiableSet( nodes.get(node).parents );
    }

    @Override
    public Iterator<Variable> iterator() {
        return Collections.unmodifiableSet(nodes.keySet()).iterator();
    }

    @Override
    public Assignments<Variable, Value> parentAssignments(Variable node) {
        return nodes.get(node).getParentAssignments();
    }
    
    private class NodeHelper {
        NodeHelper( Collection<? extends Value> vals ){
            values = Collections.unmodifiableList( new ArrayList<>(vals) );
            parents = new HashSet<>();
            distributions = new HashMap<>();
            parentAssignments = null;
        }
        final Set<Variable> parents;
        final List<Value> values;
        final Map<Map<Variable,Value>,Distribution<Value>> distributions;
        Assignments<Variable,Value> parentAssignments;
        
        void add( Variable parent ){
            if( parents.add(parent) ) parentAssignments = null;
        }
        
        void set( Map<Variable,Value> parentAssignment, Distribution<Value> distribution ){
            /*for( Map<Variable,Value> pa : getParentAssignments() ){
                if( CollectionTools.isSubset(pa, parentAssignment ) )
                    distributions.put( pa, distribution );
            }*/
            distributions.put(parentAssignment, distribution);
        }
            
        Assignments<Variable,Value> getParentAssignments(){
            if( null == parentAssignments ){
                Map<Variable, List<Value>> valueMap = new HashMap<>(parents.size());
                for( Variable parent : parents )
                    valueMap.put(parent, values( parent ));
                parentAssignments = new Assignments<>( valueMap );
            }
            return parentAssignments;
        }
    }
}
