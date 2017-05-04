// BlockMathConstant
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
 * Object representing a constant value in a {@link Model}.
 * 
 * Modification history: 
 * <ul> 
 * <li>031214 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </li> 
 * </ul>
 */

import java.io.IOException;
import java.io.Writer;

/**
 * The constant block represents a constant value source.
 **/

public class BlockMathConstant extends BlockMath
{
    /**
     * The value of this constant
     */

    String stringValue;

    /**
     * BlockMathConstant: Constructor for constant value Block.
     *
     * @param constantValue {@link String} containing value of constant
     * @param m Our parent {@link Model}
     **/

    public BlockMathConstant( String constantValue, Model m )
    {
	// Initialize superblock elements
	super("const_" + m.getNumBlocks(), "constant value", m);
	this.setValue(constantValue);
    }


    /**
     * Returns our numeric value.
     * 
     * @return double containing the current output value
     **/

    public double getValueAsDouble() { return this.value; }


    /**
     * Returns our string value.
     *
     * @return String representing the current output value
     **/

    public String getValueAsString() { return this.stringValue; }


    /**
     * Sets our value from a string.
     * 
     * If the provided String can't be converted to a double, we emit an error
     * message on System.err and exit the program.
     * 
     * @param newValue is a String containing a representation of the value of the 
     * constant to which we're set.
     */
    
    public final void setValue( String newValue ) {
    	this.stringValue = newValue;

    	try {
    	    this.value = Double.parseDouble(stringValue);
    	} catch (java.lang.NumberFormatException e) {
    	    System.err.println("Encountered content-number element <cn> containing a string ('"
    			       + newValue + "') that cannot be "
    			       + "cannot be converted into a number; mislabeled "
    			       + "content-identifier element <ci>? DAVE unable to continue.");
    	    System.exit(0);
    	}
    }
    
    /**
     * Sets our value from a Double.
     * 
     * @param newValue is the value of the constant to which we're to be set
     */
    
    public void setValue( Double newValue ) {
    	this.value = newValue;
    	this.stringValue = newValue.toString();
    }
    
    /**
     * Generate C-code equivalent of our constant.
     * 
     * @return CodeAndVarNames object containing a C source code representation 
     *         of our constant value
     */
    
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        Signal outputSig = this.getOutput();
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived()) {
//            code = "// Code for variable \"" + outVarID + "\":\n";
            cvn.appendCode(indent() + outVarID + " = ");
            cvn.addVarName(outVarID);
        }
        
        cvn.appendCode(this.getValueAsString());
        
        // if not derived, need trailing semicolon and new line
        if (!outputSig.isDerived()) {
            cvn.appendCode(endLine());
        }
        return cvn;
    }

        
    /**
     * Generates description of self.
     * 
     * @throws IOException if problems encountered writing description
     **/

    @Override
    public void describeSelf(Writer writer) throws IOException
    {
	super.describeSelf(writer);
	writer.write(" and is a Constant Value math block.");
    }

    /**
     * Implements update() method.
     * 
     * If the verbose flag is on, generate a narrative on System.out.
     * 
     * @throws DAVEException if the block has any inputs (it should be a constant)
     **/

    @Override
    public void update() throws DAVEException
    {
	if (isVerbose()) {
	    System.out.println();
	    System.out.println("Method update() called for constant block '" + this.getName() + "'");
	}
	
	// Check to see if no input
	if (this.inputs.size() > 0) {
	    throw new DAVEException("Constant block " + this.myName + " has more than zero inputs.");
        }
	// record current cycle counter
	resultsCycleCount = ourModel.getCycleCounter();
    }
    
    /**
    *
    * Indicates if all results are up-to-date.
    * 
    * Constant blocks are always ready!
    *
    * @return true always
    **/

    @Override
   public boolean isReady()
   {
	return true;
   }
}
