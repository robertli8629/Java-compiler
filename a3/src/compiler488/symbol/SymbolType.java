 
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

public class SymbolType {
	
	/** Symbol  constructor
         *  Create and initialize a symbol table 
	 */
	public SymbolType  (String type, String link){
	  this.type=type;
	  this.link=link;
	
	}
	
	public String getType() {
	    return this.type;
	}
	
	@Override
	public String toString() {
		return   "type: " + type + ", link : " + link ;
	}

	/** The rest of Symbol Table
	 *  Data structures, public and private functions
 	 *  to implement the Symbol Table
	 *  GO HERE.				
	 */
	private String type;
	private String link;

}
 
