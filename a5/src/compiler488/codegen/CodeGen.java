package compiler488.codegen;

import java.io.*;
import java.util.*;

import compiler488.compiler.Main;
import compiler488.runtime.Machine;
import compiler488.runtime.MemoryAddressException;
import compiler488.runtime.ExecutionException;
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

    /** flag to identify routine type */
    private enum RoutineType { FUNC, PROC }

    /** Global stack to store addresses of branch instructions that need
      * patching. Specifically those for return and result statements. */
    private LinkedList<Short> branchToRoutineEndStack;

    /** Offset of return address from start of activation record. */
    private final short RETURN_ADDR_OFFSET = -3;

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
   public void Initialize(Program programAST, SymbolTable st)
        throws Exception
	{
	/********************************************************/
	/* Initialization code for the code generator GOES HERE */
	/* This procedure is called once before codeGeneration  */      
	/*                                                      */
	/********************************************************/
	global_st = st;
	symbolTable = new SymbolTable();
    branchToRoutineEndStack = new LinkedList<Short>();
	
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
    private void traverse(Scope s, ASTList<ScalarDecl> arg, Object ref,
                          int lexic_level, LinkedList<Short> exit_addr_list)
                          throws Exception {

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
    private void handle_declaration(Declaration decl,
                                    Hashtable<String,Symbol> symboltable,
                                    int lexic_level)
            throws Exception {
	
	if (decl instanceof MultiDeclarations) {
	    ASTList<DeclarationPart> decl_list = ((MultiDeclarations)decl).getElements();
	    LinkedList<DeclarationPart> ll_part=decl_list.get_list();
	    
	    for (DeclarationPart dp : ll_part) {
		handle_part_declaration(dp, symboltable, decl.getType(), lexic_level);
	    }
	    return;
	}
	
	if (decl instanceof RoutineDecl) {

	    RoutineBody rb = ((RoutineDecl)decl).getRoutineBody();
	    Scope routine_scope = rb.getBody();
	    
	    ASTList<ScalarDecl> params = rb.getParameters();
        int numParams = null == params ? 0 : params.size();
        
	    if (routine_scope != null) {    // not a forward decl
	    	
	    	/* Set up branch over routine's instructions
	         * to avoid unwanted execution. */
		    short save_BR_address=(short)(current_msp+1);
		    push(Machine.UNDEFINED);
		    Machine.writeMemory(current_msp++, Machine.BR);
	        
	        // check if forward declared
	        String name = decl.getName();
	        Symbol forward_decl = symbolTable.find_variable(name);
	        if (forward_decl != null) { // find a forward declaration
	        	//System.out.println("find a forward decl");
	        	LinkedList<Short> addr_list = (LinkedList<Short>)forward_decl.getType().getLink();
	        	Iterator<Short> iterator = addr_list.iterator();
                while (iterator.hasNext()) {
                    Machine.writeMemory(iterator.next(), current_msp);
                }
	        }
	
	        // add first for recursive definition
			symbolTable.add_to_symboltable(decl, symboltable, lexic_level,
	                symbolTable.current_order_number_ll[lexic_level], current_msp,
	                numParams, false);
			symbolTable.current_order_number_ll[lexic_level]++;
	
	        int neededWords =
	            global_st.current_order_number_ll[lexic_level + 1];
	
	        // Callee responsible for allocating required space for locals.
	        push(Machine.UNDEFINED);
	        push(neededWords);
	        Machine.writeMemory(current_msp++, Machine.DUPN);
	
	        // Generate internal routine code.
			if (decl.getType() == null) {   // procedure
			    traverse(routine_scope, params, null, lexic_level + 1,null);
			} else {                        // function
			    traverse(routine_scope, params, decl, lexic_level + 1,null);
			}
	
	        // Append routine exit code. Complete return/result branches.
	        generateRoutineEpilogue(numParams, lexic_level + 1, neededWords);
	
			Machine.writeMemory(save_BR_address,current_msp);
			return;
	    } else { // forward decl
	    	symbolTable.add_to_symboltable(decl, symboltable, lexic_level, 
	    			symbolTable.current_order_number_ll[lexic_level], (short)0,
	    			numParams, true);
	    	symbolTable.current_order_number_ll[lexic_level]++;
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
    int paramOffset = 0;
	if (params != null) {
	    LinkedList<ScalarDecl> l = params.get_list();
	    for (ScalarDecl d : l) {
		//symbolTable.add_to_symboltable(d, ht, lexic_level,
         //   symbolTable.current_order_number_ll[lexic_level]);
		symbolTable.add_to_symboltable(d, ht, lexic_level, paramOffset++);
		//symbolTable.current_order_number_ll[lexic_level]++;
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
    
    private void generate_statement(Stmt stmt, LinkedList<Short> exit_addr_list,
                                    int lexic_level)
            throws Exception{

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

            // Store result expression value into return address
            addr((short) lexic_level, RETURN_ADDR_OFFSET);
            generate_expression(((ResultStmt) stmt).getValue());
            Machine.writeMemory(current_msp++, Machine.STORE);

            // Branch to routine exit code
            branchToRoutineEndStack.push((short) (current_msp + 1));
            push(Machine.UNDEFINED);    // To be patched
            Machine.writeMemory(current_msp++, Machine.BR);
       
            return;
        }
        if (stmt instanceof ReturnStmt) {

            /* Branch to routine epilogue.
             * Address patched during handling of RoutineDecl. */
            branchToRoutineEndStack.push((short) (current_msp + 1));
            push(Machine.UNDEFINED);    // To be patched
            Machine.writeMemory(current_msp++, Machine.BR);

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

            ProcedureCallStmt procStmt;
            Symbol procEntry;
            short routineAddr;
            int routineLexLvl, numParams;

            procStmt = (ProcedureCallStmt) stmt;

            procEntry = symbolTable.find_variable(procStmt.getName());
            if (null == procEntry) {
                throw new ExecutionException(
                    "Unknown procedure name during code generation.\n");
            }
            
            LinkedList<Short> addr_list = null;
            if (procEntry.is_forward_decl()) {
            	addr_list = (LinkedList<Short>)procEntry.getType().getLink();
            }

            routineAddr = procEntry.getStartLine();
            numParams = procEntry.getNumParams();

            // Add one since the stored LL is the LL the routine is declared in
            routineLexLvl = (short) (procEntry.getll() + 1);

            // Generate caller code. Branches to routine.
            generateRoutinePrologue(procStmt.getArguments().get_list(),
                                    (short) routineLexLvl,
                                    routineAddr,
                                    numParams,
                                    RoutineType.PROC,
                                    addr_list);
        }
    }
    
    private void generate_expression(Expn expn) throws Exception{
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
        if (expn instanceof BoolExpn) { // and, or
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

            FunctionCallExpn funcExpn;
            Symbol funcEntry;

            funcExpn = (FunctionCallExpn) expn;

            funcEntry = symbolTable.find_variable(funcExpn.getIdent());
            if (null == funcEntry) {
                throw new ExecutionException(
                    "Unknown function name during code generation.\n");
            }
            
            LinkedList<Short> addr_list = null;
            if (funcEntry.is_forward_decl()) {
            	addr_list = (LinkedList<Short>)funcEntry.getType().getLink();
            }

            generateRoutinePrologue(funcExpn.getArguments().get_list(),
                                    (short) (funcEntry.getll() + 1),
                                    funcEntry.getStartLine(),
                                    funcEntry.getNumParams(),
                                    RoutineType.FUNC,
                                    addr_list);
        }
    }
	
    // generate the address of the variable
    private void addr_variable(Expn expn) throws Exception {
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
    private void generate_variable(Expn expn) throws Exception {
	addr_variable(expn);
	Machine.writeMemory(current_msp++, Machine.LOAD); //LOAD
    }

    private void generateRoutinePrologue(LinkedList<Expn> args,
                                         short lexLvl,
                                         short routineAddr,
                                         int numParams,
                                         RoutineType type,
                                         LinkedList<Short> addr_list)
            throws Exception {

        short retAddr, argAddr, savedBrAddr;

        if (type == RoutineType.FUNC) {
            push(Machine.UNDEFINED);              // Return value
        }

        retAddr = (short) (current_msp + 1);      // Store to patch
        push(Machine.UNDEFINED);                  // Return address
        addr(lexLvl, (short) 0);                  // Save display entry

        for (Expn e : args) {
            generate_expression(e);
        }

        // Update display to point to first argument
        Machine.writeMemory(current_msp++, Machine.PUSHMT);
        push(numParams);
        Machine.writeMemory(current_msp++, Machine.SUB);
        Machine.writeMemory(current_msp++, Machine.SETD);
        Machine.writeMemory(current_msp++, lexLvl);

        // forward declaration: save address to list 
        savedBrAddr = (short)(current_msp + 1);
        if (addr_list != null) {
        	addr_list.add(savedBrAddr);
        }
        
        // Unconditional branch to routine instructions
        push(routineAddr);
        Machine.writeMemory(current_msp++, Machine.BR);

        // Fill return address with first instruction after branch
        Machine.writeMemory(retAddr, current_msp);
    }

    private void generateRoutineEpilogue(int numParams,
                                         int lexLvl,
                                         int allocatedSpace)
            throws MemoryAddressException {

        /* Complete all branches from return or result statements.
         * Empties patch stack. */
        while (null != branchToRoutineEndStack.peek()) {
            Machine.writeMemory(branchToRoutineEndStack.pop(),
                                current_msp);
        }

        // Pop activation record to reclaim stack space
        push((short) (numParams + allocatedSpace));
        Machine.writeMemory(current_msp++, Machine.POPN);
        
        // Saved display ref on top of stack. Reset display.
        Machine.writeMemory(current_msp++, Machine.SETD);
        Machine.writeMemory(current_msp++, (short) lexLvl);

        // Return address on top of stack. Branch back to caller.
        Machine.writeMemory(current_msp++, Machine.BR);
    } 

}

