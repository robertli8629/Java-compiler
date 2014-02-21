package compiler488.ast;

/**
 * This is a placeholder at the top of the Abstract Syntax Tree hierarchy. It is
 * a convenient place to add common behaviour.
 * @author  Dave Wortman, Marsha Chechik, Danny House
 */
public class AST {

    /* Line and column position information from the scanner. */
    private int line = -1 , col = -1;

    public void setPos(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public int getLine() {
        return this.line;
    }

    public int getCol() {
        return this.col;
    }
}
