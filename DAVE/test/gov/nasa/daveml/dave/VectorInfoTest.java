// VectorInfoTest.java
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

import org.jdom.Element;

import junit.framework.TestCase;
import gov.nasa.daveml.dave.VectorInfo;

public class VectorInfoTest extends TestCase {
	
	protected VectorInfo _vi;
	protected Model _model;
	protected VectorInfoArrayList _inputs;
	protected VectorInfoArrayList _outputs;
	private final double EPS = 0.00000001;

	protected void setUp() throws Exception {
		super.setUp();
		_vi = new VectorInfo();
		_model = genSampleModel();
		_inputs = _model.getInputVector();
		_outputs = _model.getOutputVector();
	}

	public void testVectorInfo() {
		assertNotNull( _vi );
		assertEquals( "", _vi.getName() );
                assertEquals( "", _vi.getVarID() );
		assertEquals( "", _vi.getUnits() );
		assertEquals( null, _vi.getSource() );
		assertEquals( null, _vi.getSink() );
		assertEquals( Double.NaN, _vi.getValue() );
		assertEquals( Double.NaN, _vi.getTolerance() );
	}

	public void testVectorInfoStringStringBlockBoolean() {
		_vi = new VectorInfo("alpha", "ALPHA", "deg", null, true );
		assertNotNull( _vi );
		assertEquals( "alpha", _vi.getName() );
                assertEquals( "ALPHA", _vi.getVarID() );
		assertEquals( "deg", _vi.getUnits() );
		assertEquals( null, _vi.getSource() );
		assertEquals( null, _vi.getSink() );
		assertEquals( Double.NaN, _vi.getValue() );
		assertEquals( Double.NaN, _vi.getTolerance() );
	}

	public void testSetUnits() {
		_vi.setUnits( "newUnits" );
		assertEquals( "newUnits", _vi.getUnits() );
	}

	public void testSetValueDouble() {
		_vi.setValue( -4.323 );
		assertEquals( -4.323, _vi.getValue(), EPS );
	}

	public void testSetValueString() {
		_vi.setValue("-77.323" );
		assertEquals( -77.323, _vi.getValue(), EPS );
	}

	public void testSetToleranceDouble() {
		_vi.setTolerance( -4.323 );
		assertEquals(     -4.323, _vi.getTolerance(), EPS );
	}

	public void testSetToleranceString() {
		_vi.setTolerance("-77.323" );
		assertEquals(     -77.323, _vi.getTolerance(), EPS );
	}

	public void testIsInput() {
		assertNotNull(_inputs);
		VectorInfo input0 = _inputs.get(0);
		assertTrue( input0.isInput() );
	}

	public void testGetSource() {
		VectorInfo output0 = _outputs.get(0);
		assertFalse( output0.isInput() );
		BlockOutput outblk = output0.getSource();
		assertNotNull( outblk );
		assertEquals( "output", outblk.getType() );
	}

	public void testGetSink() {
		VectorInfo input0 = _inputs.get(0);
		BlockInput inblk = input0.getSink();
		assertNotNull( inblk );
		assertEquals( "input", inblk.getType() );
	}
	
	protected Model genSampleModel() {
		// build an minus value apply element
        //   <apply>
        //     <minus/>
        //     <ci>input</ci>
        //   </apply>

        Element theValue = new Element("ci");   // add numeric constant
        theValue.addContent( "input" );

        Element minus = new Element("minus");
 
        Element applyElement = new Element("apply");
        applyElement.addContent( minus );
        applyElement.addContent( theValue );

		Model m = new Model(3,3);
		Signal in = new Signal("input", m);
		Signal out = new Signal("output", m);
		new BlockInput( in, m );
		new BlockOutput(out, m);
		Block gain = new BlockMathMinus(applyElement, m);
		try {
			gain.addOutput( out );
		} catch (DAVEException e) {
			fail("Unexpected exception in genSampleModel(): " + e.getMessage() );
		}
		try {
			gain.hookUp();
		} catch (DAVEException e) {
			fail("Unexpected exception in genSampleModel(): " + e.getMessage() );
		}
		return m;
	}


}
