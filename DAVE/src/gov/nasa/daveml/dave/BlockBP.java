// BlockBP
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
 * <p> Object representing a breakpoint block </p>
 * <p> 031214 Bruce Jackson <mailto:bruce@digiflightdyn.com> </p>
 *
 **/

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 *
 * <p>  The BP Block represents a breakpoint set. </p>
 *
 **/

public class BlockBP extends Block
{
    /**
     * The breakpoint set we use
     */

    BreakpointSet bpSet;

    /**
     *
     * Constructor that builds BP block from scratch parts.
     * <p> 
     * An associated <code>BreakpointSet</code> must be available.
     *
     * @param bpID  the ID of the associated breakpoint set to reference
     * @param ourName <code>String</code> with the name of this new <code>BlockBP</code>
     * @param inSignal <code>Signal</code> wire that connects to independent variable source
     * @param outSignal <code>Signal</code> wire that connects to function table block
     * @param m Our parent {@link Model}
     *
     * @throws DAVEException if bpID not found or if unable to connect to output signal
     *
     * @see BreakpointSet
     **/

    public BlockBP( String bpID, String ourName, Signal inSignal, Signal outSignal, Model m) throws DAVEException
    {

	// Initialize superblock elements & connect to model
	super(ourName, "breakpoint normalizer", 1, m);

	// find breakpoint set
	this.bpSet = m.getBPSetByID( bpID );
	if (this.bpSet == null) {
	    throw new DAVEException("BlockBP constructor: Unable to find breakpoint set with ID '" + bpID + "'.");
	}

                   
        // register with breakpoint set
        this.bpSet.register( this );

        // register with model
        ourModel.register( this );

        // Connect to upstream signal
        inSignal.addSink( this, 1 );

	// Connect to downstream signal
	try {
	    this.addOutput(outSignal);
	} catch (Exception e) {
	    throw new DAVEException("Unexpected error: new BP block '" + this.getName() 
			       + "' unable to hook up to downstream signal :" + e.getMessage() );
	}
    }


    /**
     * Return our bpID
     * @return String our bpID
     **/

    public String getBPID() { return this.bpSet.getBPID(); }

    /**
     * Update our output value
     * @throws DAVEException if a problem is encountered when parsing
     **/

    @Override
    public void update() throws DAVEException {
    
	Signal theInput;
	double inputValue;
	double bpValue;
	double prevValue;
	double frac = 0;
	int index = 0;

	if (isVerbose()) {
	    System.out.println();
	    System.out.println("Method update() called for breakpoint block '" + 
                    this.getName() + "'");
	}

	this.value = Double.NaN;	// to flag when not successfully calculated
	
	// Check to see if only one input
	if (this.inputs.size() < 1) {
	    throw new DAVEException("Breakpoint block " + this.myName + 
                    " has no input.");
        }
	if (this.inputs.size() > 1) {
	    throw new DAVEException("Breakpoint block " + this.myName + 
                    " has more than one input.");
        }
	// Check to see that the sole input is non-null
	theInput = this.inputs.get(0);
	if (theInput == null) {
	    throw new DAVEException("Breakpoint block " + this.myName + 
                    " found unexpected null pointer for its input.");
        }
	// see if single input variable is ready
	if (!theInput.sourceReady()) {
	    if (this.isVerbose()) {
		System.out.println(" Upstream signal '" + theInput.getName() + 
                        "' is not ready.");
            }
	    return;
	}

	// get single input variable value
	inputValue = theInput.sourceValue();
	if (this.isVerbose()) {
	    System.out.println(" Input value is " + inputValue);
        }
	// calculate index-and-fraction

	// find nearest breakpoint
	Iterator<Double> bpIt = this.bpSet.values().iterator();
	if (!bpIt.hasNext()) {
	    throw new DAVEException("Breakpoint block " + this.myName + 
                    " has no breakpoints.");
        }

	bpValue = bpIt.next();	// get 0 index value
	prevValue = bpValue;

	if (this.isVerbose()) {
	    System.out.println(" 0-index breakpoint set value is " + prevValue);
        }

	// pathological case - lowest breakpoint value above inputValue
	if (bpValue > inputValue) {
	    this.value = 0;
	    frac = 0;
	    if (this.isVerbose()) {
		System.out.println(" Input value of " + inputValue +
                        " is lower than lowest breakpoint set entry of " + 
                        bpValue);
            }
	} else {	// search for breakpoint just higher than inputValue
	    while (bpIt.hasNext()) {
		bpValue = (bpIt.next()).doubleValue();
		if (this.isVerbose()) {
		    System.out.println(" Found next bp value of " + bpValue +
				       "; prev bp is " + prevValue);
                }
		if (bpValue > inputValue) {
		    this.value = (double) index;
		    frac = (inputValue - prevValue)/(bpValue - prevValue);
		    if (this.isVerbose()) {
			System.out.println(" Found next-higher breakpt of "
					   + bpValue + " at index " + index);
			System.out.println(" Prev breakpt was " + prevValue);
			System.out.println(" Fraction computed to be " + frac);
		    }
		    break;	// jump out when next-higher breakpt found
		} else {
		    index++;
		    prevValue = bpValue;
		}
	    }
        }  // else clause

	// pathological case - highest breakpoint below inputValue
	if (bpValue <= inputValue) {
	    this.value = this.bpSet.length()-1;
	    frac = 0;
	}

	// record current cycle counter
	resultsCycleCount = ourModel.getCycleCounter();

	// save answer
	this.value = this.value + frac;
	if (isVerbose()) { 
            System.out.println(" Index-and-weight set to " + this.value);
        }
    }

    /**
     * Returns length of breakpoint vector
     * @return int with number of breakpoint values
     **/

    public int length() {
	return this.bpSet.length();
    }


    /**
     * Returns our breakpoint set
     * @return BreakpointSet
     **/

    public BreakpointSet getBPset() { return this.bpSet; }


    /**
     * Generates written description of current instance on output stream
     *
     * @param writer FileWriter instance to write to
     * @throws IOException when writing
     *
     **/

    public void describeSelf(Writer writer) throws IOException
    {
	super.describeSelf(writer);
	writer.write(" and is a breakpoint block with " + this.length() + " breakpoints.");
    }
}
