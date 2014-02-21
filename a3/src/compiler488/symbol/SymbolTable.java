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
	public Stack<Hashtable<String,Symbol>> symbolstack;
	
	private enum ScopeType {
	    MAJOR, FUNCTION, PROCEDURE, LOOP, MINOR
	}
	
	
	
	public void add_to_symboltable(Declaration decl, Hashtable<String,Symbol> symboltable) {
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
	    }
	    Symbol sym=new Symbol(decl.getName(), kind,0 , s_type); 
	    symboltable.put(decl.getName(),sym);
	}
	
	
	public void add_to_symboltable(DeclarationPart dp, Hashtable<String,Symbol> symboltable, Type type) {
	    SymbolType s_type=new SymbolType(type.toString(), null);  
	    String kind = "unknown";
	    if (dp instanceof ScalarDeclPart) {
		kind = "var";
	    } else if (dp instanceof ArrayDeclPart) {
		kind = "array";
		s_type.setLink((Object)dp);
	    }
	    Symbol sym=new Symbol(dp.getName(), kind,0 , s_type); 
	    symboltable.put(dp.getName(),sym);
	}
	
	

	
	
	
	/*
	    unused functions: just keep for reference
	*/
	
	// check if the var is defined and if two vars have the same type
// 	private void assign_syntax(Program p){
//           ASTList<Stmt> AST_stat=p.getStatements();
//           LinkedList<Stmt> stmt_ll=AST_stat.get_list();
//            ListIterator iterator_stmt = stmt_ll.listIterator();
//            if (iterator_stmt.hasNext()){
//                 while (iterator_stmt.hasNext()){
//                     Stmt stmt = (Stmt)iterator_stmt.next();
//                     if(stmt instanceof AssignStmt){
//                       AssignStmt asgn_stmt=(AssignStmt) stmt;
//                       IdentExpn expn=(IdentExpn) asgn_stmt.getLval();
//                       int flag=0;
//                       Symbol symbol = null;
//                       Iterator<Hashtable<String,Symbol>> iter=symbolstack.iterator();
//                       while(iter.hasNext()){
//                           symbol=iter.next().get(expn.getIdent());
//                           if (symbol != null) {
//                             flag=1;
//                             break;
//                           }
//                       }
//                       if(flag==0){
//                           System.out.println("name \""+expn.getIdent()+"\" is not defined");
//                       }else{
// 			  check_rval_syntax(asgn_stmt.getRval(),symbol);
//                       }
//                    }
//                 }
//             }
//         }
//         
//         private void check_rval_syntax(Expn expn,Symbol symbol){
//           if(expn instanceof BinaryExpn){
// 	      BinaryExpn binExpn = (BinaryExpn)expn;
//               if(binExpn.getLeft() instanceof IdentExpn){
//                 check_definedtype((IdentExpn)binExpn.getLeft(), symbol);
//               }
//               check_rval_syntax(binExpn.getRight(), symbol);
//           }
//           if(expn instanceof IdentExpn){
//               check_definedtype((IdentExpn)expn, symbol);
//           }
//         }
//         
//         
//         private void check_definedtype(IdentExpn expn,Symbol symbol){
//             int flag=0;
//             Iterator<Hashtable<String,Symbol>> iter=symbolstack.iterator();
//             Symbol check_symbol = null;
// 	    while(iter.hasNext()){
// 		check_symbol=iter.next().get(expn.getIdent());
// 		if (check_symbol != null){
// 		  flag=1;
// 		  if(!(check_symbol.getType().getType().equals(symbol.getType().getType()))){
// 		    flag=2;
// 		  }
// 		  break;
// 		}
// 	    }
// 	    if(flag!=1){
// 		System.out.println("name \""+expn.getIdent()+"\" is not defined");
// 		if(flag==2){
// 		  System.out.println("unsupported operand type(s):" + check_symbol.getType().getType()+symbol.getType().getType());
// 		}
// 	    }
//         }
	
	
	
	
	
}
