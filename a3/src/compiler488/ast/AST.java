package compiler488.ast;

/**
 * This is a placeholder at the top of the Abstract Syntax Tree hierarchy. It is
 * a convenient place to add common behaviour.
 * @author  Dave Wortman, Marsha Chechik, Danny House
 */
public class AST {

    /* Line and column position information from the scanner. */
    private int line = -2 , col = -2;

    public void setPos(int line, int col) {
        this.line = line;
        this.col = col;
    }

    /* Add 1 for accuracy, since Lexer counts newlines only. */
    public int getLine() {
        return this.line + 1;
    }

    /* Add 1 since Lexer doesn't count first char of matched text. */
    public int getCol() {
        return this.col + 1;
    }
}
