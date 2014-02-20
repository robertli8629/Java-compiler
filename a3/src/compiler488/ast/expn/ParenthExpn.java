package compiler488.ast.expn;

import compiler488.ast.expn.Expn;

/**
 * Represents a parenthesized expression.
 */  
public class ParenthExpn extends Expn {

    private Expn exp;

    public ParenthExpn(Expn exp) {
        this.exp = exp;
    }

    public String toString() {
        return "( " + exp + " )";
    }
    
    public Expn getParenth(){
        return exp;
    }
}

