// BlockMathRelation
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
 * <p> Relational operator math function block </p>
 * <p> 031214 Bruce Jackson <mailto:bjackson@adaptiveaero.com> </p>
 *
 **/

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;


/**
 *
 * <p>  The MathRelation block represents an Relational operator (scalar input) function</p>
 *
 **/

public class BlockMathRelation extends BlockMath
{
    /**
     * Defined relations, for speed of execution
     **/

    private static final int UNK = 0;
    private static final int LT  = 1;
    private static final int LEQ = 2;
    private static final int EQ  = 3;
    private static final int GEQ = 4;
    private static final int GT  = 5;
    private static final int NEQ = 6;

    /**
     * Which algebraic relation we're testing
     */

    String relationOp;

    /**
     * Which algebraic relation we're testing, using encoded value from internal table
     */

    int relation;
    
    /**
     *
     * <p> Constructor for relation Block <p>
     *
     * @param applyElement Reference to <code>org.jdom.Element</code>
     * containing "apply" element
     * @param m         The parent <code>Model</code>
     *
     **/

    @SuppressWarnings("unchecked")
        public BlockMathRelation( Element applyElement, Model m )
    {
        // Initialize superblock elements
        super("pending", "relation", m);

        this.relation = UNK;

        // Parse parts of the Apply element
        List<Element> kids = applyElement.getChildren();
        Iterator<Element> ikid = kids.iterator();

        // first element should be our type; also use for name
        Element first = ikid.next();
        this.relationOp = first.getName();
        this.setName( this.relationOp + "_" + m.getNumBlocks() );
        
        // take appropriate action based on type
        if(!validateRelation()) {
            System.err.println("Error - BlockMathRelation constructor called with" +
                                   " unknown relation operator " + this.relationOp);
        } else {
            // look for two inputs
            if(kids.size() != 3) {
                System.err.println("Error - <apply><[relation]/> only handles 2 arguments, not " + 
                        (kids.size()-1));
            } else {
                this.genInputsFromApply(ikid, 1);
            }
        }

        //System.out.println("    BlockMathRelation constructor: " + this.getName() + " created.");
    }


    /**
     *
     * <p> Determines if string is acceptable MathML relation </p>
     *
     * @return <code>boolean</code> true if is a relation
     *
     **/

    protected final boolean validateRelation( )
    {
    	this.relation = UNK;
    	
        if (this.relationOp.equals("lt" )) { this.relation = LT;   }
        if (this.relationOp.equals("leq")) { this.relation = LEQ;  }
        if (this.relationOp.equals("eq" )) { this.relation = EQ;   }
        if (this.relationOp.equals("geq")) { this.relation = GEQ;  }
        if (this.relationOp.equals("gt" )) { this.relation = GT;   }
        if (this.relationOp.equals("neq")) { this.relation = NEQ;  }

        return (this.relation != UNK);
    }


    /**
     * <p> Generate code equivalent of a relationship test </p>
     */
    
    @Override
    public CodeAndVarNames genCode() {
        CodeAndVarNames cvn = new CodeAndVarNames();
        Signal outputSig = this.getOutput();
        
        // check to see if we're derived variable (code fragment) or a whole statement
        // if not derived, need preceding command and the LHS of the equation too
        if (!outputSig.isDerived()) {
            cvn.appendCode(indent() + outVarID + " = ");
            cvn.addVarName(outVarID);
        }
        
        if (inputs == null) {
            cvn.appendCode(errorComment(
                    "in BlockMathRelation genCode(): encountered null input list."));
            return cvn;
        }
        
        if (inputs.size() < 2) {
            cvn.appendCode(errorComment(
                    "in BlockMathRelation genFcode(): encountered input list" +
                    " with less than the expected three elements."));
            return cvn;
        }
        Signal arg1 = inputs.get(0);
        if (arg1 == null) {
            cvn.appendCode(errorComment(
                    "in BlockMathRelation genFcode(): first input signal was null."));
            return cvn;
        }
        Signal arg2 = inputs.get(1);
        if (arg2 == null) {
            cvn.appendCode(errorComment(
            "in BlockMathRelation genFcode(): second input signal was null."));
            return cvn;
        }
        cvn.append(arg1.genCode());
        int dialect = ourModel.getCodeDialect();
        switch(dialect) {
            case Model.DT_ANSI_C:
                switch (relation) {
                    case LT:  cvn.appendCode(" < " ); break;
                    case LEQ: cvn.appendCode(" <= "); break;
                    case EQ:  cvn.appendCode(" == "); break;
                    case GEQ: cvn.appendCode(" >= "); break;
                    case GT:  cvn.appendCode(" > " ); break;
                    case NEQ: cvn.appendCode(" != "); break;
                }
                break;
            case Model.DT_FORTRAN:
                switch (relation) {
                    case LT:  cvn.appendCode(" .LT. "); break;
                    case LEQ: cvn.appendCode(" .LE. "); break;
                    case EQ:  cvn.appendCode(" .EQ. "); break;
                    case GEQ: cvn.appendCode(" .GE. "); break;
                    case GT:  cvn.appendCode(" .GT. "); break;
                    case NEQ: cvn.appendCode(" .NE. "); break;
                }
                break;
        }
        cvn.append(arg2.genCode());
        
        // if not derived, need new line
        if (!outputSig.isDerived()) {
            cvn.appendCode(this.endLine());
        }
        return cvn;
    }


