/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.db;

import edu.pitt.isp.sverchkov.data.DataTable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author YUS24
 */
public class DataTableDAO implements DataTable<String,String> {
    
    protected final Connection connection;
    protected final String table;
    protected final Map<String,String> constraints;
    protected final List<String> variables;
    
    public DataTableDAO( Connection connection, String table, List<String> variables, Map<String,String> constraints ){
        this.connection = connection;
        this.table = table;
        this.constraints = constraints;
        
        List<String> tempList = new ArrayList<>(variables.size());
        for( String var : variables )
            tempList.add( var.intern() );
        this.variables = Collections.unmodifiableList( tempList );
    }

    public DataTableDAO( Connection connection, String table, List<String> variables){
        this( connection, table, variables, Collections.EMPTY_MAP );
    }

    @Override
    public List<String> variables() {
        return variables;
    }

    @Override
    public int columnCount() {
        return variables.size();
    }

    @Override
    public int rowCount() {
        try {
            ResultSet rs = connection.createStatement().executeQuery( SQLTools.appendWhere( new StringBuilder( "select count(*) from ").append(table), constraints ).append(";").toString() );
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            throw new RuntimeException( ex );
        }        
    }

    @Override
    public void addRow(List<? extends String> row) {
        throw new UnsupportedOperationException("Not supported. This implementation is read-only.");
    }

    @Override
    public Iterator<List<String>> iterator() {
        try {
            StringBuilder sql = new StringBuilder("select ");
            boolean first = true;
            for( String var : variables ){
                if( !first )
                    sql.append(", ");
                else
                    first = false;
                sql.append( var );
            }
            sql.append(" from ").append(table);
            SQLTools.appendWhere(sql, constraints);
            sql.append(";");
            return new RSIterator( connection.createStatement().executeQuery( sql.toString() ) );
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private class RSIterator implements Iterator<List<String>> {
        
        private boolean atEnd = false;
        private boolean atNext = false;
        private List<String> next;
        private final ResultSet rs;
        
        private RSIterator( ResultSet rs ){
            this.rs = rs;
        }

        @Override
        public boolean hasNext() {
            moveToNext();
            return !atEnd;
        }

        @Override
        public List<String> next() {
            moveToNext();
            atNext = false;
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported. This implementation is read-only.");
        }
        
        private void moveToNext(){
            if( !atEnd && !atNext ){
                try {
                    atEnd = rs.next();
                    int m = rs.getMetaData().getColumnCount();
                    next = new ArrayList<>( m );
                    for( int i=1; i<=m; i++ ){
                        String thing = rs.getString(i);
                        if( thing == null ){ thing = "null"; System.out.println("Null value!");}
                        thing = thing.intern();
                        next.add( thing );
                    }
                    atNext = true;
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }                
            }
        }
    }
}
