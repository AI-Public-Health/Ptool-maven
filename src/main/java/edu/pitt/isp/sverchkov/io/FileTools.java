/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.io;

import edu.pitt.isp.sverchkov.data.DataTools;
import edu.pitt.isp.sverchkov.exec.ArgParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author YUS24
 */
public class FileTools {
    
    public final static String DELIMITER_REGEX = " *, *";
    
    enum DoOp { ER2FLAT, NOOP }
    
    public static void main( String... args ) throws FileNotFoundException, IOException {
        ArgParser parser = new ArgParser( args );
        ArgParser.Parcel<DoOp> op = parser.anenum("do", DoOp.class, DoOp.NOOP);
        ArgParser.Parcel<File>
                inFile = parser.file("in"),
                outFile = parser.file("out");
        
        parser.fill();
        
        switch( op.get() ){
            case ER2FLAT:
                DataTools.saveCSV( DataTools.dataTableFromMaps( entityRelationCSV2Maps( inFile.get() )), outFile.get(), true);
        }
    }
        
    public static Collection<Map<String,String>> entityRelationCSV2Maps( File input ) throws FileNotFoundException{
        Map<String,Map<String,String>> elements = new HashMap<>();
        try( Scanner in = new Scanner( input ) ){            
            // TODO: some logic to derive ER things
            int idIndex, attrIndex, valIndex;
            {
                String[] attrs = in.nextLine().split(DELIMITER_REGEX);
                idIndex = 0;
                attrIndex = attrs.length-2;
                valIndex = attrs.length-1;
            }
            while( in.hasNextLine() ){
                String[] line = in.nextLine().split(DELIMITER_REGEX);
                Map<String,String> properties = elements.get( line[idIndex] );
                if( null == properties ){
                    properties = new HashMap<>();
                    elements.put(line[idIndex], properties);
                }
                properties.put(line[attrIndex], line[valIndex]);
            }
        }
        return elements.values();
    }
}
