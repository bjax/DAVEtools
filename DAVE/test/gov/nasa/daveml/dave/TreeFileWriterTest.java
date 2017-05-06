// TreeFileWriter.java
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

public class TreeFileWriterTest extends TestCase {
	
	Model _m;
	TreeFileWriter _w;
	String _fileName;

	protected void setUp() throws Exception {
		super.setUp();
		_m = genSampleModel();
		_fileName = "tempFile.txt";
		_w = new TreeFileWriter( _fileName );
	}

	public void testTreeFileWriter() {
		assertNotNull( _w );
	}

	public void testDescribe() {
		try {
			_w.describe(_m);
		} catch (IOException e) {
			fail("Unexpected exception thrown in describe(): " + e.getMessage() );
		}
		
		// close and reopen file
		try {
			_w.close();
		} catch (IOException e) {
			fail("Unexpected exception thrown in close(): " + e.getMessage() );
		}
		
		FileReader f = null;
		try {
			f = new FileReader(_fileName);
		} catch (FileNotFoundException e) {
			fail("Unexpected exception thrown in creating FileReader: " + e.getMessage() );
		}
		
		assertNotNull(f);
		char[] buf = new char[1000];
		try {
			f.read(buf);
		} catch (IOException e) {
			fail("Unexpected exception thrown in reading file: " + e.getMessage() );
		}
		String contents = new String(buf);
		int end = contents.lastIndexOf('\n');  // funny chars at end of string
		String expectedMessage = 
			"Contents of model:\n" +
			"\n" +
                        "Number of inputs: 0\n" +
                        "\n" +
                        "\n" +
                        "Number of outputs: 1\n" +
                        "\n" +
                        "1 theSig (unkn)\n" +
                        "\n" +
			"Number of signals: 1\n" +
			"\n" +
			"1 Signal \"theSig\" (unkn) [theSig] connects outport 1 of block const_0 " +
			"to inport 1 of block theSig.\n" +
			"\n" +
			"Number of blocks: 2\n" +
			"\n" +
			"1 Block \"const_0\" has NO INPUTS, one output (theSig), value [3.4] " +
			"and is a Constant Value math block.\n" +
			"2 Block \"theSig\" has one input (theSig), NO OUTPUTS, value [NaN] (unkn) " +
			"and is an output block.";
		
		assertEquals( expectedMessage, contents.substring(0, end ));
	}
	
	protected Model genSampleModel() {
		Model m = new Model(3,3);
		Block constBlk = new BlockMathConstant( "3.4", m );
		Signal sig = new Signal("theSig", m);
		try {
			constBlk.addOutput(sig);
		} catch (DAVEException e) {
			fail("Unexpected exception thrown in adding signal to const block in genSampleModel(): " 
					+ e.getMessage() );
		}
		new BlockOutput(sig, m);
		return m;
	}

}
