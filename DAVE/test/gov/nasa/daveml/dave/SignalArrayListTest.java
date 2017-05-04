// SignalArrayListTest.java
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

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class SignalArrayListTest extends TestCase {
	
	protected SignalArrayList _list;
	protected Signal _sig1;
	protected Signal _sig2;
	protected Signal _sig3;
	protected Signal _sig4;
	protected Signal _sig5;
	protected Model  _model;

	protected void setUp() throws Exception {
		super.setUp();
		
		_model = new Model(2,2);
		_list = new SignalArrayList(5);
		_sig1 = new Signal("signal_1", _model);
		_sig2 = new Signal("signal_2", _model);
		_sig3 = new Signal("signal_3", _model);
		_sig4 = new Signal("signal_4", _model);
		_sig5 = new Signal("signal_5", _model);
		_list.add(_sig1);
		_list.add(_sig2);
		_list.add(_sig3);
		_list.add(_sig4);
		_list.add(_sig5);
	}

	public void testSignalArrayList() {
		SignalArrayList list = new SignalArrayList();
		assertNotNull( list );
	}

	public void testSignalArrayListInt() {
		assertNotNull( _list );
	}

	public void testSignalArrayListCollectionOfSignal() {
		Collection<Signal> c = Arrays.asList( _sig1, _sig2, _sig3, _sig4, _sig5 );
		SignalArrayList list = new SignalArrayList(c);
		assertNotNull(list);
		checkSignalArrayList( list );
	}

	public void testFindByID() {
		checkSignalArrayList( _list );
	}
	
	protected void checkSignalArrayList( SignalArrayList list ) {
		Signal sig = null;
		Integer i = 5;
		while (i > 0) {
			String id = "signal_" + i.toString();
			sig = list.findByID(id);
			assertNotNull( sig );
			assertEquals( id, sig.getVarID() ); 
			i--;
		}
		sig = list.findByID("nuts");
		assertNull( sig );
	}
}
