package compiler488.symbol;

import java.io.*;
import java.util.Stack;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
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
	    this.traverse((Scope) p);
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
// 	    System.out.println("1");
	    Hashtable<String,Symbol> symboltable=new Hashtable<String,Symbol>();
// 	    System.out.println("2");
	    ASTList<Declaration> AST_dcl=s.getDeclarations();
// 	    System.out.println(AST_dcl);
	    LinkedList<Declaration> ll=AST_dcl.get_list();
	    
// 	    System.out.println("3");
	    if (ll != null){
		ListIterator iterator = ll.listIterator();
		while (iterator.hasNext()){
			Declaration decl = (Declaration)iterator.next();
			SymbolType s_type=new SymbolType(decl.getType().toString(), "");  
			String kind = "unknown";
			if (decl instanceof ScalarDecl) {
			    kind = "var";
			} else if (decl instanceof ScalarDecl) {
			    kind = "func";
			}
			Symbol sym=new Symbol(decl.getName(), kind,0 , s_type); 
			symboltable.put(decl.getName(),sym);
		}
	    }
	    printHash(symboltable);
	    symbolstack.push(symboltable);
// 	    System.out.println("4");

	    // recursion
	    ASTList<Stmt> AST_stat=s.getStatements();
	    LinkedList<Stmt> stmt_ll=AST_stat.get_list();
	    if (stmt_ll != null){
		ListIterator iterator_stmt = stmt_ll.listIterator();
		while (iterator_stmt.hasNext()){
			Stmt stmt = (Stmt)iterator_stmt.next();
			if(stmt instanceof Scope){
				Scope scope = (Scope) stmt; // find the scope from stmtlist
				
				
				traverse(scope);
				
			}
		}
	    }
// 	    System.out.println("4.5");
	    symbolstack.pop();
// 	    System.out.println("5");
	}
	
	
	private void printHash(Hashtable<String,Symbol> ht) {
	    Set<String> keyset = ht.keySet();
	    for (String s : keyset) {
		System.out.println(s+" :");
		Symbol sym = ht.get(s);
		System.out.println(sym.toString());
	    }
	}
	
	
}
