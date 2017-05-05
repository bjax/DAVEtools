// CheckData
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

/**
 *
 * Check case data for self-verification
 *
 * @author Bruce Jackson, AAG
 *
 **/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

/**
 * 
 * A class to handle checkcases found in DAVE-ML files
 * 
 * @since DAVE_tools 0.4
 * @author Bruce Jackson
 * 040202: Written, EBJ
 *
 **/

public class CheckData
{
    /**
     *  composed of StaticShot objects
     */

    ArrayList<StaticShot> staticShots;

    /** 
     *
     * Constructor to built from JDOM Elements
     * @param shots a List of source file defined Elements from which
     * to construct the CheckData
     *
     **/

    public CheckData( List<Element> shots )
    {
	staticShots = new ArrayList<StaticShot>( 20);
	Iterator<Element> ssit = shots.iterator();
	while (ssit.hasNext()) {
	    staticShots.add( new StaticShot( ssit.next() ) );
        }
    }


    /**
     *
     * Return the array of checkcases
     * @return an ArrayList of StaticShots defined by the model source
     *
     **/

    public ArrayList<StaticShot> getStaticShots() { return this.staticShots; }

    /**
     *
     * Return the count of checkcases
     * @return the number of StaticShots defined by the model source
     *
     **/

    public int count() { return this.staticShots.size(); }

}

