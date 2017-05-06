// BlockOutputTest.java
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

import junit.framework.TestCase;

public class BlockOutputTest extends TestCase {
	
	private Model _model;
	private String _signalID;
	private Signal _signal;
	private BlockMathConstant _constBlock;
	private BlockOutput _block;
	
	protected StringWriter _writer;
	
	private final double EPS = 0.00000001;

    @Override
	protected void setUp() throws Exception {
		super.setUp();
		
		_writer = new StringWriter();

		_model = new Model(4,4);
		_signalID = "outputValue";
		_signal = new Signal(_signalID, _model);
		_constBlock = new BlockMathConstant("3.45", _model);
		_constBlock.addOutput(_signal);
		assertNotNull(_constBlock);
		_model.hookUpIO();
		assertTrue(_signal.hasDest() );
		BlockArrayList destBlocks = _signal.getDests();
		assertNotNull ( destBlocks );
		assertEquals( 1, destBlocks.size() );
		Block destBlock = destBlocks.get(0);
		assertNotNull( destBlock );
		assertEquals( "output", destBlock.getType() );
		_block = (BlockOutput) destBlock;
		assertEquals( Double.NaN, _block.getValue() );
		try {
			_model.initialize();
		} catch (DAVEException e) {
			fail("Unexpected exception in TestBlockOutput.testGetValue() wen calling model.initialize(): " 
					+ e.getMessage());
		}
	}

	public void testGetValue() {
		assertEquals( 3.45, _block.getValue(), EPS );
	}

	public void testUpdate() {
		try {
			_model.getInputVector();
		} catch (DAVEException e) {
			fail("Unexpected exception in TestBlockOutput.testGetValue() wen calling model.getInputVector(): " 
					+ e.getMessage());
		}
		try {
			_model.cycle();
		} catch (DAVEException e) {
			fail("Unexpected exception in TestBlockOutput.testGetValue() wen calling model.cycle(): " 
					+ e.getMessage());
		}
		assertEquals( 3.45, _block.getValue(), EPS );
		_constBlock.setValue("-999.345");
		try {
			_model.cycle();
		} catch (DAVEException e) {
			fail("Unexpected exception in TestBlockOutput.testGetValue() wen calling model.cycle(): " 
					+ e.getMessage());
			assertEquals( -999.345, _block.getValue(), EPS );
		}
	}
        
        public void testGenCcode() {
            _model.setCodeDialect(Model.DT_ANSI_C);
            CodeAndVarNames cvn = _block.genCode();
            assertEquals("/* outputValue is a model output with units 'unkn' */\n", 
                    cvn.getCode());
        }

        public void testGenFcode() {
            _model.setCodeDialect(Model.DT_FORTRAN);
            CodeAndVarNames cvn = _block.genCode();
            assertEquals("!  outputValue is a model output with units 'unkn'\n", 
                    cvn.getCode());
        }

	public void testGetUnits() {
		assertEquals("unkn", _block.getUnits() );
	}

	public void testGetSeqNumber() {
		assertEquals(1, _block.getSeqNumber() );
	}

	public void testDescribeSelfWriter() {
		try {
			_block.describeSelf(_writer);
		} catch (IOException e) {
			fail("testDescribeSelfWriter of TestBlockMathSum threw unexpected exception: " 
					+ e.getMessage() );
		}
		assertEquals( "Block \"outputValue\" has one input (outputValue)," +
				" NO OUTPUTS, value [3.45] (unkn) and is an output block.", 
				_writer.toString() );
	}

}
