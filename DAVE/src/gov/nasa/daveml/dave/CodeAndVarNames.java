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

import java.util.ArrayList;

/**
 * Convenience class that allows storage of code and variable names
 * in a single object
 * @author Bruce Jackson, Digital Flight Dynamics
 * <a href="mailto:bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>
 * @version 0.9
 */
public class CodeAndVarNames {
    
    /** emitted source code lines, separated by newlines */
    private String code;
    
    /** array of variables names */
    private ArrayList<String> varName;

    /**
     *
     * Simple default constructor
     *
     */
    public CodeAndVarNames() {
        code = "";
        varName = new ArrayList(5);
    } 

    /**
     *
     * Constructor that accepts a line of code (comment?)
     * @param theCode Initial code components as a String
     *
     */
    public CodeAndVarNames( String theCode ) {
        code = theCode;
        varName = new ArrayList(0);
    }

    /**
     *
     * Copy constructor
     * @param arg a CodeAndVarNames object to copy
     *
     */
    public void append( CodeAndVarNames arg ) {
        CodeAndVarNames cvn = new CodeAndVarNames();
        this.code += arg.code;
        this.varName.addAll(arg.varName);
    }

    /**
     *
     * Appends text to the existing code
     * @param string Code snippet to add
     *
     */
    public void appendCode(String string) {
        this.code += string;
    }

    /**
     *
     * Prepends text to the existing code
     * @param string the Code snipped to prepend
     *
     */
    public void prependCode(String string) {
        this.code = string + this.code;
    }

    /**
     *
     * Adds another argument (variable name) to the list
     * @param varName the variable name to append to the list
     *
     */
    public void addVarName( String varName ) {
        this.varName.add(varName);
    }

    /**
     *
     * Returns the {@link ArrayList} of variable names
     * @return the <code>ArrayList</code> of variable naems
     *
     */
    public ArrayList<String> getVarNames() {
        return varName;
    }

    /**
     *
     * Returns the variable name found at offset (0-based) 
     * @param i offset (0-based) into list of variable names
     * @return a <code>String</code> holding the variable name
     *
     */
    public String getVarName(int i) {
        return varName.get(i);
    }

    /**
     *
     * Returns the code snippet
     * @return the code snippet
     *
     */
    public String getCode() {
        return code;
    }
}
