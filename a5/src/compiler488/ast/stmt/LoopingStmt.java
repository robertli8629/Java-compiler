package compiler488.ast.stmt;

import compiler488.ast.expn.Expn;
import compiler488.ast.ASTList;

/**
 * Represents the common parts of loops.
 */
public abstract class LoopingStmt extends Stmt
{
    protected ASTList<Stmt> body ;	  // body of ther loop
    protected Expn expn;          // Loop condition

	public Expn getExpn() {
		return expn;
	}

	public void setExpn(Expn expn) {
		this.expn = expn;
	}

	public ASTList<Stmt> getBody() {
		return body;
	}

	public void setBody(ASTList<Stmt> body) {
		this.body = body;
	}

}
