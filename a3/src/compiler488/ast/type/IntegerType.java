package compiler488.ast.type;

/**
 * Used to declare objects that yield integers.
 */
public class IntegerType extends Type {
	/** Returns the string <b>"Integer"</b>. */
	@Override
	public String toString() {
		return "integer";
	}
	
	@Override
	public boolean equalTo(String type) {
	    if (type.equals("integer")) {
		return true;
	    }
	    return false;
	}
}
