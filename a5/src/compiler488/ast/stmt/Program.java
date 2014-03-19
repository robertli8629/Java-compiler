package compiler488.ast.stmt;


/**
 * Placeholder for the scope that is the entire program
 */
public class Program extends Scope {

    public Program(Scope s) {
        declarations = s.getDeclarations();
        statements = s.getStatements();
    }
}
