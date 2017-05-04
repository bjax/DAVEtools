// CodeAndVarNamesTest.java
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

package gov.nasa.daveml.dave;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ebjackso
 */
public class CodeAndVarNamesTest {
    
    CodeAndVarNames cvn0, cvn1, cvn2;
    
    public CodeAndVarNamesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        cvn0 = new CodeAndVarNames();
        cvn1 = new CodeAndVarNames("alpha is a variable\n");
        cvn1.addVarName("alpha");
        cvn2 = new CodeAndVarNames("beta is a variable\n");
        cvn2.addVarName("beta");        
    }

    /**
     * Test of append method, of class CodeAndVarNames.
     */
    @Test
    public void testAppend() {
        cvn1.append(cvn2);
        assertEquals("alpha is a variable\nbeta is a variable\n", cvn1.getCode());
        assertEquals(2, cvn1.getVarNames().size());
        assertEquals("alpha", cvn1.getVarName(0));
        assertEquals("beta",  cvn1.getVarName(1));
        assertEquals("beta is a variable\n", cvn2.getCode());
        assertEquals(1, cvn2.getVarNames().size());
        assertEquals("beta",  cvn2.getVarName(0));
        
        cvn0.append(cvn1);
        assertEquals("alpha is a variable\nbeta is a variable\n", cvn0.getCode());
        assertEquals(2, cvn0.getVarNames().size());
        assertEquals("alpha", cvn0.getVarName(0));
        assertEquals("beta",  cvn0.getVarName(1));
        
        assertEquals("alpha is a variable\nbeta is a variable\n", cvn1.getCode());
        assertEquals(2, cvn1.getVarNames().size());
        assertEquals("alpha", cvn1.getVarName(0));
        assertEquals("beta",  cvn1.getVarName(1));

        assertEquals("beta is a variable\n", cvn2.getCode());
        assertEquals(1, cvn2.getVarNames().size());
        assertEquals("beta",  cvn2.getVarName(0));
}

    /**
     * Test of appendCode method, of class CodeAndVarNames.
     */
    @Test
    public void testAppendCode() {
        cvn1.appendCode("here is some code\n");
        assertEquals("alpha is a variable\nhere is some code\n", cvn1.getCode());
        assertEquals(1, cvn1.getVarNames().size());
        assertEquals("alpha", cvn1.getVarName(0));
        assertEquals(1, cvn2.getVarNames().size());
        assertEquals("beta",  cvn2.getVarName(0));
    }

    /**
     * Test of prependCode method, of class CodeAndVarNames.
     */
    @Test
    public void testPrependCode() {
        cvn1.prependCode("here is some code\n");
        assertEquals("here is some code\nalpha is a variable\n", cvn1.getCode());
        assertEquals(1, cvn1.getVarNames().size());
        assertEquals("alpha", cvn1.getVarName(0));
        assertEquals(1, cvn2.getVarNames().size());
        assertEquals("beta",  cvn2.getVarName(0));
    }

    /**
     * Test of addVarName method, of class CodeAndVarNames.
     */
    @Test
    public void testAddVarName() {
        cvn1.addVarName("beta");
        
        assertEquals(2, cvn1.getVarNames().size());
        assertEquals("alpha", cvn1.getVarName(0));
        assertEquals("beta",  cvn1.getVarName(1));

        assertEquals(1, cvn2.getVarNames().size());
        assertEquals("beta",  cvn2.getVarName(0));
    }

    /**
     * Test of getVarNames method, of class CodeAndVarNames.
     */
    @Test
    public void testGetVarNames() {
        ArrayList<String> varNames;
        
        varNames = cvn0.getVarNames();
        assertNotNull(varNames);
        assertEquals(0, varNames.size());

        varNames = cvn1.getVarNames();
        assertEquals(1, varNames.size());
        assertEquals("alpha", varNames.get(0));
        
        varNames = cvn2.getVarNames();
        assertEquals(1, varNames.size());
        assertEquals("beta",  varNames.get(0));
    }

    /**
     * Test of getVarName method, of class CodeAndVarNames.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetVarName() {
        assertEquals(1, cvn1.getVarNames().size());
        assertEquals("alpha", cvn1.getVarName(0));
        
        assertEquals(1, cvn2.getVarNames().size());
        assertEquals("beta",  cvn2.getVarName(0));
        
        String varName = cvn1.getVarName(1); // should throw exception
   }

    /**
     * Test of getCode method, of class CodeAndVarNames.
     */
    @Test
    public void testGetCode() {
        assertEquals("",                      cvn0.getCode());
        assertEquals("alpha is a variable\n", cvn1.getCode());
        assertEquals("beta is a variable\n",  cvn2.getCode());
    }
}
