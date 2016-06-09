// BlockMath
//
//  Part of DAVE-ML utility suite, written by Bruce Jackson, NASA LaRC
//  <bruce.jackson@nasa.gov>
//  Visit <http://daveml.org> for more info.
//  Latest version can be downloaded from http://dscb.larc.nasa.gov/Products/SW/DAVEtools.html
//  Copyright (c) 2007 United States Government as represented by LAR-17460-1. No copyright is
//  claimed in the United States under Title 17, U.S. Code. All Other Rights Reserved.

package gov.nasa.daveml.dave;

/**
 *
 * <p> Superclass representing an arbitrary math function </p>
 * <p> 031212 Bruce Jackson <mailto:bruce.jackson@nasa.gov> </p>
 *
 **/

import java.util.Iterator;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 *
 * <p>  The Math block represents an arbitrary math function </p>
 *
 **/

abstract public class BlockMath extends Block
{
    
    /**
     *  variable ID
     */

    static String variableID;

    /**
     *
     * <p>Basic constructor for math Block <p>
     *
     **/

    public BlockMath()
    {
	// Initialize Block elements
	super();
        variableID = "";
    }

    /**
     *
     * <p> Constructor for math Block <p>
     *
     * @param m <code>Model</code> we're part of
     *
     **/

    public BlockMath( Model m)
    {
	// Initialize Block elements
	super(m);
        variableID = "";
    }


    /**
     *
     * <p> Constructor for math Block <p>
     *
     * @param blockName our name
     * @param blockType our type
     * @param m <code>Model</code> we're part of
     *
     **/

    public BlockMath(String blockName, String blockType, Model m)
    {
	// Initialize Block elements
	super(blockName, blockType, 1, m); 
        variableID = "";
    }


    /**
     *
     * <p> Constructor for math Block <p>
     *
     * @param blockName our name
     * @param blockType our type
     * @param numInputs how many inputs we have
     * @param m <code>Model</code> we're part of
     *
     **/

    public BlockMath(String blockName, String blockType, int numInputs, Model m)
    {
	// Initialize Block elements
	super(blockName, blockType, numInputs, m); 
        variableID = "";
    }
    
    /**
     *  Put variable name on error stream
     * @param errorMsg Message to be preceded with variableID
     * @since 0.9.8
     */
    
    static public void printErrWithVarID( String errorMsg ) {
        System.err.println("In calculations for variable '" + variableID + "':");
        System.err.println( errorMsg );
    }


    /**
     *
     * <p> Determines block type and calls appropriate constructor </p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m		The parent <code>Model</code>
     * @return Math block of appropriate type
     * @throws DAVEException if problems were encountered
     *
     **/

