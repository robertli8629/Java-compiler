 
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
	
	/** SymbolType  constructor
         *  Create and initialize a SymbolType 
	 */
	public SymbolType  (String type, Object link){
	  this.type=type;
	  this.link=link;
	
	}
	
	public String getType() {
	    return this.type;
	}
	
	public Object getLink() {
	    return this.link;
	}
	
	public void setLink(Object link) {
	    this.link = link;
	}
	
	@Override
	public String toString() {
		String link_str = "null";
		if (link != null) {
		    link_str = link.toString();
		}
		return   "type: " + type + ", link : " + link_str ;
	}

	private String type;
	private Object link;

}
 
