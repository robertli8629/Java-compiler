package compiler488.ast.stmt;

import java.io.PrintStream;

import compiler488.ast.ASTList;
import compiler488.ast.Indentable;
import compiler488.ast.decl.Declaration;

/**
 * Represents the declarations and instructions of a scope construct.
 */
public class Scope extends Stmt {
	protected ASTList<Declaration> declarations; // The declarations at the top.

	protected ASTList<Stmt> statements; // The statements to execute.

	public Scope() {
		declarations = new ASTList<Declaration>();
		statements = new ASTList<Stmt>();
	}

    public Scope(ASTList<Stmt> stmts) {
        declarations = new ASTList<Declaration>();
        statements = stmts;
    }

	/**
	 * Print a description of the <b>scope</b> construct.
	 * 
	 * @param out
	 *            Where to print the description.
	 * @param depth
	 *            How much indentation to use while printing.
	 */
	@Override
	public void printOn(PrintStream out, int depth) {
		Indentable.printIndentOnLn(out, depth, "{");
		Indentable.printIndentOnLn(out, depth, "declarations");

		declarations.printOnSeperateLines(out, depth + 1);

		Indentable.printIndentOnLn(out, depth, "statements");

		statements.printOnSeperateLines(out, depth + 1);

		Indentable.printIndentOnLn(out, depth, "}");
	}

	public ASTList<Declaration> getDeclarations() {
		return declarations;
	}

	public ASTList<Stmt> getStatements() {
		return statements;
	}

	public void setDeclarations(ASTList<Declaration> declarations) {
		this.declarations = declarations;
	}

	public void setStatements(ASTList<Stmt> statements) {
		this.statements = statements;
	}

}
