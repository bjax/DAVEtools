// NameList
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

//
// 020514 EBJ
//

package gov.nasa.daveml.dave;

import java.util.ArrayList;

/**
 *
 * Creates and manages unique object names, such as guaranteeing
 * unique block names for Simulink MDL file. Not to be confused with
 * XML and JDOM Namespace object which relate to unique names of XML
 * elements; this object ensures block and signal names are unique
 * within a model.
 * <p>
 * <code>NameList</code> represents a list of strings in use. It provides
 * methods to add, delete, and create unique names based on an initial
 * namespace.
 * <p>
 * 2010-05-04 Renamed from NameSpace to NameList to avoid confusion with JDOM Namespace
 * @author Bruce Jackson, Digital Flight Dynamics
 * <a href="mailto:bruce@digiflightdyn.com">bruce@digiflightdyn.com</a>
 * @version 0.9
 *
 */
@SuppressWarnings("serial")
public class NameList extends ArrayList<String>
{
    public NameList()  {super();}
    public NameList(int initialCapacity) { super(initialCapacity); }

    /**
     *
     * Changes name to meet namespace requirements, but does not ensure uniqueness
     * <p>
     * Currently is a placeholder for more sophisticated logic
     * @param s <code>String</code> with proposed name
     * @return String
     *
     **/
    public String  fixName( String s) { return s; }	// returns acceptable filtered version of name


    /**
     *
     * Returns <code>boolean</code> which indicates if supplied name is unique to namespace
     * @param s <code>String</code> with candidate name
     * @return boolean indicating if <b>s</b> is unique or not
     *
     **/
    public boolean isUnique(String s) { return (this.indexOf(s) == -1); }


    /**
     *
     * Changes name to meet namespace requirements, and appends
     * integer until unique name is created. Returns acceptable,
     * unique name
     *
     * @param s <code>String</code> with candidate name
     * @return String containing unique-ified <b>s</b> name
     *
     **/
    public String addUnique(String s)
    {
	String name = this.fixName(s);	// perform any unique filtering
	if (this.isUnique(s)) {
	    super.add(s);
        } else {
//System.out.print("-->Non-unique name " + s + " found; changed to ");
		int suffix = 1;
		while( !this.isUnique(s + suffix) ) { 
                    suffix++;
                }
		name = s + suffix;
		super.add(name);
//System.out.println(name);
	}
	return name;
    }
}
