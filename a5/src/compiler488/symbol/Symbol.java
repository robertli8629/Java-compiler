 
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
         *  Create and initialize a symbol 
	 */
	public Symbol  (String name, String kind, int value, SymbolType type, int lexic_level, int order_number){
	    this.name = name;
	    this.kind = kind;
	    this.value = value;
	    this.type = type;
	    this.lexic_level = lexic_level;
	    this.order_number = order_number;
	}
	
	@Override
	public String toString() {
		return   "name: " + name + ", kind : " + kind + ", value : " + Integer.toString(value) + ", type: " + type.toString() + ", lexic_level: " + lexic_level + ", order_number: " + order_number;
	}
	
	public SymbolType getType() {
	    return this.type;
	}
	
	public String getKind() {
	    return this.kind;
	}
	public short getll(){
	    return (short)this.lexic_level;
	}
	public short geton(){
	    return (short)this.order_number;
	}
	private String name;
	private String kind;
	private int value;
	private SymbolType type;
	private int lexic_level;
	private int order_number;
	
}
