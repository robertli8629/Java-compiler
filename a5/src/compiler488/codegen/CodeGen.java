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
    private void traverse(Scope s, ASTList<ScalarDecl> arg, Object ref, int lexic_level){

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
	    }
	}
	
	symbolTable.symbolstack.pop();
	
    }
    
    /** handles declaration */
    private void handle_declaration(Declaration decl, Hashtable<String,Symbol> symboltable, int lexic_level) {
	
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
    private void handle_part_declaration(DeclarationPart dp, Hashtable<String,Symbol> symboltable, Type type, int lexic_level) {
	
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
            if(bool_expn.getOpSymbol().equals("=")){
                Machine.writeMemory(current_msp++, (short)18); //EQ
                return;
            }
            if(bool_expn.getOpSymbol().equals("not=")){
                Machine.writeMemory(current_msp++, (short)18); //EQ
                push(1);
                Machine.writeMemory(current_msp++, (short)15); //SUB
                Machine.writeMemory(current_msp++, (short)13); //NEG
                return;
            }
            if(bool_expn.getOpSymbol().equals("<")){
                Machine.writeMemory(current_msp++, (short)19); //LT
                return;
            }
            if(bool_expn.getOpSymbol().equals("<=")){
                Machine.writeMemory(current_msp++, (short)21); //SWAP
                Machine.writeMemory(current_msp++, (short)19); //LT
                push(1);
                Machine.writeMemory(current_msp++, (short)15); //SUB
                Machine.writeMemory(current_msp++, (short)13); //NEG
                return;
            }
            if(bool_expn.getOpSymbol().equals(">")){
                Machine.writeMemory(current_msp++, (short)21); //SWAP
                Machine.writeMemory(current_msp++, (short)19); //LT
                return;
            }
            if(bool_expn.getOpSymbol().equals(">=")){
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
//             generate_variable(expn);
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
	

}
