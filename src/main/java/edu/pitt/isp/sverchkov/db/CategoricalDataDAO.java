/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.db;

import edu.pitt.isp.sverchkov.collections.CollectionTools;
import edu.pitt.isp.sverchkov.data.CategoricalData;
import java.sql.*;
import java.util.*;

/**
 *
 * @author YUS24
 */
public final class CategoricalDataDAO implements CategoricalData<String,String> {
    
    protected final Connection connection;
    protected final String table;
    protected final Map<String,String> constraints;
    protected boolean cacheValues = true;
    protected Map<String,List<String>> valueCache = null;
    
    public CategoricalDataDAO( Connection con, String table ){
        this( con, table, Collections.EMPTY_MAP );
    }
    
    public CategoricalDataDAO( Connection connection, String table, Map<String,String> constraints ){
        this.connection = connection;
        this.table = table;
        this.constraints = constraints;
    }

    @Override
    public int count(Map<String, String> assignment) {
        StringBuilder sql;
        SQLTools.appendWhere( (sql = new StringBuilder("select count(*) from ") ).append(table), assignment ).append(";");
        try {
            ResultSet rs = connection.createStatement().executeQuery(sql.toString());
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public Map<String, Integer> counts(String attribute, Map<String, String> assignment) {
        
        final List<String> values = values( attribute );
        Map<String, Integer> result = new HashMap<>( values.size() );
        
        for( String value : values )
            result.put( value, count( CollectionTools.immutableMapUnion( assignment, Collections.singletonMap( attribute, value ) ) ) );
        
        return Collections.unmodifiableMap( result );
    }

    @Override
    public List<String> values(String attribute) {
        List<String> result = null;
        if( !cacheValues || null == valueCache || null == (result = valueCache.get( attribute )) ){
            try {
                // Get result from DB
                StringBuilder sql = new StringBuilder("select distinct ");
                sql.append(attribute).append(" from ").append(table);
                SQLTools.appendWhere( sql, constraints );
                sql.append(";");
                
                ResultSet rs = connection.createStatement().executeQuery(sql.toString());
                
                result = new ArrayList<>(); // Might be able to get capacity from rs
                while( rs.next() )
                    result.add( rs.getString(1).intern() );
            
            } catch (SQLException ex) {
                throw new RuntimeException( ex );
            }
            
            result = Collections.unmodifiableList( result );
            
            if( cacheValues ){ // Cache this result
                if( null == valueCache ) valueCache = new HashMap<>();
                valueCache.put( attribute, result );
            }
        }
        return result;
    }

    @Override
    public List<CategoricalData<String, String>> split(String attribute) {
        List<String> values = values( attribute );
        List<CategoricalData<String,String>> result = new ArrayList<>( values.size() );
        for( String value : values ){
            Map<String,String> newConstraints = new HashMap<>(constraints);
            newConstraints.put(attribute, value);
            result.add( new CategoricalDataDAO( connection, table, Collections.unmodifiableMap( newConstraints )));
        }
        return result;
    }    
}
