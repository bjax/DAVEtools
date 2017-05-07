// BlockLimiter
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
 * Object representing a two-sided limiter block
 * <p>
 * 2010-12-15 Bruce Jackson, NASA Langley Research Center
 *     &lt;mailto:bruce@digiflightdyn.com&gt;
 * @author Bruce Jackson
 *
 **/

import java.io.IOException;
import java.io.Writer;

/**
 *
 * The Limiter Block provides upper and lower limits to an input signal.
 *
 **/
public class BlockLimiter extends Block {
    /**
     *
     *  units of measure of downstream block
     *
     */
    String units;

    /**
     *
     * lower limit, or -Inf
     *
     */
    Double lowerLim;

    /**
     *
     * upper limit, or +Inf
     *
     */
    Double upperLim;

    /**
     *
     * indicates presence of lower limit (not -Inf)
     *
     */
    boolean hasLowerLim;

    /**
     *
     * indicates presence of valid upper limit (not +Inf)
     *
     */
    boolean hasUpperLim;



    /**
     *
     * Constructor for output Block
     * @param sourceSignal Upstream {@link Signal} with which to connect
     * @param m {@link Model} we're part of
     * @param lowerLimit <code>String</code> representing the minimum value we can pass (-Infinity means no limit)
     * @param upperLimit <code>String</code> representing the maximum value we can pass (+Infinity means no limit)
     *
     **/
    public BlockLimiter( Signal sourceSignal, Model m, double lowerLimit, double upperLimit )
    {
        // Initialize superblock elements
        super(sourceSignal.getName()+ " limiter", "limiter", 1, m);

        // record our U of M
        this.units = sourceSignal.getUnits();

        // record limits
        lowerLim = lowerLimit;
        upperLim = upperLimit;

        // ensure correct order
        if (lowerLim > upperLim) {
            Double temp = lowerLim;
            lowerLim = upperLim;
            upperLim = temp;
        }

        // set limiting flags
        hasLowerLim = !lowerLim.isInfinite();
        hasUpperLim = !upperLim.isInfinite();

        // hook up to upstream signal
        //System.out.println("    BlockOutput constructor: " + myName + " is hooking up to signal " + sourceSignal.getName());
        sourceSignal.addSink( this, 1 );
        //System.out.println("    BlockOutput constructor: " + myName + " as output " + seqNumber);
    }


    /**
     *
     * Returns the output value
     * <p>
     * This method is distinguished from normal
     * {@link Block.getValue()} in that it is public
     * @return the output value of this limiter as a <code>double</code>
     *
     **/
    @Override
    public double getValue()    { return this.value; }


    /**
     *
     * Returns the units of measure of the output signal
     * @return String with units of measure (encoded per ANSI/AIAA
     * S-119-2011) of output signal
     *
     **/
    public String getUnits() { return this.units; }
    
    /**
     *
     * Generates source code for all code types
     * @return {link @CodeAndVarNames} with source code to apply limits to input
     *
     */
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        String rel;
        Signal input;
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (outputSig != null) {
            if (!outputSig.isDerived()) {
//                cvn.code = "// Code for variable \"" + outVarID + "\":\n";
                cvn.appendCode(indent() + outVarID + " = ");
                cvn.addVarName(outVarID);
            }
        }
        input = inputs.get(0);
        int dialect = ourModel.getCodeDialect();
        cvn.append( input.genCode() );
        if (this.hasLowerLimit()) {
            cvn.appendCode(endLine());
            rel = " < "; // default is DT_ANSI_C
            if (dialect == Model.DT_FORTRAN) {
                rel = " .LT. ";
            }
            cvn.appendCode(beginIf( outVarID + rel + lowerLim.toString() ));
            cvn.appendCode(indent() + "  " + outVarID + " = " + lowerLim.toString());
            cvn.appendCode(endLine());
            cvn.appendCode(endIf());
        }
        if (this.hasUpperLimit()) {
            if (!this.hasLowerLimit()) {
                cvn.appendCode(endLine()); // don't issue blank line
            }
            rel = " > "; // default is DT_ANSI_C
            if (dialect == Model.DT_FORTRAN) {
                rel = " .GT. ";
            }
            cvn.appendCode(beginIf( outVarID + rel + upperLim.toString() ));
            cvn.appendCode(indent() + "  " + outVarID + " = " + upperLim.toString());
            cvn.appendCode(endLine());
            cvn.appendCode(endIf());
        }
        // if not derived, need trailing semicolon and new line if no limits
        if (outputSig != null) {
            if (!outputSig.isDerived()) {
                if (!this.hasLowerLimit() && !this.hasUpperLimit() ) {
                    cvn.appendCode(endLine());
                }
            }
        }
        return cvn;
    }


    /**
     *
     * Generates description of self
     * @throws IOException if problems encountered when writing description
     *
     **/
    @Override
    public void describeSelf(Writer writer) throws IOException
    {
        super.describeSelf(writer);
        writer.write(" (" + units + ") and is a limiter block with " +
                "a lower limit of " + lowerLim +
                " and an upper limit of " + upperLim + ".");
    }

    /**
     *
     * Implements update() method
     * @throws DAVEException if update fails
     *
     **/
    @Override
    public void update() throws DAVEException
    {
        if (isVerbose()) {
            System.out.println();
            System.out.println("Method update() called for limiter block '" + 
                    this.getName() + "'");
        }

        // Check to see if only one input
        if (this.inputs.size() < 1) {
            throw new DAVEException(" Limiter block " + this.myName + 
                    " has no input.");
        }
        if (this.inputs.size() > 1) {
            throw new DAVEException(" Limiter block " + this.myName + 
                    " has more than one input.");
        }
        // see if single input variable is ready
        Signal theInput = this.inputs.get(0);
        if (!theInput.sourceReady()) {
            if (this.isVerbose()) {
                System.out.println(" Upstream signal '" + theInput.getName() + 
                        "' is not ready.");
            }
            return;
        }

        // get single input variable value
        double inputValue = theInput.sourceValue();
        if (this.isVerbose()) {
            System.out.println(" Input value is " + inputValue);
        }

        // show ourselves up-to-date
        this.resultsCycleCount = ourModel.getCycleCounter();

        // save answer
        this.value = inputValue;
        if (hasLowerLim) {
            if( this.value < lowerLim ) {
                this.value = lowerLim;
            }
        }
        if (hasUpperLim) {
            if( this.value > upperLim ) {
                this.value = upperLim;
            }
        }

    }

    /**
     *
     * Indicates a practical lower limit has been defined
     * @return true if lower limit exists
     *
     */
    public boolean hasLowerLimit() { return hasLowerLim; }

    /**
     *
     * Indicates a practical upper limit has been defined
     * @return true if upper limit exists
     *
     */
    public boolean hasUpperLimit() { return hasUpperLim; }

    /**
     *
     * Returns the value of the lower limit
     * @return lower limit as a double
     *
     */
    public double getLowerLimit() { return lowerLim.doubleValue(); }

    /**
     *
     * Returns the value of the upper limit
     * @return upper limit as a double
     *
     */
    public double getUpperLimit() { return upperLim.doubleValue(); }

}