    @SuppressWarnings("unchecked")
    public static BlockMath factory( Element applyElement, Model m)
           throws DAVEException
    {
//System.out.println("    BlockMath factory called.");
	// Parse parts of the Apply element
	List<Element> kids = applyElement.getChildren();
	Iterator<Element> ikid = kids.iterator();

	// first element should be our type
	Element first  = ikid.next();
	String theType = first.getName();
	
        // find the variable's ID
	Element varDef;
        varDef = applyElement.getParentElement().getParentElement().getParentElement();
        Attribute varID;
        varID = varDef.getAttribute("varID");
        if (varID != null) {
            variableID = varID.getValue();
        }
	
// take appropriate action based on type
	if( theType.equals("abs") ) {
	    return new BlockMathAbs( applyElement, m );
        }
	if( theType.equals("lt" ) ||
	    theType.equals("leq") ||
	    theType.equals("eq" ) ||
	    theType.equals("geq") ||
	    theType.equals("gt" ) ||
	    theType.equals("neq") ) {
	    return new BlockMathRelation( applyElement, m );
        }
        if( theType.equals("not") ||
            theType.equals("or" ) ||
            theType.equals("and") ) {
	    return new BlockMathLogic( applyElement, m );
        }
        if( theType.equals("minus") ) {
            return new BlockMathMinus( applyElement, m );
        }
	if( theType.equals("piecewise") ) {
	    return new BlockMathSwitch( applyElement, m );
        }
	if( theType.equals("plus") ) {
	    return new BlockMathSum( applyElement, m );
        }
	if( theType.equals("times") ||
	    theType.equals("quotient") ||
	    theType.equals("divide") ) {
	    return new BlockMathProduct( applyElement, m );
        }
        if( theType.equals("max"    ) ||
            theType.equals("min"    ) ) {
            return new BlockMathMinmax( applyElement, m );
        }
	if( theType.equals("power"  ) ||
	    theType.equals("sin"    ) ||
	    theType.equals("cos"    ) ||
	    theType.equals("tan"    ) ||
	    theType.equals("arcsin" ) ||
	    theType.equals("arccos" ) ||
	    theType.equals("arctan" ) ||
            theType.equals("ceiling") ||
            theType.equals("floor"  ) ) {
		try {
			return new BlockMathFunction( applyElement, m);
		} catch (DAVEException e) {
			printErrWithVarID("Exception when trying to build a math function of type '"
					+ theType + "' - which is unrecognized. Aborting...");
			System.exit(-1);
		}
        }
	if( theType.equals("csymbol") ) {
	    return new BlockMathFunctionExtension( applyElement, m);
        }

        printErrWithVarID("  DAVE's MathML implementation doesn't allow a <"
                + theType + "> element directly after an <apply> element");
        return null;
    }

    /**
     *
     * <p> Finds or generates appropriate inputs from math constructs </p>
     * 
     * @param ikid List <code>Iterator</code> for elements of top-level &lt;apply&gt;.
     * @param inputPortNumber <code>Int</code> with 1-based input number
     *
     **/

    public void genInputsFromApply( Iterator<Element> ikid, int inputPortNumber ) 
    {
	int i = inputPortNumber;
	while( ikid.hasNext() ) {	    
            // look at each input
            Element in  = ikid.next();
            String name = in.getName();
            //		if (this.getName().equals("divide_4"))
            //		    System.out.println("*-*-*-* In building block '" + this.getName() 
            //				       + "' I found input math type " + name + "... adding as input " + i);
            // is it single scalar variable name <ci>?
            if( name.equals("ci") ) {
                String varID = in.getTextTrim();	// get variable name
                this.addVarID(i, varID);		// add it to proper input
                //this.hookUpInput(i++);		// and hook up to signal, if defined.
                // this is now done later
            } else if( name.equals("cn") ) { // or maybe a constant value?               
                String constantValue = in.getTextTrim();	// get constant value
//		this.addVarID(i, constantValue);		// add it as input - placeholder, not needed
                this.addConstInput(constantValue, i);		// Create and hook up constant block
            } else if( name.equals("apply") ) { // recurse
                this.addVarID(i, "");				// placeholder - no longer needed
                // next throws DAVEException if bad syntax in <apply>. Catch and abend here
                // so we don't have to change the many calling routines.
                Signal s = null;
                try {
                    s = new Signal(in, ourModel);       // Signal constructor recognizes <apply>...
                } catch (DAVEException e) {
                    printErrWithVarID("Bad syntax while parsing <apply> in element '" +
                            in.getName() + "'. Aborting.");
                }
                                                        // .. and will call our BlockMath.factory() ...
                if( s!= null )	 {			// .. and creates upstream blocks & signals
                    s.addSink(this,i);			// hook us up as output of new signal path
                    s.setDerivedFlag();	// Note that this is a newly-created signal not part of orig model
                } else {
                    printErrWithVarID("Null signal returned when creating recursive math element.");
                }
            } else {
                printErrWithVarID("BlockMath didn't find usable element (something like 'apply', 'ci' or 'cn'),"
                                   + " instead found: " + in.getName());
            }
            i++;
        }
    }
}
