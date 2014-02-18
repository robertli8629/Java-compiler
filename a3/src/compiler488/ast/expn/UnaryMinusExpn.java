package compiler488.ast.expn;

/**
 * Represents negation of an integer expression
 */
public class UnaryMinusExpn extends UnaryExpn {

	public UnaryMinusExpn(Expn operand, String opSymbol) {
		super(operand, operator);
	}
}
