// DAVEException
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

/**
 *
 * Exception class for DAVE package
 * <p>
 * Simple wraps the standard Java <code>Exception</code>
 * @author Bruce Jackson, Digital Flight Dynamics
 * <a href="mailto:bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>
 * @version 0.9
 * @since 0.4
 *
 **/

@SuppressWarnings("serial")
public class DAVEException extends Exception {
    
    /**
     *
     * Default constructor for DAVEException
     *
     */
    public DAVEException() {
	super();
    }

    /**
     *
     * Constructor with message
     * @param s <code>String</code> with the exception message
     *
     */
    public DAVEException(String s) {
	super(s);
    }
}
