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
import compiler488.ast.Readable;
import compiler488.ast.Printable;
import compiler488.ast.AST;


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
	/** indicate whether any semantic error occurs */
	public boolean error_flag = false;
	
	private boolean showSymbolTable = false;
	PrintStream ps;
	
	// array to store current order_number in each lexic_level, support at most 10 LL
// 	int[] current_order_number_ll = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     
     /** SemanticAnalyzer constructor */
	public Semantics (){
	
	}
	
	/** represent the type of the scope we are currently traversing */
	private enum ScopeType {
	    MAJOR, FUNCTION, PROCEDURE, LOOP, MINOR, LOOP_IN_FUNCTION, LOOP_IN_PROCEDURE
	}

	/**  semanticsInitialize - called once by the parser at the      */
	/*                        start of  compilation                 */
	public void Initialize(Program programAST, boolean showSymbolTable, SymbolTable st) {
	
	   /*   Initialize the symbol table             */
	
	   // Symbol.Initialize();
	   
	   /*********************************************/
	   /*  Additional initialization code for the   */
	   /*  semantic analysis module                 */
	   /*  GOES HERE                                */
	   /*********************************************/

	   ByteArrayOutputStream baos = new ByteArrayOutputStream();
	   ps = new PrintStream(baos);
	   
	   this.showSymbolTable = showSymbolTable;
	   
	   symbolTable = st;

	   this.traverse((Scope) programAST, null, ScopeType.MAJOR, null, 0, 0);

	   if (showSymbolTable) {
		   System.out.println("printing out the symbol table: ");
		   String content = baos.toString();
		   System.out.println(content);
	   }
	   
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
	
	
	
	/** 
	 * main semantic checking routine
	 * arg: if not null, it represents parameter list passed by function/procedure declaration scope
	 * ref: if not null, it represents function declaration, which is used to check the return type of the function
	 *  */
	private void traverse(Scope s, ASTList<ScalarDecl> arg, ScopeType scope_type, Object ref, int lexic_level, int order_number){
// 	    System.out.println("enter traverse");
	    Hashtable<String,Symbol> symboltable=new Hashtable<String,Symbol>();

	    ASTList<Declaration> AST_dcl=s.getDeclarations();
	    LinkedList<Declaration> ll=AST_dcl.get_list();
	    
	    symbolTable.symbolstack.push(symboltable);
	    
	    if (arg != null) {
		order_number = add_params(symboltable, arg, lexic_level, order_number);
	    }
	    
	    if (ll != null){
		ListIterator iterator = ll.listIterator();
		while (iterator.hasNext()){
			Declaration decl = (Declaration)iterator.next();
			
			order_number = handle_declaration(decl, symboltable, scope_type, lexic_level, order_number);
		}
	    }
	    
	    // if there is any left over forward declaration: throw an error
	    check_left_over_forward_decl(symboltable);
	    
	    if (showSymbolTable) { 
	    	printHash(symboltable);
	    }
	    
	    // recursion
	    ASTList<Stmt> AST_stat=s.getStatements();
	    LinkedList<Stmt> stmt_ll=AST_stat.get_list();
	    if (stmt_ll != null){
		ListIterator iterator_stmt = stmt_ll.listIterator();
		while (iterator_stmt.hasNext()){
			Stmt stmt = (Stmt)iterator_stmt.next();
			
			handle_statement(stmt, scope_type, ref, lexic_level);
		}
	    }
	    
	    symbolTable.symbolstack.pop();
// 	    System.out.println("exit traverse");
	}
	
	/** check any leftover forward declarations */
	private void check_left_over_forward_decl(Hashtable<String,Symbol> symboltable) {
	    
	    Set<String> keyset = symboltable.keySet();
	    for (String key : keyset) {
		Symbol sym = symboltable.get(key);
		if (sym.getType().getLink() instanceof RoutineDecl){ // found routine decl
		    RoutineDecl routine = (RoutineDecl)sym.getType().getLink();
		    RoutineBody rb = routine.getRoutineBody();
		    if (rb.getBody() == null) { // found a forward decl
			print(routine, routine + " is not resolved in a real declaration");
		    }
		}
	    }
	    
	    return;
	}
	
	/** recursively handle statements */
	private void recursive_stmt(ASTList<Stmt> AST_stat, ScopeType scope_type, Object ref, int lexic_level){
	    LinkedList<Stmt> stmt_ll = AST_stat.get_list();
	    if (stmt_ll != null){
		ListIterator iterator_stmt = stmt_ll.listIterator();
		while (iterator_stmt.hasNext()){
			Stmt stmt = (Stmt)iterator_stmt.next();
			
			handle_statement(stmt, scope_type, ref, lexic_level);
		}
	    }
	}
	
	/** handles declaration semantic checking */
	private int handle_declaration(Declaration decl, Hashtable<String,Symbol> symboltable, ScopeType scope_type, int lexic_level, int order_number) {
	
	    // Semantic analysis S10: check whether variable is declared in currect scope
	    if (decl instanceof ScalarDecl) {
		check_if_declared(symboltable, decl);
	    }
	    
	    if (decl instanceof MultiDeclarations) {
		ASTList<DeclarationPart> decl_list = ((MultiDeclarations)decl).getElements();
		LinkedList<DeclarationPart> ll_part=decl_list.get_list();
		
		for (DeclarationPart dp : ll_part) {
		    order_number = handle_part_declaration(dp, symboltable, scope_type, decl.getType(), lexic_level, order_number);
		}
		return order_number;
	    }
	    
	    // Semantic analysis S54: associate params if any with scope
	    if (decl instanceof RoutineDecl) {
		check_if_declared(symboltable, decl);
		RoutineBody rb = ((RoutineDecl)decl).getRoutineBody();
		Scope routine_scope = rb.getBody();
		if (routine_scope != null) { // not a forward decl
		    int result = check_forward_decl((RoutineDecl)decl); // S49: if function/procedure declared forward: verify declaration match
		    if (result < 0) { // forward declaration does not match
			return order_number + 1;
		    }
		    symbolTable.add_to_symboltable(decl, symboltable, lexic_level, symbolTable.current_order_number_ll[lexic_level]); // add first for recursive definition
		    symbolTable.current_order_number_ll[lexic_level]++;
		    ASTList<ScalarDecl> params = rb.getParameters();
		    if (decl.getType() == null) {
			traverse(routine_scope, params, ScopeType.PROCEDURE, null, lexic_level + 1, 0);
		    } else {
			traverse(routine_scope, params, ScopeType.FUNCTION, decl, lexic_level + 1, 0);
		    }
		    return order_number + 1;
		} else { // forward declaration
		    int result = check_forward_decl((RoutineDecl)decl);
		    if (result < 0) { // forward declaration does not match
			return order_number + 1;
		    }    
		    symbolTable.add_to_symboltable(decl, symboltable, lexic_level, symbolTable.current_order_number_ll[lexic_level]);
		    symbolTable.current_order_number_ll[lexic_level]++;
		    return order_number + 1;
		}
	    }
	    
	    // add it to symbol table
	    symbolTable.add_to_symboltable(decl, symboltable, lexic_level, symbolTable.current_order_number_ll[lexic_level]);
	    symbolTable.current_order_number_ll[lexic_level]++;
	
	    return order_number + 1;
	}
	
	/** checks forward declaration
	 * return -1 if encounters an error
	*/
	private int check_forward_decl(RoutineDecl decl) { // 
// 	    System.out.println("check_forward_decl");
	
	    ASTList<ScalarDecl> arg_list = decl.getRoutineBody().getParameters();
	    LinkedList<ScalarDecl> arg_ll = null;
	    int size_used = 0;
	    if (arg_list != null) {
		arg_ll = arg_list.get_list();
	        size_used = arg_ll.size();
	    }
	    
	    String name = decl.getName();
	    // look for forward declaration only in the current scope
	    Hashtable<String,Symbol> ht = symbolTable.symbolstack.peek();
	    Symbol symbol_found = ht.get(name);
	    
	    if (symbol_found != null) { // found
		if (symbol_found.getType().getLink() instanceof RoutineDecl){ // found routine decl
		    RoutineDecl routine = (RoutineDecl)symbol_found.getType().getLink();
		    RoutineBody rb = routine.getRoutineBody();
		    if (rb.getBody() == null) { // found a forward decl
			if (decl.getRoutineBody().getBody() == null) { // forward declaration conflict;
			    print(decl, "forward declaration for " + name + " already existed");
			    return -1;
			}
			if (symbol_found.getType().getType() == "null" && decl.getType() != null) {
			    print(decl, name + " is pre-declared as a procedure, not as a function");
			    return -1;
			}
			if (symbol_found.getType().getType() != "null") {
			    if (decl.getType() == null) {
				print(decl, name + " is pre-declared as a function, not as a procedure");
				return -1;
			    } else if (!decl.getType().equalTo(symbol_found.getType().getType())) {
				print(decl, "return type of " + name + " is different from that of forward declaration");
				return -1;
			    }
			}
			
			ASTList<ScalarDecl> arg_list_expected = routine.getRoutineBody().getParameters();
			LinkedList<ScalarDecl> arg_ll_expected = null;
			int size_expected = 0;
			if (arg_list_expected != null) {
			    arg_ll_expected = arg_list_expected.get_list();
			    size_expected = arg_list_expected.get_list().size();
			}
			if(size_expected != size_used){
			    print(decl, "forward declaration error: argument size mismatch for " + routine + " : expect " + size_expected + " arguments, used " + size_used + " arguments");
			    return -1;
			} else {
			    int i;
			    for (i = 0;i < size_used; i++){
				ScalarDecl expn=arg_ll.get(i);
				ScalarDecl scalar_decl=arg_ll_expected.get(i);
				if((expn.getType() instanceof IntegerType)){
				    if(!(scalar_decl.getType() instanceof IntegerType)){
					print(expn, "forward declaration error: expect argument number " + i+1 + " type boolean");
					return -1;
				    }
				}
				if((expn.getType() instanceof BooleanType)){
				    if(!(scalar_decl.getType() instanceof BooleanType)){
					print(expn, "forward declaration error: expect argument number " + i+1 + " type integer");
					return -1;
				    }
				}
			    }
			}
			return 0;
		    }
		}
	    }
	    
	    return 0;
	}
	
	/** handles declaration part semantic checking */
	private int handle_part_declaration(DeclarationPart dp, Hashtable<String,Symbol> symboltable, ScopeType scope_type, Type type, int lexic_level, int order_number) {
	    
	    if (dp instanceof ScalarDeclPart) {
		check_if_declared(symboltable, dp);
	    }
	    
	    if (dp instanceof ArrayDeclPart) {
		check_if_declared(symboltable, dp);
		
		// Semantic analysis S46: check that lower bound is <= upper bound
		ArrayDeclPart adp = (ArrayDeclPart)dp;
		if (adp.isTwoDimensional()) { // 2 dim
		    if ((adp.getLowerBoundary1() > adp.getUpperBoundary1()) || (adp.getLowerBoundary2() > adp.getUpperBoundary2())) {
			print(dp, "array " + adp.toString() + " : lower boundary greater than upper boundary");
		    }
		} else { // 1 dim
		    if (adp.getLowerBoundary1() > adp.getUpperBoundary1()) {
			print(dp, "array " + adp.toString() + " : lower boundary greater than upper boundary");
		    }
		}
		int size = adp.calculate_array_size();
		
		symbolTable.add_to_symboltable(dp, symboltable, type, lexic_level, symbolTable.current_order_number_ll[lexic_level]);
		symbolTable.current_order_number_ll[lexic_level] += size;
		return order_number + 1;
	    }
	    
	    symbolTable.add_to_symboltable(dp, symboltable, type, lexic_level, symbolTable.current_order_number_ll[lexic_level]);
	    symbolTable.current_order_number_ll[lexic_level]++;
	    return order_number + 1;
	}
	
	
	
	/** handles statement semantic checking */
	private void handle_statement(Stmt stmt, ScopeType scope_type, Object ref, int lexic_level) {
	
	    if(stmt instanceof Scope){
		Scope scope = (Scope) stmt; // find the scope from stmtlist
		ScopeType st = scope_type;
		if (scope_type == ScopeType.MAJOR) {
		    scope_type = ScopeType.MINOR;
		}
		traverse(scope, null, scope_type, ref, lexic_level, 0);
		    
	    }
	    
	    if (stmt instanceof ExitStmt) { // S50: check that exit statement is in a loop
		if (!((scope_type == ScopeType.LOOP) || (scope_type == ScopeType.LOOP_IN_FUNCTION) || (scope_type == ScopeType.LOOP_IN_PROCEDURE))) {
		    print(stmt, "exit statement not in a loop"); // S30: check boolean type
		}
		
		if (((ExitStmt)stmt).getExpn() != null) { // exit when statement
		    if(!(expn_analysis(((ExitStmt)stmt).getExpn()).equals("boolean"))){
			    print(stmt, "Boolean type required for expression in exit when statement"); // S30: check boolean type
		    }
		}
	    }
	    
	    if (stmt instanceof ResultStmt) { // S51: check that result statement is in a function
		if (!((scope_type == ScopeType.FUNCTION) || (scope_type == ScopeType.LOOP_IN_FUNCTION))) {
		    print(stmt, "result statement not in a function");
		    return;
		}
		
		RoutineDecl decl = (RoutineDecl)ref;
		if(!(expn_analysis(((ResultStmt)stmt).getValue()).equals(decl.getType().toString()))){ // S35: Check that expression type matches the return type of the function
			print(stmt, "type mismatch in result statement");
		}
	    }
	    
	    if (stmt instanceof ReturnStmt) { // S52: check that return statement is in a procedure
		if (!((scope_type == ScopeType.PROCEDURE) || (scope_type == ScopeType.LOOP_IN_PROCEDURE))) {
		    print(stmt, "return statement not in a procedure");
		}
	    }
	    
	    if (stmt instanceof LoopingStmt) { // looping statement (while/repeat)
		if(!(expn_analysis(((LoopingStmt)stmt).getExpn()).equals("boolean"))){
			print(stmt, "Boolean type required for expression in loop"); // S30: check boolean type
		}
		ASTList<Stmt> whileStmts = ((LoopingStmt)stmt).getBody();
		LinkedList<Stmt> whilestmt_ll=whileStmts.get_list();
		// set scope type according to current scope
		ScopeType st = ScopeType.LOOP;
		switch (scope_type) {
		    case FUNCTION:
			st = ScopeType.LOOP_IN_FUNCTION;
			break;
		    case PROCEDURE:
			st = ScopeType.LOOP_IN_PROCEDURE;
			break;
		    case LOOP_IN_FUNCTION:
			st = ScopeType.LOOP_IN_FUNCTION;
			break;
		    case LOOP_IN_PROCEDURE:
			st = ScopeType.LOOP_IN_PROCEDURE;
			break;
		    default:
			st = ScopeType.LOOP;
			break;
		}
		for (Stmt whileStmt : whilestmt_ll) {
		    handle_statement(whileStmt, st, ref, lexic_level);
		}
	    }
	    
	    // S34: check that variable and expression in assignment are the same type
	    if(stmt instanceof AssignStmt){
		    AssignStmt asgn_stmt = (AssignStmt) stmt;
		    Expn expn = asgn_stmt.getRval();
		    Expn var = asgn_stmt.getLval();
		    String var_type = variable_analysis(var);
		    String expn_type = expn_analysis(expn);
		    if(var_type != "" && expn_type != "" && !(var_type.equals(expn_type))){
			    print(expn, "Type error: expect variable " + var.toString() + " type: " + var_type + ", but given type: " + expn_type);
		    }
	    }
	    
	    if(stmt instanceof IfStmt){
		IfStmt if_stmt=(IfStmt) stmt;
		if(!(expn_analysis(if_stmt.getCondition()).equals("boolean"))){
			print(if_stmt.getCondition(), "Boolean type required for expression in if statement");  // S30: check boolean type
		}
		
		ScopeType st = scope_type;
		if (scope_type == ScopeType.MAJOR) {
		    scope_type = ScopeType.MINOR;
		}
		
		recursive_stmt(if_stmt.getWhenTrue(), scope_type, ref, lexic_level); // check statements after then
		if (if_stmt.getWhenFalse() != null) {
		    recursive_stmt(if_stmt.getWhenFalse(), scope_type, ref, lexic_level); // check statements after else
		}
	    }
	    
	    if(stmt instanceof ProcedureCallStmt){ // S43: check that the number of arguments is equal to the number of formal parameters
		ProcedureCallStmt proc_stmt = (ProcedureCallStmt)stmt;
		ASTList<Expn> arg_list = (proc_stmt).getArguments();
		int size_used = 0;
		String name = proc_stmt.getName();
		LinkedList<Expn> arg_ll = null;
		if (arg_list != null) {
		    arg_ll = arg_list.get_list();
		    size_used = arg_ll.size();
		}
		
		Symbol symbol_found = symbolTable.find_variable(name);
		
		if(symbol_found == null){
		    print(stmt, "procedure \"" + name + "\" is not defined");
		} else {
		    if(symbol_found.getType().getLink() instanceof RoutineDecl){
			if (symbol_found.getType().getType() != "null") {
			    print(stmt, name + " is declared as a function, not as a procedure");
			    return;
			}
			RoutineDecl routine=(RoutineDecl) symbol_found.getType().getLink();
			ASTList<ScalarDecl> arg_list_expected = routine.getRoutineBody().getParameters();
			int size_expected = 0;
			if (arg_list_expected != null) {
			    size_expected = arg_list_expected.get_list().size();
			}
			
			if(size_expected != size_used){
			    print(stmt, "argument size mismatch for " + routine + " : expect " + size_expected + " arguments, used " + size_used + " arguments");
			} else {
			    if (size_expected == 0) {
				return;
			    }
			    int n = check_arguments_match(arg_ll, arg_list_expected.get_list()); // return "" if error
			    if (n < 0) {
				return;
			    }
			}
		    } else {
			print(stmt, name + " is not declared as a procedure");
		    }
		}
	    }
	    
	    if(stmt instanceof PutStmt){
		PutStmt put_stmt = (PutStmt) stmt;
		handle_output(put_stmt);
	    }
	    
	    if(stmt instanceof GetStmt){
		GetStmt get_stmt = (GetStmt) stmt;
		handle_input(get_stmt);
	    }
	    
	    return;
	}

	/** handles output semantic checking */
	private void handle_output(PutStmt stmt){
	    ASTList<Printable> outputs = stmt.getOutputs();
	    LinkedList<Printable> output_ll = outputs.get_list();
	    for(Printable output : output_ll){
		if ((output instanceof TextConstExpn) || (output instanceof NewlineConstExpn)) {
		    continue;
		} else if (output instanceof Expn){
		    Expn expn = (Expn) output;
		    if(!(expn_analysis(expn).equals("integer"))){ // S31: Check that type of expression or variable is integer
			print(expn, "Type of " + expn + " in put statement is not integer");
		    }
		}
	    }
	}
	
	/** handles input semantic checking */
	private void handle_input(GetStmt stmt){
	    ASTList<Readable> inputs = stmt.getInputs();
	    LinkedList<Readable> input_ll = inputs.get_list();
	    for(Readable input : input_ll){
		if(input instanceof IdentExpn){
		    IdentExpn expn = (IdentExpn) input;
		    if(!(variable_analysis(expn).equals("integer"))){ // S31: Check that type of expression or variable is integer
			print(expn, "Type of " + expn + " in get statement is not integer");
		    }
		} else if (input instanceof SubsExpn) {
		    SubsExpn expn = (SubsExpn) input;
		    if(!(variable_analysis(expn).equals("integer"))){ // S31: Check that type of expression or variable is integer
			print(expn, "Type of " + expn + " in get statement is not integer");
		    }
		}
	    }
	}
	
	/** print out the symbol table of the given level */
	private void printHash(Hashtable<String,Symbol> ht) {
	    Set<String> keyset = ht.keySet();
	    for (String s : keyset) {
			Symbol sym = ht.get(s);
//			System.out.println(sym.toString());
			ps.print(sym.toString() + "\n");
	    }
	}
	
	/** check whether name is declared in current scope */
	private void check_if_declared(Hashtable<String,Symbol> ht, DeclarationPart dp) {
	    String name = dp.getName();
	    Set<String> keyset = ht.keySet();
	    if (keyset.contains(name)) {
		print(dp, "name \"" + name + "\" is already defined in the scope");
		return;
	    }
	}
	
	/** check whether name is declared in current scope */
	private void check_if_declared(Hashtable<String,Symbol> ht, Declaration decl) {
	    String name = decl.getName();
	    Set<String> keyset = ht.keySet();
	    if (keyset.contains(name)) {
		// if it's a forward declaration, we are good
		Symbol sym = ht.get(name);
		if (sym.getKind().equals("func")) {
		    RoutineDecl routine = (RoutineDecl)sym.getType().getLink();
		    if (routine.getRoutineBody().getBody() == null) { // it's a forward declaration, do not throw error
			return;
		    }
		}
	    
		print(decl, "name \"" + name + "\" is already defined in the scope");
		return;
	    }
	}
	
	/** S54: associate params if any with scope */
	private int add_params(Hashtable<String,Symbol> ht, ASTList<ScalarDecl> params, int lexic_level, int order_number) {
// 	    System.out.println("add_params");
	    if (params != null) {
		LinkedList<ScalarDecl> l = params.get_list();
		for (ScalarDecl d : l) {
		    check_if_declared(ht, d);
		    symbolTable.add_to_symboltable(d, ht, lexic_level, symbolTable.current_order_number_ll[lexic_level]);
		    symbolTable.current_order_number_ll[lexic_level]++;
		    order_number++;
		}
	    }
	    return order_number;
	}
	
	
	/** handle semantic checking of expressions */
	private String expn_analysis(Expn expn){
            if(expn instanceof IntConstExpn){
                return "integer";
            }
            
            if(expn instanceof UnaryMinusExpn){
            	Expn operand = ((UnaryMinusExpn)expn).getOperand();
                if(!(expn_analysis(operand).equals("integer"))){ // S31: check integer type
                        print(expn, "bad operand type for unary : " + operand + " has to be integer");
                }
                return "integer";
            }
            
            if(expn instanceof ArithExpn){
                ArithExpn arith_expn=(ArithExpn) expn;
                if(!(expn_analysis(arith_expn.getLeft()).equals("integer"))){
                        print(expn, "bad operand type(s) for " + arith_expn.getOpSymbol() + " : " + arith_expn.getLeft() + " has to be integer");
                }
                if(!(expn_analysis(arith_expn.getRight()).equals("integer"))){
                        print(expn, "bad operand type(s) for " + arith_expn.getOpSymbol() + " : " + arith_expn.getRight() + " has to be integer");
                }
                return "integer";
            }
            
            if(expn instanceof BoolConstExpn){
                return "boolean";
            }
            
            if(expn instanceof NotExpn){ // S30: check boolean type
            	Expn operand = ((NotExpn)expn).getOperand();
                if(!(expn_analysis(operand).equals("boolean"))){
                        print(expn, "bad operand type for not : " + operand + " has to be boolean");
                }
                return "boolean";
            }
            
            if(expn instanceof BoolExpn){
                BoolExpn bool_expn=(BoolExpn) expn;
                if(!(expn_analysis(bool_expn.getLeft()).equals("boolean"))){
                        print(expn, "bad operand type(s) for " + bool_expn.getOpSymbol() + " : " + bool_expn.getLeft() + " has to be boolean");
                }
                if(!(expn_analysis(bool_expn.getRight()).equals("boolean"))){
                        print(expn, "bad operand type(s) for " + bool_expn.getOpSymbol() + " : " + bool_expn.getRight() + " has to be boolean");
                }
                return "boolean";
            }
            
            if(expn instanceof EqualsExpn){
                EqualsExpn equals_expn=(EqualsExpn) expn;
                if(!(expn_analysis(equals_expn.getLeft()).equals(expn_analysis(equals_expn.getRight())))){ // S32: check that left and right operand have same type
                        print(expn, "uncomparable types between " + equals_expn.getLeft() + " and " + equals_expn.getRight());
                }
                return "boolean";
            }
            
            if(expn instanceof CompareExpn){
                CompareExpn comp_expn=(CompareExpn) expn;
                if(!(expn_analysis(comp_expn.getLeft()).equals("integer"))){
                        print(expn, "bad operand type(s) for " + comp_expn.getOpSymbol() + " : " + comp_expn.getLeft() + " has to be integer");
                }
                if(!(expn_analysis(comp_expn.getRight()).equals("integer"))){
                        print(expn, "bad operand type(s) for " + comp_expn.getOpSymbol() + " : " + comp_expn.getRight() + " has to be integer");
                }
                return "boolean";
            }
            
            if(expn instanceof ParenthExpn){
                ParenthExpn parenth_expn=(ParenthExpn) expn;
                return expn_analysis(parenth_expn.getParenth());
            }
            
            if(expn instanceof ConditionalExpn){
                ConditionalExpn cond_expn=(ConditionalExpn) expn;
                if(!(expn_analysis(cond_expn.getCondition()).equals("boolean"))){ // S30: check boolean type
                        print(expn, "bad conditional expression : " + cond_expn.getCondition() + " has to be boolean");
                }
                if(!(expn_analysis(cond_expn.getTrueValue()).equals(expn_analysis(cond_expn.getFalseValue())))){ // S33: Both exprs in conditional are the same type
                        print(expn, "incompatible types between " + cond_expn.getTrueValue() + " and " + cond_expn.getFalseValue());
                }
                return expn_analysis(cond_expn.getTrueValue()); // S24: set result type of conditional expressions
            }
            
            if(expn instanceof FunctionCallExpn){ // S43: check argument number match
                FunctionCallExpn func_expn=(FunctionCallExpn) expn;
		
                int size_used = 0;
                String name = func_expn.getIdent();
				ASTList<Expn> arg_list = func_expn.getArguments();
				LinkedList<Expn> arg_ll = null;
				if (arg_list != null) {
				    arg_ll = arg_list.get_list();
				    size_used = arg_ll.size();
				}
				
				Symbol symbol_found = symbolTable.find_variable(name);
				
				if(symbol_found == null){
				    print(expn, "function \"" + name + "\" is not defined");
				} else {
				    if(symbol_found.getType().getLink() instanceof RoutineDecl){
					if (symbol_found.getType().getType() == "null") {
					    print(expn, name + " is declared as a procedure, not as a function");
					    return "";
					}
					RoutineDecl routine=(RoutineDecl) symbol_found.getType().getLink();
					ASTList<ScalarDecl> arg_list_expected = routine.getRoutineBody().getParameters();
					int size_expected = 0;
					if (arg_list_expected != null) {
					    size_expected = arg_list_expected.get_list().size();
					}
					
					if(size_expected != size_used){
					    print(expn, "argument size mismatch for " + routine + " : expect " + size_expected + " arguments, used " + size_used + " arguments");
					} else {
					    if (size_expected == 0) {
						return symbol_found.getType().getType();
					    }
					    int n = check_arguments_match(arg_ll, arg_list_expected.get_list()); // return "" if error
					    if (n < 0) {
						return "";
					    }
					}
					return symbol_found.getType().getType();
				    } else {
					print(expn, name + " is not declared as a function");
				    }
				}
				return "";
		
            }
            
            if ((expn instanceof IdentExpn) || (expn instanceof SubsExpn)) {
                return variable_analysis(expn);
            }
            
            return "";
            
        }
       
       /** check whether two list of arguments match */
       private int check_arguments_match(LinkedList<Expn> arg_ll, LinkedList<ScalarDecl> arg_ll_expected) {
// 	     System.out.println("check_arguments_match");
	     int i;
	     for(i = 0; i < arg_ll.size(); i++){
			Expn expn = arg_ll.get(i);
			ScalarDecl decl = arg_ll_expected.get(i);
			if(expn_analysis(expn).equals("integer")){
			    if(!(decl.getType() instanceof IntegerType)){
				print(expn, "type error: expect argument number " + i+1 + " type boolean");
				return -1;
			    }
			}
			if(expn_analysis(expn).equals("boolean")){
			    if(!(decl.getType() instanceof BooleanType)){
				print(expn, "type error: expect argument number " + i+1 + " type integer");
				return -1;
			    }
			}
	     }
	     
	     return 0;
       }

       
        /** handle semantic checking of variables
         * S29: check whether the variable is declared or visible */
        private String variable_analysis(Expn expn){
		    if(expn instanceof IdentExpn){
				IdentExpn ident_expn=(IdentExpn) expn;
				Symbol symbol_found = symbolTable.find_variable(ident_expn.toString());
				
				if(symbol_found == null){
				    print(ident_expn, "variable \"" + ident_expn.toString() + "\" is not defined");
				} else {
				    if (symbol_found.getKind().equals("func") || symbol_found.getKind().equals("array")) {
					print(ident_expn, ident_expn.toString() + " is not a valid variable");
					return "";
				    }
				    return symbol_found.getType().getType();
				}
            }

            if(expn instanceof SubsExpn){ // array check
				SubsExpn sub_expn=(SubsExpn) expn;
		
				if(!(expn_analysis(sub_expn.getSubscript1()).equals("integer"))){ // S31: integer type check
				    print(expn, "First index should be integer");
				}
				if(sub_expn.getSubscript2() != null){
				    if(!(expn_analysis(sub_expn.getSubscript2()).equals("integer"))){
					print(expn, "Second index should be integer");
				    }
				}
		
				Symbol symbol_found = symbolTable.find_variable(sub_expn.getVariable());
				
				if(symbol_found == null){
				    print(expn, "variable \"" + sub_expn.getVariable() + "\" is not defined");
				} else { // S38, S55: 1 dimentional / 2 dimentional array check
				    if(symbol_found.getType().getLink() instanceof ArrayDeclPart){
					ArrayDeclPart array=(ArrayDeclPart) symbol_found.getType().getLink();
					if(array.isTwoDimensional() && sub_expn.getSubscript2() == null){
					    print(expn, sub_expn.getVariable()+" should be two dimensional array");
					} else if (!array.isTwoDimensional() && sub_expn.getSubscript2() != null) {
					    print(expn, sub_expn.getVariable()+" should be one dimensional array");
					}
				    } else {
					print(expn, sub_expn.getVariable() + " is not declared as an array");
				    }
				    return symbol_found.getType().getType();
				}
		
            }

            return "";
	}
	
	/** print line/column number with the error message. */
	private void print(AST ast, String string) {
	    System.out.println("Line: " + ast.getLine() + " Column: " + ast.getCol() + " : " + string);
	    error_flag = true;
	}
	

}

