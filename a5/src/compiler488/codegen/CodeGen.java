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
	
	System.out.println(global_st.current_order_number_ll[0]);
	System.out.println(global_st.current_order_number_ll[1]);
	
	int alloc_size = global_st.current_order_number_ll[0];
	
	Machine.setPC( (short) 0 ) ;		/* where code to be executed begins */
	
	// debug
// 	Machine.writeMemory(current_msp++, (short)26); // TRON
	
	// init main scope
	Machine.writeMemory(current_msp++, (short)5); // PUSHMT
	Machine.writeMemory(current_msp++, (short)6); // SETD
	Machine.writeMemory(current_msp++, (short)0); // 0
	push(Machine.UNDEFINED); // PUSH UNDEFINED
	push(alloc_size); // PUSH main_needed_words
	Machine.writeMemory(current_msp++, (short)10); // DUPN
	
	// start main scope
	traverse((Scope) programAST, null, null, 0);
	
	// exit main scope
	Machine.writeMemory(current_msp++, (short)0); // HALT
	
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
    private void traverse(Scope s, ASTList<ScalarDecl> arg, Object ref, int lexic_level) throws MemoryAddressException {

	Hashtable<String,Symbol> symboltable=new Hashtable<String,Symbol>();

	ASTList<Declaration> AST_dcl=s.getDeclarations();
	LinkedList<Declaration> ll=AST_dcl.get_list();
	
	symbolTable.symbolstack.push(symboltable);
	
	if (arg != null) {
// 	    order_number = add_params(symboltable, arg, lexic_level, order_number);
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
		    
// 		    handle_statement(stmt, ref, lexic_level);
		    LinkedList<Short> ls = new LinkedList<Short>();
		    generate_statement(stmt, ls, lexic_level);
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
	
	// Semantic analysis S54: associate params if any with scope
	if (decl instanceof RoutineDecl) {
	
	    RoutineBody rb = ((RoutineDecl)decl).getRoutineBody();
	    Scope routine_scope = rb.getBody();
	    if (routine_scope != null) { // not a forward decl

		symbolTable.add_to_symboltable(decl, symboltable, lexic_level, symbolTable.current_order_number_ll[lexic_level]); // add first for recursive definition
		symbolTable.current_order_number_ll[lexic_level]++;
		ASTList<ScalarDecl> params = rb.getParameters();
		if (decl.getType() == null) { // procedure
		    traverse(routine_scope, params, null, lexic_level + 1);
		} else { // function
		    traverse(routine_scope, params, decl, lexic_level + 1);
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
    
    private void printHash(Hashtable<String,Symbol> ht) {
	Set<String> keyset = ht.keySet();
	for (String s : keyset) {
	    Symbol sym = ht.get(s);
	    System.out.println(sym.toString());
// 		ps.print(sym.toString() + "\n");
	}
    }
	
	
    private void push(short value) throws MemoryAddressException {
	Machine.writeMemory(current_msp++, (short)4);
        Machine.writeMemory(current_msp++, value);
    }
    
    private void push(int value) throws MemoryAddressException {
	Machine.writeMemory(current_msp++, (short)4);
        Machine.writeMemory(current_msp++, (short)value);
    }
    
    private void addr(short LL, short ON) throws MemoryAddressException {
	Machine.writeMemory(current_msp++,(short)1);
	Machine.writeMemory(current_msp++,LL);
	Machine.writeMemory(current_msp++,ON);
    }
    
    private void generate_statement(Stmt stmt, LinkedList<Short> l, int lexic_level) throws MemoryAddressException{
        if(stmt instanceof Scope){
	    Scope scope = (Scope) stmt; // find the scope from stmtlist
	    traverse(scope, null, null, lexic_level);
	}
        if(stmt instanceof AssignStmt){
            AssignStmt asgn_stmt = (AssignStmt) stmt;
            IdentExpn expn=(IdentExpn)asgn_stmt.getLval();
            Symbol symbol=symbolTable.find_variable(expn.getIdent());
            addr(symbol.getll(),symbol.geton());
            generate_expression(asgn_stmt.getRval());
            Machine.writeMemory(current_msp++,(short)3);//STORE
            return;
        }
        if(stmt instanceof IfStmt){
            IfStmt if_stmt = (IfStmt) stmt;
            generate_expression(if_stmt.getCondition());
            short save_BF_address=(short)(current_msp+1);
            push((short)0);
            Machine.writeMemory(current_msp++,(short)12);//BF
       
            ASTList<Stmt> stmt_list = if_stmt.getWhenTrue();
            LinkedList<Stmt> stmt_ll = stmt_list.get_list();
            int i;
            for(i = 0; i < stmt_ll.size(); i++){
                Stmt list_stmt = stmt_ll.get(i);
                generate_statement(list_stmt, l, lexic_level);
            }
            if(if_stmt.getWhenFalse()!=null){
                short save_BR_address=(short)(current_msp+1);
                push((short)0);
                Machine.writeMemory(current_msp++,(short)11);//BR
               
                Machine.writeMemory(save_BF_address,current_msp);
                ASTList<Stmt> false_list = if_stmt.getWhenFalse();
                LinkedList<Stmt> false_ll = false_list.get_list();
                for(i = 0; i < false_ll.size(); i++){
                    Stmt fasle_stmt = false_ll.get(i);
                    generate_statement(fasle_stmt, l, lexic_level);
                }
               
                Machine.writeMemory(save_BR_address,current_msp);
            }else{
                Machine.writeMemory(save_BF_address,current_msp);
            }
            return;
        }
        if (stmt instanceof LoopingStmt){
            LoopingStmt loop_stmt=(LoopingStmt) stmt;
            LinkedList<Short> exit_list=new LinkedList<Short>();
            if(loop_stmt instanceof RepeatUntilStmt){
                short save_BF_address= current_msp;
                ASTList<Stmt> body_list = loop_stmt.getBody();
                LinkedList<Stmt> body_ll = body_list.get_list();
                int i;
                for(i = 0; i < body_ll.size(); i++){
                    Stmt body_stmt = body_ll.get(i);
                    generate_statement(body_stmt, exit_list, lexic_level);
                }
               
                generate_expression(loop_stmt.getExpn());
                Machine.writeMemory(current_msp++,(short)4);
                Machine.writeMemory(current_msp++,save_BF_address);
                Machine.writeMemory(current_msp++,(short)12);//BF
                Iterator<Short> iterator = exit_list.iterator();
                while(iterator.hasNext()){
                    Machine.writeMemory(iterator.next(),current_msp);
                }
                return;
            }else{
                short save_BR_address=current_msp;
                generate_expression(loop_stmt.getExpn());
                short save_BF_address=(short)(current_msp+1);
                push((short)0);
                Machine.writeMemory(current_msp++,(short)12);//BF
               
                ASTList<Stmt> body_list = loop_stmt.getBody();
                LinkedList<Stmt> body_ll = body_list.get_list();
                int i;
                for(i = 0; i < body_ll.size(); i++){
                    Stmt body_stmt = body_ll.get(i);
                    generate_statement(body_stmt, exit_list, lexic_level);
                }
               
                push(save_BR_address);
                Machine.writeMemory(current_msp++,(short)11);//BR
                Machine.writeMemory(save_BF_address,current_msp);
                Iterator<Short> iterator = exit_list.iterator();
                while(iterator.hasNext()){
                    Machine.writeMemory(iterator.next(),current_msp);
                }
                return;
            }
        }
        if (stmt instanceof ExitStmt){
            ExitStmt exit_stmt = (ExitStmt) stmt;      
            if(exit_stmt.getExpn()==null){
                l.add((short)(current_msp+1));
                push((short)0);
                Machine.writeMemory(current_msp++,(short)11);//BR
                return;
            }else{
                generate_expression(exit_stmt.getExpn());
                push((short)1);
                Machine.writeMemory(current_msp++,(short)15);//SUB
                Machine.writeMemory(current_msp++,(short)13);//NEG
                l.add((short)(current_msp+1));
                push((short)0);
                Machine.writeMemory(current_msp++,(short)12);//BF
            }
        }
        if (stmt instanceof ResultStmt){
            ResultStmt result_stmt = (ResultStmt) stmt;
            generate_expression(result_stmt.getValue());
            Machine.writeMemory(current_msp++,(short)3);
       
//             push();
            Machine.writeMemory(current_msp++,(short)8);//POPN
            Machine.writeMemory(current_msp++,(short)6);//SETD
            Machine.writeMemory(current_msp++,(short)11);//BR
       
            return;
        }
        if (stmt instanceof ReturnStmt){
//             push();
            Machine.writeMemory(current_msp++,(short)8);//POPN
            Machine.writeMemory(current_msp++,(short)6);//SETD
            Machine.writeMemory(current_msp++,(short)11);//BR
            return;
        }
        if(stmt instanceof GetStmt){
            GetStmt get_stmt=(GetStmt) stmt;
            ASTList<Readable> inputs = get_stmt.getInputs();
            LinkedList<Readable> input_ll = inputs.get_list();
            for(Readable input : input_ll){
                if(input instanceof IdentExpn){
                    IdentExpn expn = (IdentExpn) input;
                    Symbol symbol=symbolTable.find_variable(expn.getIdent());
                    addr(symbol.getll(),symbol.geton());
               
               
                } else if (input instanceof SubsExpn) {
                    SubsExpn expn = (SubsExpn) input;
                    Symbol symbol=symbolTable.find_variable(expn.getVariable());
                    addr(symbol.getll(),symbol.geton());
               
                }
                Machine.writeMemory(current_msp++,(short)24); //READI
                Machine.writeMemory(current_msp++,(short)3); //STORE
            }
            return;
        }
        if(stmt instanceof PutStmt){
            PutStmt put_stmt=(PutStmt) stmt;
            ASTList<Printable> outputs = put_stmt.getOutputs();
            LinkedList<Printable> output_ll = outputs.get_list();
            for(Printable output : output_ll){
                if (output instanceof TextConstExpn){
                    TextConstExpn text=(TextConstExpn)output;
                    String string=text.getValue();
                    for (int i=0; i<string.length(); i++){
                        push((short)string.charAt(i));
                        Machine.writeMemory(current_msp++, (short)23);//PRINTC
                    }
                } else if (output instanceof NewlineConstExpn) {
                    push((short)'\n');
                    Machine.writeMemory(current_msp++, (short)23);//PRINTC
                } else if (output instanceof Expn){
                    Expn expn = (Expn) output;
                    generate_expression(expn);
                    Machine.writeMemory(current_msp++, (short)25);//PRINTI
                }
            }
        }
        if(stmt instanceof ProcedureCallStmt){
            ProcedureCallStmt proc_stmt = (ProcedureCallStmt)stmt;
            short save_BR_address=(short)(current_msp+1);
            push((short)0);
            Machine.writeMemory(current_msp++,(short)1);//ADDR
       
            Machine.writeMemory(current_msp++,(short)5);//PUSHMT
            Machine.writeMemory(current_msp++,(short)6);//SETD
       
            ASTList<Expn> arg_list = proc_stmt.getArguments();
            LinkedList<Expn> arg_ll = arg_list.get_list();
            int i;
            for(i = 0; i < arg_ll.size(); i++){
                Expn arg_expn = arg_ll.get(i);
                generate_expression(arg_expn);
            }

            push((short)0);
            Machine.writeMemory(current_msp++,(short)11);
        }
    }
    
    private void handle_output(PutStmt stmt) throws MemoryAddressException {
	ASTList<Printable> outputs = stmt.getOutputs();
	LinkedList<Printable> output_ll = outputs.get_list();
	for(Printable output : output_ll){
	    if ((output instanceof TextConstExpn) || (output instanceof NewlineConstExpn)) {
		continue;
	    } else if (output instanceof Expn){
		Expn expn = (Expn) output;
		generate_expression(expn);
		Machine.writeMemory(current_msp++,(short)25); //PRINTI
	    }
	}
    }
    
    private void generate_expression(Expn expn) throws MemoryAddressException{
        if(expn instanceof IntConstExpn){
            IntConstExpn int_expn=(IntConstExpn)expn;
            push(int_expn.getValue());
            return;
        }
        if(expn instanceof UnaryMinusExpn){
            UnaryMinusExpn unary_expn=(UnaryMinusExpn) expn;
            generate_expression(unary_expn.getOperand());
            Machine.writeMemory(current_msp++, (short)13);
        }
        if(expn instanceof ArithExpn){
            ArithExpn arith_expn=(ArithExpn) expn;
            if(arith_expn.getOpSymbol().equals("+")){
                generate_expression(arith_expn.getLeft());
                generate_expression(arith_expn.getRight());
                Machine.writeMemory(current_msp++, (short)14);
                return;
            }
            if(arith_expn.getOpSymbol().equals("-")){
                generate_expression(arith_expn.getLeft());
                generate_expression(arith_expn.getRight());
                Machine.writeMemory(current_msp++, (short)15);
                return;
            }
            if(arith_expn.getOpSymbol().equals("*")){
                generate_expression(arith_expn.getLeft());
                generate_expression(arith_expn.getRight());
                Machine.writeMemory(current_msp++, (short)16);
                return;
            }
            if(arith_expn.getOpSymbol().equals("/")){
                generate_expression(arith_expn.getLeft());
                generate_expression(arith_expn.getRight());
                Machine.writeMemory(current_msp++, (short)17);
                return;
            }
        }
        if(expn instanceof BoolConstExpn){
            BoolConstExpn bool_expn=(BoolConstExpn) expn;
            if(bool_expn.getValue()){
                push(Machine.MACHINE_TRUE);
                return;
            }else{
                push(Machine.MACHINE_FALSE);
                return;
            }
        }
        if(expn instanceof NotExpn){
            NotExpn not_expn=(NotExpn) expn;
            generate_expression(not_expn.getOperand());
            push(1);
            Machine.writeMemory(current_msp++, (short)15); //SUB
            Machine.writeMemory(current_msp++, (short)13); //NEG
            return;
        }
        if(expn instanceof BoolExpn){
            BoolExpn bool_expn=(BoolExpn) expn;
            generate_expression(bool_expn.getLeft());
            generate_expression(bool_expn.getRight());
            if(bool_expn.getOpSymbol().equals("and")){
                Machine.writeMemory(current_msp++, (short)16); //MUL
                return;
            }
            if(bool_expn.getOpSymbol().equals("or")){
                Machine.writeMemory(current_msp++, (short)20); //OR
                return;
            }
        }
        if(expn instanceof EqualsExpn){
            EqualsExpn equals_expn=(EqualsExpn) expn;
            generate_expression(equals_expn.getLeft());
            generate_expression(equals_expn.getRight());
            if(equals_expn.getOpSymbol().equals("=")){
                Machine.writeMemory(current_msp++, (short)18); //EQ
                return;
            }
            if(equals_expn.getOpSymbol().equals("not=")){
                Machine.writeMemory(current_msp++, (short)18); //EQ
                push(1);
                Machine.writeMemory(current_msp++, (short)15); //SUB
                Machine.writeMemory(current_msp++, (short)13); //NEG
                return;
            }
        }
        if(expn instanceof CompareExpn){
            CompareExpn compare_expn=(CompareExpn) expn;
            generate_expression(compare_expn.getLeft());
            generate_expression(compare_expn.getRight());
            if(compare_expn.getOpSymbol().equals("<")){
                Machine.writeMemory(current_msp++, (short)19); //LT
                return;
            }
            if(compare_expn.getOpSymbol().equals("<=")){
                Machine.writeMemory(current_msp++, (short)21); //SWAP
                Machine.writeMemory(current_msp++, (short)19); //LT
                push(1);
                Machine.writeMemory(current_msp++, (short)15); //SUB
                Machine.writeMemory(current_msp++, (short)13); //NEG
                return;
            }
            if(compare_expn.getOpSymbol().equals(">")){
                Machine.writeMemory(current_msp++, (short)21); //SWAP
                Machine.writeMemory(current_msp++, (short)19); //LT
                return;
            }
            if(compare_expn.getOpSymbol().equals(">=")){
                Machine.writeMemory(current_msp++, (short)19); //LT
                push(1);
                Machine.writeMemory(current_msp++, (short)15); //SUB
                Machine.writeMemory(current_msp++, (short)13); //NEG
                return;
            }
        }
        if(expn instanceof ParenthExpn){
            ParenthExpn parent_expn=(ParenthExpn) expn;
            generate_expression(parent_expn.getParenth());
            return;
        }
        if(expn instanceof ConditionalExpn){
            ConditionalExpn cond_expn=(ConditionalExpn) expn;
            generate_expression(cond_expn.getCondition());
            Machine.writeMemory(current_msp++, (short)4); //PUSH
            short save_BF_address=current_msp;
            current_msp++;
            Machine.writeMemory(current_msp++, (short)12); //BF
       
            generate_expression(cond_expn.getTrueValue());
            Machine.writeMemory(current_msp++, (short)4); //PUSH
            short save_BR_address=current_msp;
            current_msp++;
            Machine.writeMemory(current_msp++, (short)11); //BR
       
            Machine.writeMemory(save_BF_address,current_msp);
            generate_expression(cond_expn.getFalseValue());
       
            Machine.writeMemory(save_BR_address,current_msp);
       
            return;
        }
        if ((expn instanceof IdentExpn) || (expn instanceof SubsExpn)) {
            generate_variable(expn);
        }
        if(expn instanceof FunctionCallExpn){
            FunctionCallExpn func_expn=(FunctionCallExpn) expn;
            Machine.writeMemory(current_msp++, (short)4);
            short undefined_address=current_msp;
            current_msp++;
       
            Machine.writeMemory(current_msp++, (short)4);
            short after_function_address=current_msp;
            current_msp++;
       
            Machine.writeMemory(current_msp++, (short)1); //ADDR
            //LL
            //ON
       
            Machine.writeMemory(current_msp++, (short)5); //PUSHMT
            Machine.writeMemory(current_msp++, (short)6); //SETD
            //LL
       
            ASTList<Expn> arg_list = func_expn.getArguments();
            LinkedList<Expn> arg_ll = arg_list.get_list();
            int i;
            for(i = 0; i < arg_ll.size(); i++){
                Expn arg_expn = arg_ll.get(i);
                generate_expression(arg_expn);
            }
       
            Machine.writeMemory(current_msp++, (short)4); //PUSH
//             Machine.writeMemory(current_msp++,?);//function address
            Machine.writeMemory(current_msp++, (short)11); //BR
       
            Machine.writeMemory(after_function_address,current_msp);
       
        }
    }
	
    private void generate_variable(Expn expn) throws MemoryAddressException {
	if(expn instanceof IdentExpn){
	    IdentExpn ident_expn=(IdentExpn) expn;
	    Symbol symbol = symbolTable.find_variable(ident_expn.toString());
	    
	    addr(symbol.getll(),symbol.geton());
	    Machine.writeMemory(current_msp++, (short)2); //LOAD

	}

	if(expn instanceof SubsExpn){ // array 
	    SubsExpn sub_expn=(SubsExpn) expn;

	    Symbol symbol = symbolTable.find_variable(sub_expn.getVariable());
	    
	}

	return;
    }

    
    
}
