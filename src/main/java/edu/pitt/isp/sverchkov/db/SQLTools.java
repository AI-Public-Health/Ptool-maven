/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.db;

import java.util.Map;

/**
 *
 * @author YUS24
 */
public class SQLTools {
    protected static StringBuilder appendWhere( StringBuilder app, Map<String,String> constraints ){
        if( null != constraints && constraints.size() > 0 ){
            app.append(" where");
            for( Map.Entry<String,String> entry : constraints.entrySet() )
                app
                        .append(" ")
                        .append(entry.getKey())
                        .append("='")
                        .append(entry.getValue())
                        .append("'");
        }
        return app;
    }
}
