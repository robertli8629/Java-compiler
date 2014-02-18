package compiler488.symbol;

import java.io.*;
import java.util.Stack;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import compiler488.symbol.*;
import compiler488.ast.ASTList;
import compiler488.ast.stmt.*;
import compiler488.ast.decl.*;


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
	private Stack<Hashtable<String,Symbol>> symbolstack;
	
	
	private void traverse(Scope s){
	
	    Hashtable<String,Symbol> symboltable=new Hashtable();
				  
	    ASTList<Declaration> AST_dcl=s.getDeclarations();
	    LinkedList<Declaration> ll=AST_dcl.get_list();
	    ListIterator iterator = ll.listIterator();
	    
	    if (iterator.hasNext()){
		while (iterator.hasNext()){
			Declaration decl = (Declaration)iterator.next();
			SymbolType s_type=new SymbolType("decl.getType()", "");  // first var is decl.getType()
			Symbol sym=new Symbol(decl.getName(), "decl.kind",0 , s_type); // second var is decl.kind
			symboltable.put(decl.getName(),sym);
		}
	    }
	    symbolstack.push(symboltable);
	  
	    // recursion
	    ASTList<Stmt> AST_stat=s.getStatements();
	    LinkedList<Stmt> stmt_ll=AST_stat.get_list();
	    ListIterator iterator_stmt = stmt_ll.listIterator();
	    if (iterator_stmt.hasNext()){
		while (iterator_stmt.hasNext()){
			Stmt stmt = (Stmt)iterator_stmt.next();
			if(stmt instanceof Scope){
				Scope scope = (Scope) stmt; // find the scope from stmtlist
				
				
				traverse(scope);
				
			}
		}
	    }
	    
	    symbolstack.pop();
	   
	}
}
