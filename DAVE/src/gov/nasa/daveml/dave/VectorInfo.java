// VectorInfo
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
 * Object providing information about an input or output Signal to a
 * <code>Model</code>
 *
 * @author Bruce Jackson <a href="mailto:bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>
 *
 **/

public class VectorInfo
{
	/**
	 *  Name of input or output block
	 */

	String  signalName;

	/**
	 *  ID of signal
	 */

        String  varID;

	/**
	 *  units of measure
	 */

	String  myUnits;

	/**
	 *  if true, we're an input
	 */

	boolean isInput;

	/**
	 *  source block (if output)
	 */

	Block   source;

	/**
	 *  sink block (if input)
	 */

	Block   sink;

	/**
	 *  input or output value
	 */

	double  value;

	/**
	 *  verification tolerance value
	 */

	double  tolerance;


	/**
	 * <p> Elementary constructor </p>
	 **/

	public VectorInfo()
	{
		this.signalName =  "";
		this.varID = "";
		this.myUnits = "";
		this.source = null;
		this.sink = null;
		this.value = Double.NaN;
		this.tolerance = Double.NaN;
	}


	/**
	 *
	 * Simple constructor (builds from scratch)
	 *
	 * @param signalName A <code>String</code> containing the name of
	 * the signal to construct
         * @param id String with the variable ID we represent
	 * @param units Our units-of-measure
	 * @param blk The source or sink <code>Block</code> we represent
	 * @param isInput If true, we represent an input
	 *
	 **/

        public VectorInfo(String signalName, String id, String units, Block blk, boolean isInput)
	{
		this(); // initialize tol, e.g.
		this.signalName = signalName;
		this.varID = id;
		this.myUnits = units;
		if (isInput) {
                    this.isInput = true;
                    this.sink = blk;
                    this.source = null;
                    if (blk != null) {
                        BlockInput ib;
                        ib = (BlockInput) blk;
                        if (ib.hasIC()) {
                            this.value = ib.getValue();
                        }
                    }
		} else {
			this.isInput = false;
			this.sink = null;
			this.source = blk;
		}
	}


	/**
	 * <p> Set the name of the signal </p>
	 *
	 * @param theName <code>String</code> to use for name
	 *
	 **/

	//private void setName( String theName ) { this.signalName = theName; }


	/**
	 *
	 * <p> Set the units of the signal </p>
	 *
	 * @param theUnits <code>String</code> to use for name
	 *
	 **/

	public void setUnits( String theUnits ) { this.myUnits = theUnits; }


	/**
	 *
	 * Set the value of the signal
	 *
	 * @param theValue value to remember
	 *
	 **/

	public void setValue( double theValue ) { this.value = theValue; }


	/**
	 *
	 * Set the value of the signal
	 *
	 * @param theValue value to remember
	 *
	 **/

	public void setValue( String theValue ) { this.value = Double.parseDouble( theValue ); }


	/**
	 *
	 * <p> Sets the verification tolerance of the signal </p>
	 *
	 * @param theValue <code>double</code> value to remember
	 *
	 **/

	public void setTolerance( double theValue ) { this.tolerance = theValue; }


	/**
	 *
	 * <p> Sets the verification tolerance of the signal </p>
	 *
	 * @param theValue <code>double</code> value to remember
	 *
	 **/

	public void setTolerance( String theValue ) { this.tolerance = Double.parseDouble( theValue ); }


	/**
	 *
	 * Returns signal name as <code>String</code>
         * @return name of Signal as <code>String</code>
	 *
	 **/

	public String getName() { return this.signalName; }

	/**
	 *
	 * Returns signal ID as <code>String</code>
         * @return ID of signal as <code>String</code>
	 *
	 **/

	public String getVarID() { return this.varID; }

	/**
	 *
	 * Returns variable name as <code>String</code>
         * @return a String containing units-of-measure encoded per
         * ANSI/AIAA-S-119-2011
	 *
	 **/

	public String getUnits() { return this.myUnits; }


	/**
	 *
	 * Returns the value of this element
         * @return double value of this element
	 *
	 **/

	public double getValue() { return this.value; }


	/**
	 *
	 * Returns the verification tolerance of this element. Only
	 * makes sense for outputs; default value for inputs is <code>NaN</code>.
         * @return double with the verification tolerance for this element
	 *
	 **/

	public double getTolerance() { return this.tolerance; }

    
	/**
	 *
	 * Indicates if we're an input or not
	 * @return boolean <code>true</code> if we're an input;
	 * otherwise <code>false</code>
         *
	 **/

	public boolean isInput() { return this.isInput; }


	/**
	 *
	 * Returns source block for output values
         * @return BlockOutput where we obtain our calculated output value (output vectors only)
	 *
	 **/

	public BlockOutput getSource() { return (BlockOutput) this.source; }


	/**
	 *
	 * Returns input block we overwrite for input vector elements
         * @return BlockInput where we overwrite the current value (input vectors only)
	 *
	 **/

	public BlockInput getSink() { return (BlockInput) this.sink; }

}

