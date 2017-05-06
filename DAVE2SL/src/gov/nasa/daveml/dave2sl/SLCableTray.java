// SLCableTray.java
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
//
//	020621 E. B. Jackson <bruce@digiflightdyn.com>
//

package gov.nasa.daveml.dave2sl;

import java.util.ArrayList;

/**
 *
 * Extends ArrayList for new add capability, where
 * duplicates are not allowed.
 *
 * <p>
 * Modification history:
 *  <ul>
 *   <li>020621 Written</li>
 *   <li>040227 Updated for version 0.5</li>
 *  </ul>
 *
 * @author Bruce Jackson <a href="mailto:bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>}
 * @version 0.9
 *
 **/

@SuppressWarnings("serial")
public class SLCableTray extends ArrayList<Object>
{

	/**
	 *  line offset for each item in tray
	 */

	static int offset = 5;


	/**
	 *
	 * Constructor.
	 *
	 **/

	public SLCableTray()
	{
		super();
	}


	/**
	 *
	 * Constructor, with initializing number of elements.
	 * @param count An initial number of slots to reserve
	 **/

	public SLCableTray( int count )
	{
            super( count );
	}


	/**
	 *
	 * Overrides ArrayList add method to prevent duplicates.
	 *
	 * @param theObject the object to be added to the tray.
	 *
	 **/

	public boolean add(Object theObject)
	{
		if( !this.contains( theObject ) )
			return super.add( theObject );
		return false;
	}


	/**
	 *
	 * Returns the width/height of the tray, based on the number of
	 * signals carried in the tray.
	 *
	 * @return the size of the tray.
	 *
	 **/

	public int getSize()
	{
		if( this.size() > 0)
			return (this.size()-1)*offset;
		else
			return 0;
	}


	/**
	 *
	 * Returns the distance within the tray of a particular signal
	 * from the left/top edge of the tray.
	 * @param theSignal the {@link SLSignal} to evaluate
	 * @return the standoff distance of the signal line
	 *
	 **/

        public int getStandoff( SLSignal theSignal ) {
            return this.indexOf( theSignal )*offset;
	}
}
