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
	    this.traverse((Scope) p, null, ScopeType.MAJOR);
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
	
	private enum ScopeType {
	    MAJOR, FUNCTION, PROCEDURE, LOOP, MINOR
	}
	
	// second parameter is only used when entering a function scope with parameters
	private void traverse(Scope s, ASTList<ScalarDecl> arg, ScopeType scope_type){
// 	    System.out.println("enter traverse");
	    Hashtable<String,Symbol> symboltable=new Hashtable<String,Symbol>();

	    ASTList<Declaration> AST_dcl=s.getDeclarations();
	    LinkedList<Declaration> ll=AST_dcl.get_list();
	    
	    if (arg != null) {
		add_params(symboltable, arg);
	    }
	    
	    if (ll != null){
		ListIterator iterator = ll.listIterator();
		while (iterator.hasNext()){
			Declaration decl = (Declaration)iterator.next();
			
			handle_declaration(decl, symboltable, scope_type);
		}
	    }
	    printHash(symboltable);
	    symbolstack.push(symboltable);

	    // recursion
	    ASTList<Stmt> AST_stat=s.getStatements();
	    LinkedList<Stmt> stmt_ll=AST_stat.get_list();
	    if (stmt_ll != null){
		ListIterator iterator_stmt = stmt_ll.listIterator();
		while (iterator_stmt.hasNext()){
			Stmt stmt = (Stmt)iterator_stmt.next();
			
			handle_statement(stmt, scope_type);
		}
	    }
	    
	    symbolstack.pop();
// 	    System.out.println("exit traverse");
	}
	private void recursive_stmt(ASTList<Stmt> AST_stat,ScopeType scope_type){
	    LinkedList<Stmt> stmt_ll=AST_stat.get_list();
	    if (stmt_ll != null){
		ListIterator iterator_stmt = stmt_ll.listIterator();
		while (iterator_stmt.hasNext()){
			Stmt stmt = (Stmt)iterator_stmt.next();
			
			handle_statement(stmt, scope_type);
		}
	    }
	}
	
	private void handle_declaration(Declaration decl, Hashtable<String,Symbol> symboltable, ScopeType scope_type) {
	
	    // Semantic analysis S10: check whether variable is declared in currect scope
	    if (decl instanceof ScalarDecl) {
		check_if_declared(symboltable, decl.getName());
	    }
	    
	    if (decl instanceof MultiDeclarations) {
		ASTList<DeclarationPart> decl_list = ((MultiDeclarations)decl).getElements();
		LinkedList<DeclarationPart> ll_part=decl_list.get_list();
		
		for (DeclarationPart dp : ll_part) {
// 		    add_to_symboltable(dp, symboltable, decl.getType());
		    handle_part_declaration(dp, symboltable, scope_type, decl.getType());
		}
		return;
	    }
	    
	    // Semantic analysis S54: associate params if any with scope
	    if (decl instanceof RoutineDecl) {
		RoutineBody rb = ((RoutineDecl)decl).getRoutineBody();
		Scope routine_scope = rb.getBody();
		ASTList<ScalarDecl> params = rb.getParameters();
		if (decl.getType() == null) {
		    traverse(routine_scope, params, ScopeType.PROCEDURE);
		} else {
		    traverse(routine_scope, params, ScopeType.FUNCTION);
		}
		
	    }
	    
	    // add it to symbol table
	    add_to_symboltable(decl, symboltable);
	
	    return;
	}
	
	private void handle_part_declaration(DeclarationPart dp, Hashtable<String,Symbol> symboltable, ScopeType scope_type, Type type) {
	    
	    if (dp instanceof ScalarDeclPart) {
		check_if_declared(symboltable, dp.getName());
	    }
	    
	    if (dp instanceof ArrayDeclPart) {
		check_if_declared(symboltable, dp.getName());
		
		// Semantic analysis S46: check that lower bound is <= upper bound
		ArrayDeclPart adp = (ArrayDeclPart)dp;
		if (adp.isTwoDimensional()) { // 2 dim
		    if ((adp.getLowerBoundary1() > adp.getUpperBoundary1()) || (adp.getLowerBoundary2() > adp.getUpperBoundary2())) {
			System.out.println("array error: lower boundary greater than upper boundary");
		    }
		} else { // 1 dim
		    if (adp.getLowerBoundary1() > adp.getUpperBoundary1()) {
			System.out.println("array error: lower boundary greater than upper boundary");
		    }
		}
	    }
	    
	    add_to_symboltable(dp, symboltable, type);
	}
	
	private void handle_statement(Stmt stmt, ScopeType scope_type) {
	
	    if(stmt instanceof Scope){
		Scope scope = (Scope) stmt; // find the scope from stmtlist\
		traverse(scope, null, ScopeType.MINOR);
		    
	    }
	    
	    if (stmt instanceof ExitStmt) { // Semantic analysis S50: check that exit statement is in a loop
		if (scope_type != ScopeType.LOOP) {
		    System.out.println("exit statement not in a loop");
		}
		if(!(expn_analysis(((ExitStmt)stmt).getExpn()).equals("boolean"))){
			System.out.println("Boolean type required");
		}
	    }
	    
	    if (stmt instanceof ResultStmt) { // Semantic analysis S51: check that result statement is in a function
		if (scope_type != ScopeType.FUNCTION) {
		    System.out.println("result statement not in a function");
		}
		if(!(expn_analysis(((ResultStmt)stmt).getValue()).equals("boolean"))){
			System.out.println("Boolean type required");
		}
	    }
	    
	    if (stmt instanceof ReturnStmt) { // Semantic analysis S52: check that return statement is in a procedure
		if (scope_type != ScopeType.PROCEDURE) {
		    System.out.println("return statement not in a procedure");
		}
	    }
	    
	    if (stmt instanceof LoopingStmt) { // looping statement (while/repeat)
		if(!(expn_analysis(((LoopingStmt)stmt).getExpn()).equals("boolean"))){
			System.out.println("Boolean type required");
		}
		ASTList<Stmt> whileStmts = ((LoopingStmt)stmt).getBody();
		LinkedList<Stmt> whilestmt_ll=whileStmts.get_list();
		for (Stmt whileStmt : whilestmt_ll) {
		    if(whileStmt instanceof Scope){
			Scope scope = (Scope) whileStmt; // find the scope from stmtlist
			traverse(scope, null, ScopeType.LOOP);
			    
		    } else {
			handle_statement(whileStmt, ScopeType.LOOP);
		    }
		}
	    }
	    
	    if(stmt instanceof AssignStmt){
		    //System.out.println("AssignStmt!!!!");
		    AssignStmt asgn_stmt=(AssignStmt) stmt;
		    Expn expn=asgn_stmt.getRval();
		    IdentExpn var=(IdentExpn) asgn_stmt.getLval();
		    if(!(variable_analysis(var).equals(expn_analysis(expn)))){
			    System.out.println("Type error");
		    }
	    }
	    if(stmt instanceof IfStmt){
		IfStmt if_stmt=(IfStmt) stmt;
		if(!(expn_analysis(if_stmt.getCondition()).equals("boolean"))){
			System.out.println("Boolean type required");
		}
		recursive_stmt(if_stmt.getWhenTrue(),scope_type);
		recursive_stmt(if_stmt.getWhenFalse(),scope_type);
	    }
	    if(stmt instanceof ProcedureCallStmt){
		
	    }
	    return;
	}
	
	
	private void add_to_symboltable(Declaration decl, Hashtable<String,Symbol> symboltable) {
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
	
	
	private void add_to_symboltable(DeclarationPart dp, Hashtable<String,Symbol> symboltable, Type type) {
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
	
	
	private void printHash(Hashtable<String,Symbol> ht) {
// 	    System.out.println("printHash");
	    Set<String> keyset = ht.keySet();
	    for (String s : keyset) {
		System.out.println(s+" :");
		Symbol sym = ht.get(s);
		System.out.println(sym.toString());
	    }
// 	    System.out.println("end printHash");
	}
	
	
	// check whether name is declared in ht
	private void check_if_declared(Hashtable<String,Symbol> ht, String name) {
// 	    Hashtable<String,Symbol> ht = this.symbolstack.peek();
	    Set<String> keyset = ht.keySet();
	    if (keyset.contains(name)) {
		System.out.println("name \"" + name + "\" is already defined in the scope");
	    }
	}
	
	// Semantic analysis S54: associate params if any with scope
	private void add_params(Hashtable<String,Symbol> ht, ASTList<ScalarDecl> params) {
	    System.out.println("add_params");
	    if (params != null) {
		LinkedList<ScalarDecl> l = params.get_list();
		for (ScalarDecl d : l) {
    // 		System.out.println(d);
		    add_to_symboltable(d, ht);
		}
	    }
	}
	
	
	
	
	private String expn_analysis(Expn expn){
            if(expn instanceof IntConstExpn){
                return "integer";
            }
            if(expn instanceof UnaryExpn){
                if(!(expn_analysis(expn).equals("integer"))){
                        System.out.println("bad operand type for unary -");
                }
                return "integer";
            }
            if(expn instanceof ArithExpn){
                ArithExpn arith_expn=(ArithExpn) expn;
                if(!(expn_analysis(arith_expn.getLeft()).equals("integer"))){
                        System.out.println("unsupported operand type(s) for "+arith_expn.getOpSymbol());
                }
                if(!(expn_analysis(arith_expn.getRight()).equals("integer"))){
                        System.out.println("unsupported operand type(s) for "+arith_expn.getOpSymbol());
                }
                return "integer";
            }
            if(expn instanceof BoolConstExpn){
                return "boolean";
            }
            if(expn instanceof NotExpn){
                if(!(expn_analysis(expn).equals("boolean"))){
                        System.out.println("bad operand type for not");
                }
                return "boolean";
            }
            if(expn instanceof BoolExpn){
                BoolExpn bool_expn=(BoolExpn) expn;
                if(!(expn_analysis(bool_expn.getLeft()).equals("boolean"))){
                        System.out.println("unsupported operand type(s) for "+bool_expn.getOpSymbol());
                }
                if(!(expn_analysis(bool_expn.getRight()).equals("boolean"))){
                        System.out.println("unsupported operand type(s) for "+bool_expn.getOpSymbol());
                }
                return "boolean";
            }
            if(expn instanceof EqualsExpn){
                EqualsExpn euqals_expn=(EqualsExpn) expn;
         if(!(expn_analysis(euqals_expn.getLeft()).equals(expn_analysis(euqals_expn.getRight())))){
                        System.out.println("uncomparable types");
                }
                return "boolean";
            }
            if(expn instanceof CompareExpn){
                CompareExpn comp_expn=(CompareExpn) expn;
                if(!(expn_analysis(comp_expn.getLeft()).equals("integer"))){
                        System.out.println("unsupported operand type(s) for "+comp_expn.getOpSymbol());
                }
                if(!(expn_analysis(comp_expn.getRight()).equals("integer"))){
                        System.out.println("unsupported operand type(s) for "+comp_expn.getOpSymbol());
                }
                return "boolean";
            }
            if(expn instanceof ParenthExpn){
                ParenthExpn parenth_expn=(ParenthExpn) expn;
                return expn_analysis(parenth_expn.getParenth());
            }
            if(expn instanceof ConditionalExpn){
                ConditionalExpn cond_expn=(ConditionalExpn) expn;
                if(!(expn_analysis(cond_expn.getCondition()).equals("boolean"))){
                        System.out.println("unsupported operand type");
                }
         if(!(expn_analysis(cond_expn.getTrueValue()).equals(expn_analysis(cond_expn.getFalseValue())))){
                        System.out.println("unsupported operand type");
                }
                return expn_analysis(cond_expn.getCondition());
            }
            if(expn instanceof FunctionCallExpn){
                FunctionCallExpn func_expn=(FunctionCallExpn) expn;
               
            }
            if(expn instanceof IdentExpn){
                IdentExpn ident_expn=(IdentExpn) expn;
                return variable_analysis(ident_expn);
            }
            return "";
        }
       
       
       
       
        private String variable_analysis(IdentExpn ident_expn){
            Iterator<Hashtable<String,Symbol>> iter=symbolstack.iterator();
            int flag=0;
            Symbol symbol = null;
            while(iter.hasNext()){
                symbol=iter.next().get(ident_expn.toString());
                if (symbol != null) {
                        flag=1;
                        break;
                }
            }
            if(flag==0){
                System.out.println("name \""+ident_expn.toString()+"\" is not defined");
            }else if(symbol!= null){
                //System.out.println("ident_expn.toString():"+symbol.getType().getType());
                return symbol.getType().getType();
            }
            return "";
        }
	
	
	
	
	// check if the var is defined and if two vars have the same type
	private void assign_syntax(Program p){
          ASTList<Stmt> AST_stat=p.getStatements();
          LinkedList<Stmt> stmt_ll=AST_stat.get_list();
           ListIterator iterator_stmt = stmt_ll.listIterator();
           if (iterator_stmt.hasNext()){
                while (iterator_stmt.hasNext()){
                    Stmt stmt = (Stmt)iterator_stmt.next();
                    if(stmt instanceof AssignStmt){
                      AssignStmt asgn_stmt=(AssignStmt) stmt;
                      IdentExpn expn=(IdentExpn) asgn_stmt.getLval();
                      int flag=0;
                      Symbol symbol = null;
                      Iterator<Hashtable<String,Symbol>> iter=symbolstack.iterator();
                      while(iter.hasNext()){
                          symbol=iter.next().get(expn.getIdent());
                          if (symbol != null) {
                            flag=1;
                            break;
                          }
                      }
                      if(flag==0){
                          System.out.println("name \""+expn.getIdent()+"\" is not defined");
                      }else{
			  check_rval_syntax(asgn_stmt.getRval(),symbol);
                      }
                   }
                }
            }
        }
        
        private void check_rval_syntax(Expn expn,Symbol symbol){
          if(expn instanceof BinaryExpn){
	      BinaryExpn binExpn = (BinaryExpn)expn;
              if(binExpn.getLeft() instanceof IdentExpn){
                check_definedtype((IdentExpn)binExpn.getLeft(), symbol);
              }
              check_rval_syntax(binExpn.getRight(), symbol);
          }
          if(expn instanceof IdentExpn){
              check_definedtype((IdentExpn)expn, symbol);
          }
        }
        
        
        private void check_definedtype(IdentExpn expn,Symbol symbol){
            int flag=0;
            Iterator<Hashtable<String,Symbol>> iter=symbolstack.iterator();
            Symbol check_symbol = null;
	    while(iter.hasNext()){
		check_symbol=iter.next().get(expn.getIdent());
		if (check_symbol != null){
		  flag=1;
		  if(!(check_symbol.getType().getType().equals(symbol.getType().getType()))){
		    flag=2;
		  }
		  break;
		}
	    }
	    if(flag!=1){
		System.out.println("name \""+expn.getIdent()+"\" is not defined");
		if(flag==2){
		  System.out.println("unsupported operand type(s):" + check_symbol.getType().getType()+symbol.getType().getType());
		}
	    }
        }
	
	
	
	
	
}
