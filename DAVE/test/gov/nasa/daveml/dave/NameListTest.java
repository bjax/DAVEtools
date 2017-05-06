// NameListTest.java
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

import junit.framework.TestCase;

public class NameListTest extends TestCase {
	
	protected NameList nl;

	protected void setUp() throws Exception {
		super.setUp();
		nl = new NameList(4);
		
		nl.add("name1");
		nl.add("name2");
		nl.add("name3");
	}

	public void testFixName() {
		// does nothing - echos back proposed name
		assertEquals( "name2_1", nl.fixName("name2_1") );
	}

	public void testIsUnique() {
		assertTrue(  nl.isUnique("name4") );
		assertFalse( nl.isUnique("name3") );
	}

	public void testAddUnique() {
		nl.addUnique("name2"); // not unique
		assertEquals(4, nl.size() );
		assertEquals( "name21", nl.get(3) ); // should append "1" to end of name
		
		nl.addUnique("name21");
		assertEquals(5, nl.size() );
		assertEquals( "name211", nl.get(4) );
		
		nl.addUnique("name22");	// unique - no change
		assertEquals(6, nl.size());
		assertEquals("name22", nl.get(5) );
	}

}
