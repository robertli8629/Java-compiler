 
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
	public Symbol  (String name, String kind, short start_line, SymbolType type,
                    int lexic_level, int order_number, int numParams){
	    this.name = name;
	    this.kind = kind;
	    this.start_line = start_line;
	    this.type = type;
	    this.lexic_level = lexic_level;
	    this.order_number = order_number;
        this.numParams = numParams;
	}
	
	@Override
	public String toString() {
		return   "name: " + name + ", kind : " + kind + ", start_line : " +
                  start_line + ", type: " + type.toString() +
                  ", lexic_level: " + lexic_level + ", order_number: " +
                  order_number + ", numParams: " + numParams;
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

    public short getStartLine() {
        return this.start_line;
    }

    public int getNumParams() {
        return this.numParams;
    }

	private String name;
	private String kind;
	private short start_line;
	private SymbolType type;
	private int lexic_level;
	private int order_number;
    private int numParams;
	
}
