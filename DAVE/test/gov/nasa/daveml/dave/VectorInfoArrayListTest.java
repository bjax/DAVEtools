// VectorInfoArrayListTest.java
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

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class VectorInfoArrayListTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testVectorInfoArrayList() {
		VectorInfoArrayList vial = new VectorInfoArrayList();
		assertNotNull( vial );
	}

	public void testVectorInfoArrayListInt() {
		VectorInfoArrayList vial = new VectorInfoArrayList(50);
		assertNotNull( vial );
	}

	public void testVectorInfoArrayListCollectionOfVectorInfo() {
		VectorInfo vi1 = new VectorInfo("alpha", "ALPHA", "deg", null, false );
		VectorInfo vi2 = new VectorInfo("beta",  "BETA", "deg", null, false );
		VectorInfo vi3 = new VectorInfo("gamma", "GAMMA", "deg", null, false );
		Collection<VectorInfo> c = Arrays.asList( vi1, vi2, vi3 );
		VectorInfoArrayList vial = new VectorInfoArrayList( c );
		assertNotNull( vial );
		assertEquals( 3, vial.size() );
		VectorInfo vi = vial.get(0);
		assertNotNull( vi );
		assertEquals( "alpha", vi.getName() );
                assertEquals( "ALPHA", vi.getVarID() );
		vi = vial.get(1);
		assertNotNull( vi );
		assertEquals( "beta", vi.getName() );
                assertEquals( "BETA", vi.getVarID() );
		vi = vial.get(2);
		assertNotNull( vi );
		assertEquals( "gamma", vi.getName() );
                assertEquals( "GAMMA", vi.getVarID() );
	}

}
