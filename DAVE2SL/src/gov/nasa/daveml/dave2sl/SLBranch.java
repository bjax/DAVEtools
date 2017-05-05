// SLBranch.java
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

package gov.nasa.daveml.dave2sl;

import java.util.ArrayList;

/**
 * <p>
 * The SLBranch contains a list of SLLineSegments that run from the output port of 
 * one block to the input port of another block.
 * </p>
 *
 *<p> 
 * Modification history: 
 * <ul>
 *  <li>2010-05-04: Written to simplify SLSignal and SLDiagram classes</li>
 * </ul>
 *
 * @author Bruce Jackson <a href="mailto:bjackson@adaptiveaero.com">bjackson@adaptiveaero.com</a>}
 * @since 0.9
 * @version 0.9
 *
 **/

public class SLBranch extends ArrayList<SLLineSegment>
{
	/*
	 * Set the initial number of segments
	 */
	public SLBranch(int i) {
		super(i);
	}

	/**
	 * Not sure why Eclipse wants this to be present
	 */
	private static final long serialVersionUID = -1954668026239970685L;

}
