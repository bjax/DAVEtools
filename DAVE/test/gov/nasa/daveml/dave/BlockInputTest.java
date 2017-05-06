// BlockInputTest.java
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, originally of NASA LaRC, now at
//  Digital Flight Dynamics <bruce@digiflightdyn.com>
//
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://github.com/bjax/DAVEtools
//
//  Original version of DAVEtools, prior to version 0.9.8: Copyright (c) 2007 United States
//  Government as represented by LAR-17460-1. No copyright is claimed in the United States under
//  Title 17, U.S. Code. All Other Rights Reserved.
//
//  Copyright (c) 2017 Digital Flight Dynamics

package gov.nasa.daveml.dave;


import java.io.IOException;
import java.io.StringWriter;

import junit.framework.*;


public class BlockInputTest extends TestCase {

	private Signal _fs;
	private BlockInput _bi;
	private Model _m;
	private StringWriter _writer;

	protected void setUp() throws Exception {
		_m  = new Model(2,2);
		_fs = new Signal( "full signal", "fulsig", "deg", 4, _m );
		_bi = new BlockInput( _fs, _m );
		_writer = new StringWriter();
		try {
			_m.initialize();
		} catch (DAVEException e) {
			assertTrue(false);
		}
		_bi.update();
	}
	
	public void testConstructor() {
		Model m = new Model(1,1);
		assertTrue( m != null );
		Signal s = new Signal("my_signal", m);
		assertTrue( s != null );
		
		Block b = new BlockInput(s, m);
		assertTrue(                b != null );
		assertEquals( "input",     b.getType() );
		assertEquals( "my_signal", b.getName() );
		assertEquals( 0,           b.numInputs() );
		assertEquals( 0,           b.numVarIDs() );
		assertTrue(                b.outputConnected() );
		assertEquals( "my_signal", b.getOutputVarID() );
		assertEquals( s,           b.getOutput() );
		
		assertEquals( 1, m.getNumBlocks() );
	}
	
	public void testIsReady() {
		assertTrue( _bi.isReady() );  // input block should always be ready
	}
        
        public void testHasIC() {
            assertFalse( _bi.hasIC() ); // downstream signal does not have IC
        }
	
	public void testAllInputsReady() {
		assertTrue( _bi.allInputsReady() );  // input block should always be ready
	}
	
	public void testVerbosity() {
		assertFalse( _bi.isVerbose() );
		_bi.makeVerbose();
		assertTrue( _bi.isVerbose() );
		_bi.silence();
		assertFalse( _bi.isVerbose() );
	}
	
	public void testSetInputValue( ) {
		_bi.setInputValue( -4.533 );
		assertEquals( -4.533, _bi.getValue(), 0.0000001 );
	}
	
	public void testGetUnits( ) {
		assertEquals( "deg", _bi.getUnits() );
	}
	
	public void testGetSeqNumber() {
		assertEquals( 1, _bi.getSeqNumber() );
	}
	
	public void testGetOutputVarID() {
		assertEquals( "fulsig", _bi.getOutputVarID() );
	}
        
        public void testGenCcode() {
            _m.setCodeDialect(Model.DT_ANSI_C);
            CodeAndVarNames cvn = new CodeAndVarNames();
            cvn.append(_bi.genCode());
            assertEquals("/* fulsig is a model input with units 'deg' */\n", cvn.getCode());
            assertEquals("fulsig", cvn.getVarName(0));
        }

        public void testGenFcode() {
            _m.setCodeDialect(Model.DT_FORTRAN);
            CodeAndVarNames cvn = new CodeAndVarNames();
            cvn.append(_bi.genCode());
            assertEquals("!  fulsig is a model input with units 'deg'\n", cvn.getCode());
            assertEquals("fulsig", cvn.getVarName(0));
        }
	
	public void testDescribeSelf() {
		try {
			_bi.describeSelf(_writer);
		} catch (IOException e) {
			assertTrue(false);
		}
		assertEquals( "Block \"full signal\" has NO INPUTS, one output (full signal), value [NaN] (deg)", 
				_writer.toString() );
	}

	public static Test suite() {
		return new TestSuite( BlockInputTest.class );
	}

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
