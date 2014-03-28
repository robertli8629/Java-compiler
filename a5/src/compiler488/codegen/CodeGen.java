package compiler488.codegen;

import java.io.*;
import java.util.*;
import compiler488.compiler.Main;
import compiler488.runtime.Machine;
import compiler488.runtime.MemoryAddressException;
import compiler488.symbol.*;
import compiler488.ast.ASTList;
import compiler488.ast.stmt.*;
import compiler488.ast.decl.*;
import compiler488.ast.expn.*;
import compiler488.ast.type.*;
import compiler488.ast.Readable;
import compiler488.ast.Printable;
import compiler488.ast.AST;

/**      CodeGenerator.java 
 *<pre>
 *  Code Generation Conventions
 *
 *  To simplify the course project, this code generator is
 *  designed to compile directly to pseudo machine memory
 *  which is available as the private array memory[]
 *
 *  It is assumed that the code generator places instructions
 *  in memory in locations
 *
 *      memory[ 0 .. startMSP - 1 ]
 *
 *  The code generator may also place instructions and/or
 *  constants in high memory at locations (though this may
 *  not be necessary)
 *      memory[ startMLP .. Machine.memorySize - 1 ]
 *
 *  During program exection the memory area
 *      memory[ startMSP .. startMLP - 1 ]
 *  is used as a dynamic stack for storing activation records
 *  and temporaries used during expression evaluation.
 *  A hardware exception (stack overflow) occurs if the pointer
 *  for this stack reaches the memory limit register (mlp).
 *
 *  The code generator is responsible for setting the global
 *  variables:
 *      startPC         initial value for program counter
 *      startMSP        initial value for msp
 *      startMLP        initial value for mlp
 * </pre>
 * @author  <B> PUT YOUR NAMES HERE </B>
 */

