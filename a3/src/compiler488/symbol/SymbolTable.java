package compiler488.symbol;

import java.io.*;
import java.util.Stack;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.Iterator;
import compiler488.symbol.*;
import compiler488.ast.ASTList;
import compiler488.ast.stmt.*;
import compiler488.ast.decl.*;
import compiler488.ast.expn.*;
import compiler488.ast.type.*;


/** Symbol Table
 *  This almost empty class is a framework for implementing
 *  a Symbol Table class for the CSC488S compiler
 *  
 *  Each implementation can change/modify/delete this class
 *  as they see fit.
 *
 *  @author  <B> PUT YOUR NAMES HERE </B>
 */

public class SymbolTable {
	
	/** Symbol Table  constructor
         *  Create and initialize a symbol table 
	 */
	public SymbolTable  (){
	    symbolstack = new Stack<Hashtable<String,Symbol>>();
	}

	/**  Initialize - called once by semantic analysis  
	 *                at the start of  compilation     
	 *                May be unnecessary if constructor
 	 *                does all required initialization	
	 */
	public void Initialize(Program p) {
	   
	   /**   Initialize the symbol table             
	    *	Any additional symbol table initialization
	    *  GOES HERE                                	
	    */
// 	    this.traverse((Scope) p, null, ScopeType.MAJOR);
	}

	/**  Finalize - called once by Semantics at the end of compilation
	 *              May be unnecessary 		
	 */
	public void Finalize(){
	
	  /**  Additional finalization code for the 
	   *  symbol table  class GOES HERE.
	   *  
	   */
	}
	

	/** The rest of Symbol Table
	 *  Data structures, public and private functions
 	 *  to implement the Symbol Table
	 *  GO HERE.				
	 */
	
	/** the main symbol table stack */
	public Stack<Hashtable<String,Symbol>> symbolstack;
	
	/** add declaration to the symbol table */
	public void add_to_symboltable(Declaration decl, Hashtable<String,Symbol> symboltable, int lexic_level, int order_number) {
	    Type tp = decl.getType();
	    SymbolType s_type = null;
	    if (tp != null) {
		s_type=new SymbolType(tp.toString(), null);  
	    } else {
		s_type=new SymbolType("null", null); 
	    }
	    String kind = "unknown";
	    if (decl instanceof ScalarDecl) {
		kind = "var";
	    } else if (decl instanceof RoutineDecl) {
		kind = "func";
		s_type.setLink((Object)decl);
	    }
	    Symbol sym=new Symbol(decl.getName(), kind,0 , s_type, lexic_level, order_number); 
	    symboltable.put(decl.getName(),sym);
	}
	
	/** add declaration part to the symbol table */
	public void add_to_symboltable(DeclarationPart dp, Hashtable<String,Symbol> symboltable, Type type, int lexic_level, int order_number) {
	    SymbolType s_type=new SymbolType(type.toString(), null);  
	    String kind = "unknown";
	    if (dp instanceof ScalarDeclPart) {
		kind = "var";
	    } else if (dp instanceof ArrayDeclPart) {
		kind = "array";
		s_type.setLink((Object)dp);
	    }
	    Symbol sym=new Symbol(dp.getName(), kind,0 , s_type, lexic_level, order_number); 
	    symboltable.put(dp.getName(),sym);
	}
	
	
	
	
}
