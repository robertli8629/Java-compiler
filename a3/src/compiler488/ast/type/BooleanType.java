package compiler488.ast.type;

/**
 * The type of things that may be true or false.
 */
public class BooleanType extends Type {
	/** Returns the string <b>"boolean"</b>. */
	@Override
	public String toString() {
		return "boolean";
	}
	
	@Override
	public boolean equalTo(String type) {
	    if (type.equals("boolean")) {
		return true;
	    }
	    return false;
	}
	
}