public class CodeGen
    {

    /** initial value for memory stack pointer */
    private short startMSP;
    /** initial value for program counter */
    private short startPC;
    /** initial value for memory limit pointer */
    private short startMLP;

    /** flag for tracing code generation */
    private boolean traceCodeGen = Main.traceCodeGen ;

    private short current_msp = 0;
    private SymbolTable symbolTable;
    private SymbolTable global_st;
    /**  
     *  Constructor to initialize code generation
     */
    public CodeGen()
	{
	// YOUR CONSTRUCTOR GOES HERE.

	}

    // Utility procedures used for code generation GO HERE.

    /** 
     *  Additional intialization for gode generation.
     *  Called once at the start of code generation.
     *  May be unnecesary if constructor does everything.
     */

   /** Additional initialization for Code Generation (if required) */
   public void Initialize(Program programAST, SymbolTable st) throws MemoryAddressException
	{
	/********************************************************/
	/* Initialization code for the code generator GOES HERE */
	/* This procedure is called once before codeGeneration  */      
	/*                                                      */
	/********************************************************/
	global_st = st;
	symbolTable = new SymbolTable();
	
// 	System.out.println(global_st.current_order_number_ll[0]);
// 	System.out.println(global_st.current_order_number_ll[1]);
	
	int alloc_size = global_st.current_order_number_ll[0];
	
	Machine.setPC( (short) 0 ) ;		/* where code to be executed begins */
	
	// debug
// 	Machine.writeMemory(current_msp++, Machine.TRON); // TRON
	
	// init main scope
	Machine.writeMemory(current_msp++, Machine.PUSHMT); // PUSHMT
	Machine.writeMemory(current_msp++, Machine.SETD); // SETD
	Machine.writeMemory(current_msp++, (short)0); // 0
	push(Machine.UNDEFINED); // PUSH UNDEFINED
	push(alloc_size); // PUSH main_needed_words
	Machine.writeMemory(current_msp++, Machine.DUPN); // DUPN
	
	// start main scope
	traverse((Scope) programAST, null, null, 0, null);
	
	// exit main scope
	Machine.writeMemory(current_msp++, Machine.HALT); // HALT
	
	Machine.setMSP(current_msp);   	/* where memory stack begins */
	Machine.setMLP((short) ( Machine.memorySize -1 ) );	
	
	return;
	}

    
    /**  
     *  Perform any requred cleanup at the end of code generation.
     *  Called once at the end of code generation.
     *  @throws MemoryAddressException
     */
    void Finalize()
        throws MemoryAddressException     // from Machine.writeMemory 
	{
	/********************************************************/
	/* Finalization code for the code generator GOES HERE.  */      
	/*                                                      */
	/* This procedure is called once at the end of code     */
	/* generation                                           */
	/********************************************************/

	//  REPLACE THIS CODE WITH YOUR OWN CODE
	//  THIS CODE generates a single HALT instruction 
        //  as an example.
 	Machine.setPC( (short) 0 ) ;		/* where code to be executed begins */
	Machine.setMSP((short)  1 );   	/* where memory stack begins */
	Machine.setMLP((short) ( Machine.memorySize -1 ) );	
					/* limit of stack */
        Machine.writeMemory((short)  0 , Machine.HALT );

	return;
	}

    /** Procedure to implement code generation based on code generation
     *  action number
     * @param actionNumber  code generation action to perform
     */
    void generateCode( int actionNumber )
	{
	if( traceCodeGen )
	    {
		//output the standard trace stream
		Main.traceStream.println("CodeGen: C" +  actionNumber );
	    }

	/****************************************************************/
	/*  Code to implement the code generation actions GOES HERE     */
	/*  This dummy code generator just prints the actionNumber      */
	/*  In Assignment 5, you'll implement something more interesting */
        /*                                                               */
        /*  FEEL FREE TO ignore or replace this procedure                */
	/****************************************************************/

        System.out.println("Codegen: C" + actionNumber ); 
	return;
	}

     //  ADDITIONAL FUNCTIONS TO IMPLEMENT CODE GENERATION GO HERE
     
     
     	/** 
	 * main code generation routine
	 * arg: if not null, it represents parameter list passed by function/procedure declaration scope
	 * ref: if not null, it represents function declaration, which is used to check the return type of the function
	 *  */
    private void traverse(Scope s, ASTList<ScalarDecl> arg, Object ref, int lexic_level, LinkedList<Short> exit_addr_list) throws MemoryAddressException {

	Hashtable<String,Symbol> symboltable=new Hashtable<String,Symbol>();

	ASTList<Declaration> AST_dcl=s.getDeclarations();
	LinkedList<Declaration> ll=AST_dcl.get_list();
	
	symbolTable.symbolstack.push(symboltable);
	
	if (arg != null) {
	    add_params(symboltable, arg, lexic_level);
	}
	
	if (ll != null){
	    ListIterator iterator = ll.listIterator();
	    while (iterator.hasNext()){
		    Declaration decl = (Declaration)iterator.next();
		    
		    handle_declaration(decl, symboltable, lexic_level);
	    }
	}
	
	
// 	    if (showSymbolTable) { 
// 	    	printHash(symboltable);
// 	    }
// 	printHash(symboltable);
	
	// recursion
	ASTList<Stmt> AST_stat=s.getStatements();
	LinkedList<Stmt> stmt_ll=AST_stat.get_list();
	if (stmt_ll != null){
	    ListIterator iterator_stmt = stmt_ll.listIterator();
	    while (iterator_stmt.hasNext()){
		    Stmt stmt = (Stmt)iterator_stmt.next();
		    if (exit_addr_list == null) {
			exit_addr_list = new LinkedList<Short>();
		    }
		    generate_statement(stmt, exit_addr_list, lexic_level);
	    }
	}
	
	symbolTable.symbolstack.pop();
	
    }
    
    /** handles declaration */
    private void handle_declaration(Declaration decl, Hashtable<String,Symbol> symboltable, int lexic_level) throws MemoryAddressException {
	
	if (decl instanceof MultiDeclarations) {
	    ASTList<DeclarationPart> decl_list = ((MultiDeclarations)decl).getElements();
	    LinkedList<DeclarationPart> ll_part=decl_list.get_list();
	    
	    for (DeclarationPart dp : ll_part) {
		handle_part_declaration(dp, symboltable, decl.getType(), lexic_level);
	    }
	    return;
	}
	
	if (decl instanceof RoutineDecl) {
	    short save_BR_address=(short)(current_msp+1);
	    push(0);
	    Machine.writeMemory(current_msp++, Machine.BR); //BR
	    RoutineBody rb = ((RoutineDecl)decl).getRoutineBody();
	    Scope routine_scope = rb.getBody();
	    if (routine_scope != null) { // not a forward decl

		symbolTable.add_to_symboltable(decl, symboltable, lexic_level, symbolTable.current_order_number_ll[lexic_level], current_msp); // add first for recursive definition
// 		System.out.println("current_msp: " + current_msp);
		symbolTable.current_order_number_ll[lexic_level]++;
		ASTList<ScalarDecl> params = rb.getParameters();
		if (decl.getType() == null) { // procedure
		    traverse(routine_scope, params, null, lexic_level + 1,null);
		    Machine.writeMemory(save_BR_address,current_msp);
		} else { // function
		    traverse(routine_scope, params, decl, lexic_level + 1,null);
		    Machine.writeMemory(save_BR_address,current_msp);
		}
		return;
	    }
	}
	
	// add it to symbol table
	symbolTable.add_to_symboltable(decl, symboltable, lexic_level, symbolTable.current_order_number_ll[lexic_level]);
	symbolTable.current_order_number_ll[lexic_level]++;
    
	return;
    }
    
    /** handles declaration part */
    private void handle_part_declaration(DeclarationPart dp, Hashtable<String,Symbol> symboltable, Type type, int lexic_level) throws MemoryAddressException {
	
	symbolTable.add_to_symboltable(dp, symboltable, type, lexic_level, symbolTable.current_order_number_ll[lexic_level]);
	symbolTable.current_order_number_ll[lexic_level]++;
	return;
    }
    
    private void add_params(Hashtable<String,Symbol> ht, ASTList<ScalarDecl> params, int lexic_level) {
// 	    System.out.println("add_params");
	if (params != null) {
	    LinkedList<ScalarDecl> l = params.get_list();
	    for (ScalarDecl d : l) {
		symbolTable.add_to_symboltable(d, ht, lexic_level, symbolTable.current_order_number_ll[lexic_level]);
		symbolTable.current_order_number_ll[lexic_level]++;
	    }
	}
	return;
    }
    
    private void printHash(Hashtable<String,Symbol> ht) {
	Set<String> keyset = ht.keySet();
	for (String s : keyset) {
	    Symbol sym = ht.get(s);
	    System.out.println(sym.toString());
// 		ps.print(sym.toString() + "\n");
	}
    }
	
    private void push(short value) throws MemoryAddressException {
	Machine.writeMemory(current_msp++, Machine.PUSH);
        Machine.writeMemory(current_msp++, value);
    }
    
    private void push(int value) throws MemoryAddressException {
	Machine.writeMemory(current_msp++, Machine.PUSH);
        Machine.writeMemory(current_msp++, (short)value);
    }
    
    private void addr(short LL, short ON) throws MemoryAddressException {
	Machine.writeMemory(current_msp++, Machine.ADDR);
	Machine.writeMemory(current_msp++, LL);
	Machine.writeMemory(current_msp++, ON);
    }
    
    private void generate_statement(Stmt stmt, LinkedList<Short> exit_addr_list, int lexic_level) throws MemoryAddressException{
        if(stmt instanceof Scope){
	    Scope scope = (Scope) stmt; 
	    traverse(scope, null, null, lexic_level, exit_addr_list);
	}
        if(stmt instanceof AssignStmt) {
            AssignStmt asgn_stmt = (AssignStmt) stmt;
            Expn expn = asgn_stmt.getLval();
	    addr_variable(expn);
	    generate_expression(asgn_stmt.getRval());
	    Machine.writeMemory(current_msp++, Machine.STORE); //STORE
        }
        if(stmt instanceof IfStmt) {
            IfStmt if_stmt = (IfStmt) stmt;
            generate_expression(if_stmt.getCondition());
            short save_BF_address=(short)(current_msp + 1);
            push(Machine.UNDEFINED);
            Machine.writeMemory(current_msp++, Machine.BF);//BF
       
            ASTList<Stmt> stmt_list = if_stmt.getWhenTrue();
            LinkedList<Stmt> stmt_ll = stmt_list.get_list();
            int i;
            for (i = 0; i < stmt_ll.size(); i++) {
                Stmt list_stmt = stmt_ll.get(i);
                generate_statement(list_stmt, exit_addr_list, lexic_level);
            }
            if (if_stmt.getWhenFalse()!=null) {
                short save_BR_address=(short)(current_msp + 1);
                push(Machine.UNDEFINED);
                Machine.writeMemory(current_msp++, Machine.BR);//BR
               
                Machine.writeMemory(save_BF_address,current_msp);
                ASTList<Stmt> false_list = if_stmt.getWhenFalse();
                LinkedList<Stmt> false_ll = false_list.get_list();
                for(i = 0; i < false_ll.size(); i++){
                    Stmt fasle_stmt = false_ll.get(i);
                    generate_statement(fasle_stmt, exit_addr_list, lexic_level);
                }
               
                Machine.writeMemory(save_BR_address, current_msp);
            } else {
                Machine.writeMemory(save_BF_address, current_msp);
            }
            return;
        }
        if (stmt instanceof LoopingStmt) {
            LoopingStmt loop_stmt=(LoopingStmt) stmt;
            LinkedList<Short> exit_list=new LinkedList<Short>();
            if (loop_stmt instanceof RepeatUntilStmt) {
                short save_BF_address= current_msp;
                ASTList<Stmt> body_list = loop_stmt.getBody();
                LinkedList<Stmt> body_ll = body_list.get_list();
                int i;
                for (i = 0; i < body_ll.size(); i++) {
                    Stmt body_stmt = body_ll.get(i);
                    generate_statement(body_stmt, exit_list, lexic_level);
                }
               
                generate_expression(loop_stmt.getExpn());
                Machine.writeMemory(current_msp++, Machine.PUSH); // PUSH
                Machine.writeMemory(current_msp++,save_BF_address);
                Machine.writeMemory(current_msp++, Machine.BF); // BF
                Iterator<Short> iterator = exit_list.iterator();
                while (iterator.hasNext()) {
                    Machine.writeMemory(iterator.next(), current_msp);
                }
                return;
            } else { // WhileDoStmt
                short save_BR_address=current_msp;
                generate_expression(loop_stmt.getExpn());
                short save_BF_address=(short)(current_msp+1);
                push(Machine.UNDEFINED);
                Machine.writeMemory(current_msp++, Machine.BF);//BF
               
                ASTList<Stmt> body_list = loop_stmt.getBody();
                LinkedList<Stmt> body_ll = body_list.get_list();
                int i;
                for (i = 0; i < body_ll.size(); i++) {
                    Stmt body_stmt = body_ll.get(i);
                    generate_statement(body_stmt, exit_list, lexic_level);
                }
               
                push(save_BR_address);
                Machine.writeMemory(current_msp++, Machine.BR);//BR
                Machine.writeMemory(save_BF_address,current_msp);
                Iterator<Short> iterator = exit_list.iterator();
                while (iterator.hasNext()) {
                    Machine.writeMemory(iterator.next(),current_msp);
                }
                return;
            }
        }
        if (stmt instanceof ExitStmt) {
            ExitStmt exit_stmt = (ExitStmt) stmt;      
            if (exit_stmt.getExpn() == null) { // exit stmt
                exit_addr_list.add((short)(current_msp+1));
                push(Machine.UNDEFINED);
                Machine.writeMemory(current_msp++, Machine.BR); //BR
            } else { // exit when stmt
                generate_expression(exit_stmt.getExpn());
                push(1);
                Machine.writeMemory(current_msp++, Machine.SUB); //SUB
                Machine.writeMemory(current_msp++, Machine.NEG); //NEG
                exit_addr_list.add((short)(current_msp+1));
                push(Machine.UNDEFINED);
                Machine.writeMemory(current_msp++, Machine.BF); //BF
            }
        }
        if (stmt instanceof ResultStmt) {
            ResultStmt result_stmt = (ResultStmt) stmt;
            generate_expression(result_stmt.getValue());
            Machine.writeMemory(current_msp++, Machine.STORE);
       
//             push();
            Machine.writeMemory(current_msp++, Machine.POPN);//POPN
            Machine.writeMemory(current_msp++, Machine.SETD);//SETD
            Machine.writeMemory(current_msp++, Machine.BR);//BR
       
            return;
        }
        if (stmt instanceof ReturnStmt) {
//             push();
            Machine.writeMemory(current_msp++, Machine.POPN);//POPN
            Machine.writeMemory(current_msp++, Machine.SETD);//SETD
            Machine.writeMemory(current_msp++, Machine.BR);//BR
            return;
        }
        if (stmt instanceof GetStmt) {
            GetStmt get_stmt=(GetStmt) stmt;
            ASTList<Readable> inputs = get_stmt.getInputs();
            LinkedList<Readable> input_ll = inputs.get_list();
            for(Readable input : input_ll){               
                if (input instanceof IdentExpn) {
		    IdentExpn ident_expn=(IdentExpn) input;
		    Symbol symbol = symbolTable.find_variable(ident_expn.toString());
		    addr(symbol.getll(), symbol.geton());
		}
		if (input instanceof SubsExpn) { // array 
		    SubsExpn sub_expn=(SubsExpn) input;
		    Symbol symbol = symbolTable.find_variable(sub_expn.getVariable());
		    Expn sub_expn1 = sub_expn.getSubscript1();
		    Expn sub_expn2 = sub_expn.getSubscript2();
		    ArrayDeclPart adp = (ArrayDeclPart)symbol.getType().getLink();
		    addr(symbol.getll(), symbol.geton());
		    generate_expression(sub_expn1);
		    int lb1 = adp.getLowerBoundary1();
		    push(lb1);
		    Machine.writeMemory(current_msp++, Machine.SUB); //SUB
		    if (sub_expn2 != null) { // 2 dimensional
			int ub2 = adp.getUpperBoundary2();
			int lb2 = adp.getLowerBoundary2();
			push(ub2 - lb2 + 1);
			Machine.writeMemory(current_msp++, Machine.MUL); //MUL
			generate_expression(sub_expn2);
			push(lb2);
			Machine.writeMemory(current_msp++, Machine.SUB); //SUB
			Machine.writeMemory(current_msp++, Machine.ADD); //ADD
		    }
		    Machine.writeMemory(current_msp++, Machine.ADD); //ADD
		}
                
                Machine.writeMemory(current_msp++, Machine.READI); //READI
                Machine.writeMemory(current_msp++, Machine.STORE); //STORE
            }
            return;
        }
        if (stmt instanceof PutStmt) {
            PutStmt put_stmt=(PutStmt) stmt;
            ASTList<Printable> outputs = put_stmt.getOutputs();
            LinkedList<Printable> output_ll = outputs.get_list();
            for (Printable output : output_ll) {
                if (output instanceof TextConstExpn) {
                    TextConstExpn text=(TextConstExpn)output;
                    String string=text.getValue();
                    for (int i=0; i<string.length(); i++) {
                        push((short)string.charAt(i));
                        Machine.writeMemory(current_msp++, Machine.PRINTC);//PRINTC
                    }
                } else if (output instanceof NewlineConstExpn) {
                    push((short)'\n');
                    Machine.writeMemory(current_msp++, Machine.PRINTC);//PRINTC
                } else if (output instanceof Expn){
                    Expn expn = (Expn) output;
                    generate_expression(expn);
                    Machine.writeMemory(current_msp++, Machine.PRINTI);//PRINTI
                }
            }
        }
        if (stmt instanceof ProcedureCallStmt) {
            ProcedureCallStmt proc_stmt = (ProcedureCallStmt)stmt;
            short save_BR_address=(short)(current_msp+1);
            push((short)0);
            Machine.writeMemory(current_msp++, Machine.ADDR);//ADDR
       
            Machine.writeMemory(current_msp++, Machine.PUSHMT);//PUSHMT
            Machine.writeMemory(current_msp++, Machine.SETD);//SETD
       
            ASTList<Expn> arg_list = proc_stmt.getArguments();
            LinkedList<Expn> arg_ll = arg_list.get_list();
            int i;
            for (i = 0; i < arg_ll.size(); i++) {
                Expn arg_expn = arg_ll.get(i);
                generate_expression(arg_expn);
            }

            push((short)0);
            Machine.writeMemory(current_msp++,Machine.BR);
        }
    }
    
    private void generate_expression(Expn expn) throws MemoryAddressException{
        if (expn instanceof IntConstExpn) {
            IntConstExpn int_expn=(IntConstExpn)expn;
            push(int_expn.getValue());
            return;
        }
        if (expn instanceof UnaryMinusExpn) {
            UnaryMinusExpn unary_expn=(UnaryMinusExpn) expn;
            generate_expression(unary_expn.getOperand());
            Machine.writeMemory(current_msp++, Machine.NEG);
        }
        if (expn instanceof ArithExpn) {
            ArithExpn arith_expn=(ArithExpn) expn;
            if (arith_expn.getOpSymbol().equals("+")) {
                generate_expression(arith_expn.getLeft());
                generate_expression(arith_expn.getRight());
                Machine.writeMemory(current_msp++, Machine.ADD);
                return;
            }
            if (arith_expn.getOpSymbol().equals("-")) {
                generate_expression(arith_expn.getLeft());
                generate_expression(arith_expn.getRight());
                Machine.writeMemory(current_msp++, Machine.SUB);
                return;
            }
            if (arith_expn.getOpSymbol().equals("*")) {
                generate_expression(arith_expn.getLeft());
                generate_expression(arith_expn.getRight());
                Machine.writeMemory(current_msp++, Machine.MUL);
                return;
            }
            if (arith_expn.getOpSymbol().equals("/")) {
                generate_expression(arith_expn.getLeft());
                generate_expression(arith_expn.getRight());
                Machine.writeMemory(current_msp++, Machine.DIV);
                return;
            }
        }
        if (expn instanceof BoolConstExpn) {
            BoolConstExpn bool_expn=(BoolConstExpn) expn;
            if (bool_expn.getValue()) {
                push(Machine.MACHINE_TRUE);
                return;
            } else {
                push(Machine.MACHINE_FALSE);
                return;
            }
        }
        if (expn instanceof NotExpn) {
            NotExpn not_expn=(NotExpn) expn;
            generate_expression(not_expn.getOperand());
            push(1);
            Machine.writeMemory(current_msp++, Machine.SUB); //SUB
            Machine.writeMemory(current_msp++, Machine.NEG); //NEG
            return;
        }
        if (expn instanceof BoolExpn) {
            BoolExpn bool_expn=(BoolExpn) expn;
            generate_expression(bool_expn.getLeft());
            short save_BF_address=(short)(current_msp+1);
            push(Machine.UNDEFINED);
            Machine.writeMemory(current_msp++, Machine.BF); // BF
            if (bool_expn.getOpSymbol().equals("and")) {
                generate_expression(bool_expn.getRight());
                short save_BR_address=(short)(current_msp+1);
                push(Machine.UNDEFINED);
                Machine.writeMemory(current_msp++, Machine.BR);//BR
                Machine.writeMemory(save_BF_address,current_msp);
                push(Machine.MACHINE_FALSE);
                Machine.writeMemory(save_BR_address,current_msp);
                return;
            }
            if (bool_expn.getOpSymbol().equals("or")) {
                push(Machine.MACHINE_TRUE);
                short save_BR_address=(short)(current_msp+1);
                push(Machine.UNDEFINED);
                Machine.writeMemory(current_msp++, Machine.BR);//BR
                Machine.writeMemory(save_BF_address,current_msp);
                generate_expression(bool_expn.getRight());
                Machine.writeMemory(save_BR_address,current_msp);
                return;
            }
        }
        if (expn instanceof EqualsExpn) {
            EqualsExpn equals_expn=(EqualsExpn) expn;
            generate_expression(equals_expn.getLeft());
            generate_expression(equals_expn.getRight());
            if (equals_expn.getOpSymbol().equals("=")) {
                Machine.writeMemory(current_msp++, Machine.EQ); //EQ
                return;
            }
            if (equals_expn.getOpSymbol().equals("not=")) {
                Machine.writeMemory(current_msp++, Machine.EQ); //EQ
                push(1);
                Machine.writeMemory(current_msp++, Machine.SUB); //SUB
                Machine.writeMemory(current_msp++, Machine.NEG); //NEG
                return;
            }
        }
        if (expn instanceof CompareExpn) {
            CompareExpn compare_expn=(CompareExpn) expn;
            generate_expression(compare_expn.getLeft());
            generate_expression(compare_expn.getRight());
            if (compare_expn.getOpSymbol().equals("<")) {
                Machine.writeMemory(current_msp++, Machine.LT); //LT
                return;
            }
            if (compare_expn.getOpSymbol().equals("<=")) {
                Machine.writeMemory(current_msp++, Machine.SWAP); //SWAP
                Machine.writeMemory(current_msp++, Machine.LT); //LT
                push(1);
                Machine.writeMemory(current_msp++, Machine.SUB); //SUB
                Machine.writeMemory(current_msp++, Machine.NEG); //NEG
                return;
            }
            if (compare_expn.getOpSymbol().equals(">")) {
                Machine.writeMemory(current_msp++, Machine.SWAP); //SWAP
                Machine.writeMemory(current_msp++, Machine.LT); //LT
                return;
            }
            if (compare_expn.getOpSymbol().equals(">=")) {
                Machine.writeMemory(current_msp++, Machine.LT); //LT
                push(1);
                Machine.writeMemory(current_msp++, Machine.SUB); //SUB
                Machine.writeMemory(current_msp++, Machine.NEG); //NEG
                return;
            }
        }
        if (expn instanceof ParenthExpn) {
            ParenthExpn parent_expn=(ParenthExpn) expn;
            generate_expression(parent_expn.getParenth());
            return;
        }
        if (expn instanceof ConditionalExpn) {
            ConditionalExpn cond_expn=(ConditionalExpn) expn;
            generate_expression(cond_expn.getCondition());
            Machine.writeMemory(current_msp++, Machine.PUSH); //PUSH
            short save_BF_address=current_msp;
            current_msp++;
            Machine.writeMemory(current_msp++, Machine.BF); //BF
       
            generate_expression(cond_expn.getTrueValue());
            Machine.writeMemory(current_msp++, Machine.PUSH); //PUSH
            short save_BR_address=current_msp;
            current_msp++;
            Machine.writeMemory(current_msp++, Machine.BR); //BR
       
            Machine.writeMemory(save_BF_address,current_msp);
            generate_expression(cond_expn.getFalseValue());
       
            Machine.writeMemory(save_BR_address,current_msp);
       
            return;
        }
        if ((expn instanceof IdentExpn) || (expn instanceof SubsExpn)) {
            generate_variable(expn);
        }
        if (expn instanceof FunctionCallExpn) {
            FunctionCallExpn func_expn=(FunctionCallExpn) expn;
            Machine.writeMemory(current_msp++, Machine.PUSH); //PUSH
            short undefined_address=current_msp;
            current_msp++;
       
            Machine.writeMemory(current_msp++, Machine.PUSH); //PUSH
            short after_function_address=current_msp;
            current_msp++;
       
            Machine.writeMemory(current_msp++, Machine.ADDR); //ADDR
            //LL
            //ON
       
            Machine.writeMemory(current_msp++, Machine.PUSHMT); //PUSHMT
            Machine.writeMemory(current_msp++, Machine.SETD); //SETD
            //LL
       
            ASTList<Expn> arg_list = func_expn.getArguments();
            LinkedList<Expn> arg_ll = arg_list.get_list();
            int i;
            for (i = 0; i < arg_ll.size(); i++) {
                Expn arg_expn = arg_ll.get(i);
                generate_expression(arg_expn);
            }
       
            Machine.writeMemory(current_msp++, Machine.PUSH); //PUSH
//             Machine.writeMemory(current_msp++,?);//function address
            Machine.writeMemory(current_msp++, Machine.BR); //BR
       
            Machine.writeMemory(after_function_address,current_msp);
       
        }
    }
	
    // generate the address of the variable
    private void addr_variable(Expn expn) throws MemoryAddressException {
	if (expn instanceof IdentExpn) {
	    IdentExpn ident_expn=(IdentExpn) expn;
	    Symbol symbol = symbolTable.find_variable(ident_expn.toString());
	    
	    addr(symbol.getll(), symbol.geton());

	}

	if (expn instanceof SubsExpn) { // array 
	    SubsExpn sub_expn=(SubsExpn) expn;
	    Symbol symbol = symbolTable.find_variable(sub_expn.getVariable());
	    
	    Expn sub_expn1 = sub_expn.getSubscript1();
	    Expn sub_expn2 = sub_expn.getSubscript2();
	    
	    ArrayDeclPart adp = (ArrayDeclPart)symbol.getType().getLink();
	    
	    addr(symbol.getll(), symbol.geton());
	    
	    generate_expression(sub_expn1);
	    
	    int lb1 = adp.getLowerBoundary1();
	    push(lb1);
	    Machine.writeMemory(current_msp++, Machine.SUB); //SUB
	    
	    if (sub_expn2 != null) { // 2 dimensional
		int ub2 = adp.getUpperBoundary2();
		int lb2 = adp.getLowerBoundary2();
		
		push(ub2 - lb2 + 1);
		Machine.writeMemory(current_msp++, Machine.MUL); //MUL
		
		generate_expression(sub_expn2);

		push(lb2);
		Machine.writeMemory(current_msp++, Machine.SUB); //SUB
		
		Machine.writeMemory(current_msp++, Machine.ADD); //ADD
		
	    }
	    
	    Machine.writeMemory(current_msp++, Machine.ADD); //ADD
	    
	}

	return;
    
    }
	
    // load the value of the variable
    private void generate_variable(Expn expn) throws MemoryAddressException {
	addr_variable(expn);
	Machine.writeMemory(current_msp++, Machine.LOAD); //LOAD
    }

    
    
}
