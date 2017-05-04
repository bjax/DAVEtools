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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.daveml.dave;

import java.util.ArrayList;

/**
 * Convenience class that allows storage of code and variable names
 * in a single object
 * @author ebjackso
 */
public class CodeAndVarNames {
    
    /** emitted source code lines, separated by newlines */
    private String code;
    
    /** array of variables names */
    private ArrayList<String> varName;
    
    public CodeAndVarNames() {
        code = "";
        varName = new ArrayList(5);
    } 
    
    public CodeAndVarNames( String theCode ) {
        code = theCode;
        varName = new ArrayList(0);
    }
    
    public void append( CodeAndVarNames arg ) {
        CodeAndVarNames cvn = new CodeAndVarNames();
        this.code += arg.code;
        this.varName.addAll(arg.varName);
    }

    public void appendCode(String string) {
        this.code += string;
    }
    
    public void prependCode(String string) {
        this.code = string + this.code;
    }
    
    public void addVarName( String varName ) {
        this.varName.add(varName);
    }
    
    public ArrayList<String> getVarNames() {
        return varName;
    }
    
    public String getVarName(int i) {
        return varName.get(i);
    }
    
    public String getCode() {
        return code;
    }
}