    /**
     *
     * <p> Generates description of self </p>
     *
     * @throws <code>IOException</code>
     **/

    @Override
    public void describeSelf(Writer writer) throws IOException
    {
        super.describeSelf(writer);
        writer.write(" and is a Relational Operator math block.");
    }

    /**
     * Sets relationship operator from String
     * @throws DAVEException 
     */
    
    public void setRelationOp( String relationString ) throws DAVEException {
    	this.relationOp = relationString.toLowerCase();
    	if (!this.validateRelation()) {
    		throw new DAVEException("Unrecognized relation string: " + relationString );
    	}
    }

    /**
     *
     * Returns relationship operator as String
     *
     **/

    public String getRelationOp() { return this.relationOp; }


    /**
     *
     * <p> Implements update() method </p>
     * @throws DAVEException
     *
     **/

    @Override
    public void update() throws DAVEException
    {
        int numInputs;
        Iterator<Signal> theInputs;
        Signal theInput;
        double[] theInputValue;
        int index = 0;
        int requiredInputs = 2;

        boolean verbose = this.isVerbose();

        if (verbose) {
            System.out.println();
            System.out.println("Entering update method for function '" + this.getName() + "'");
        }

        // sanity check to see if we have exact number of inputs
        numInputs = this.inputs.size();
        if (numInputs != requiredInputs) {
            throw new DAVEException("Number of inputs to '" + this.getName() +
                    "' wrong - should be " + requiredInputs + ".");
        }

        // allocate memory for the input values
        theInputValue = new double[requiredInputs];
        
        // see if each input variable is ready
        theInputs = this.inputs.iterator();

        while (theInputs.hasNext()) {
            theInput = theInputs.next();
            if (!theInput.sourceReady()) {
                if (verbose) {
                    System.out.println(" Upstream signal '" + 
                            theInput.getName() + "' is not ready.");
                }
                return;
            } else {
                theInputValue[index] = theInput.sourceValue();
            }
            index++;
        }

        // Calculate our output
        this.value = Double.NaN;
        switch (this.relation) {
        case LT:
            this.value = (theInputValue[0] <  theInputValue[1] ? 1.0 : 0.0); 
            break;
        case LEQ:
            this.value = (theInputValue[0] <= theInputValue[1] ? 1.0 : 0.0); 
            break;
        case EQ:
            this.value = (theInputValue[0] == theInputValue[1] ? 1.0 : 0.0);
            break;
        case GEQ:
            this.value = (theInputValue[0] >= theInputValue[1] ? 1.0 : 0.0);
            break;
        case GT:
            this.value = (theInputValue[0] >  theInputValue[1] ? 1.0 : 0.0);
            break;
        case NEQ:
            this.value = (theInputValue[0] != theInputValue[1] ? 1.0 : 0.0);
            break;
        }
        if (this.value == Double.NaN) {
            throw new DAVEException("Unrecognized operator " + this.relationOp + " in block " + this.getName());
        }
        // record current cycle counter
        resultsCycleCount = ourModel.getCycleCounter();
    }
}
