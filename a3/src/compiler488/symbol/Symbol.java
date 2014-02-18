 
package compiler488.symbol;

import java.io.*;

/** Symbol Table
 *  This almost empty class is a framework for implementing
 *  a Symbol Table class for the CSC488S compiler
 *  
 *  Each implementation can change/modify/delete this class
 *  as they see fit.
 *
 *  @author  <B> PUT YOUR NAMES HERE </B>
 */

public class Symbol {
	
	/** Symbol  constructor
         *  Create and initialize a symbol table 
	 */
	public Symbol  (String name, String kind, int value, SymbolType type){
	    this.name=name;
	    this.kind=kind;
	    this.value=value;
	    this.type=type;
	}
	
	@Override
	public String toString() {
		return   "name: " + name + ", kind : " + kind + ", value : " + Integer.toString(value) + ", type: " + type.toString();
	}

	/** The rest of Symbol Table
	 *  Data structures, public and private functions
 	 *  to implement the Symbol Table
	 *  GO HERE.				
	 */
	private String name;
	private String kind;
	private int value;
	private SymbolType type;
}
