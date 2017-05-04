// CEquationsFileWriter.java
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, originally of NASA LaRC, now at
//  Adaptive Aerospace Group, Inc. <bjackson@adaptiveaero.com>
//
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://github.com/bjax/DAVEtools
//
//  Original version of DAVEtools, prior to version 0.9.8: Copyright (c) 2007 United States
//  Government as represented by LAR-17460-1. No copyright is claimed in the United States under
//  Title 17, U.S. Code. All Other Rights Reserved.
//
//  Copyright (c) 2017 Adaptive Aerospace Group, Inc.

package gov.nasa.daveml.dave2post;

import gov.nasa.daveml.dave.*;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author ebjackso
 */
class CEquationsFileWriter extends FileWriter {
    
    Model ourModel;

    public CEquationsFileWriter(Model theModel, String sourceFileName) throws IOException {
        super( sourceFileName );
        ourModel = theModel;
    }

    void generateTableCall(BlockFuncTable bft) {
        System.out.println("      clf4 = gentab (gs.motbl.clf3t(1))");
    }
    
}
