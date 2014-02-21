package compiler488.semantics;

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

/** Implement semantic analysis for compiler 488 
 *  @author  <B> Put your names here </B>
 */
public class Semantics {
	
        /** flag for tracing semantic analysis */
	private boolean traceSemantics = false;
	/** file sink for semantic analysis trace */
	private String traceFile = new String();
	public FileWriter Tracer;
	public File f;
	private SymbolTable symbolTable;
     
     
     /** SemanticAnalyzer constructor */
	public Semantics (){
	
	}
	
	private enum ScopeType {
	    MAJOR, FUNCTION, PROCEDURE, LOOP, MINOR
	}

	/**  semanticsInitialize - called once by the parser at the      */
	/*                        start of  compilation                 */
	public void Initialize(Program programAST) {
	
	   /*   Initialize the symbol table             */
	
	   // Symbol.Initialize();
	   
	   /*********************************************/
	   /*  Additional initialization code for the   */
	   /*  semantic analysis module                 */
	   /*  GOES HERE                                */
	   /*********************************************/
	   
	   symbolTable = new SymbolTable();
// 	   symbolTable.Initialize(programAST);

	   this.traverse((Scope) programAST, null, ScopeType.MAJOR);
	   
	}

	/**  semanticsFinalize - called by the parser once at the        */
	/*                      end of compilation                      */
	void Finalize(){
	
	  /*  Finalize the symbol table                 */
	
	  // Symbol.Finalize();
	  
	   /*********************************************/
	  /*  Additional finalization code for the      */
	  /*  semantics analysis module                 */
	  /*  GOES here.                                */
	  /**********************************************/
	  
	}
	
	/**
	 *  Perform one semantic analysis action
         *  @param  actionNumber  semantic analysis action number
         */
	void semanticAction( int actionNumber ) {

	if( traceSemantics ){
		if(traceFile.length() > 0 ){
	 		//output trace to the file represented by traceFile
	 		try{
	 			//open the file for writing and append to it
	 			File f = new File(traceFile);
	 		    Tracer = new FileWriter(traceFile, true);
	 				          
	 		    Tracer.write("Sematics: S" + actionNumber + "\n");
	 		    //always be sure to close the file
	 		    Tracer.close();
	 		}
	 		catch (IOException e) {
	 		  System.out.println(traceFile + 
				" could be opened/created.  It may be in use.");
	 	  	}
	 	}
	 	else{
	 		//output the trace to standard out.
	 		System.out.println("Sematics: S" + actionNumber );
	 	}
	 
	}
	                     
	   /*************************************************************/
	   /*  Code to implement each semantic action GOES HERE         */
	   /*  This stub semantic analyzer just prints the actionNumber */   
	   /*                                                           */
           /*  FEEL FREE TO ignore or replace this procedure            */
	   /*************************************************************/
	                     
	   System.out.println("Semantic Action: S" + actionNumber  );
	   return ;
	}

	// ADDITIONAL FUNCTIONS TO IMPLEMENT SEMANTIC ANALYSIS GO HERE
	
	
	
	
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
// 	    printHash(symboltable);
	    symbolTable.symbolstack.push(symboltable);

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
	    
	    symbolTable.symbolstack.pop();
// 	    System.out.println("exit traverse");
	}
	
	private void recursive_stmt(ASTList<Stmt> AST_stat, ScopeType scope_type){
	    LinkedList<Stmt> stmt_ll = AST_stat.get_list();
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
	    symbolTable.add_to_symboltable(decl, symboltable);
	
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
	    
	    symbolTable.add_to_symboltable(dp, symboltable, type);
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
	    
	    // S34: check that variable and expression in assignment are the same type
	    if(stmt instanceof AssignStmt){
		    AssignStmt asgn_stmt=(AssignStmt) stmt;
		    Expn expn=asgn_stmt.getRval();
		    IdentExpn var=(IdentExpn) asgn_stmt.getLval();
		    String var_type = variable_analysis(var);
		    String expn_type = expn_analysis(expn);
		    if(!(var_type.equals(expn_type))){
			    System.out.println("Type error: expect variable " + var.toString() + " type: " + var_type + " but given type: " + expn_type);
		    }
	    }
	    
	    if(stmt instanceof IfStmt){
		IfStmt if_stmt=(IfStmt) stmt;
		if(!(expn_analysis(if_stmt.getCondition()).equals("boolean"))){
			System.out.println("Boolean type required");
		}
		recursive_stmt(if_stmt.getWhenTrue(), ScopeType.MINOR);
		recursive_stmt(if_stmt.getWhenFalse(), ScopeType.MINOR);
	    }
	    
	    if(stmt instanceof ProcedureCallStmt){
		
	    }
	    
	    return;
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
	
	// S54: associate params if any with scope
	private void add_params(Hashtable<String,Symbol> ht, ASTList<ScalarDecl> params) {
	    System.out.println("add_params");
	    if (params != null) {
		LinkedList<ScalarDecl> l = params.get_list();
		for (ScalarDecl d : l) {
    // 		System.out.println(d);
		    symbolTable.add_to_symboltable(d, ht);
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
       
       
       
        // S29: check whether the variable is declared or visible
        private String variable_analysis(IdentExpn ident_expn){
            Iterator<Hashtable<String,Symbol>> iter=symbolTable.symbolstack.iterator(); // iterator of the stack iterates from bottom to top
            Symbol symbol = null;
            Symbol symbol_found = null;
            while(iter.hasNext()){
                symbol=iter.next().get(ident_expn.toString());
                if (symbol != null) {
		    symbol_found = symbol;
                }
            }
//             System.out.println("top");
//             System.out.println(symbolTable.symbolstack.peek().get(ident_expn.toString()));
            
            if(symbol_found==null){
                System.out.println("variable \"" + ident_expn.toString() + "\" is not defined");
            }else if(symbol_found!= null){
//                 System.out.println("ident_expn.toString():"+symbol_found.getType().getType());
                return symbol_found.getType().getType();
            }
            return "";
        }
	
	
	

}
