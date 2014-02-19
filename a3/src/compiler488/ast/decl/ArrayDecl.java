package compiler488.ast.decl;
import compiler488.ast.type.Type;


/**
 *	Represents the declaration of an array variable.	
 */

public class ArrayDecl extends Declaration {

	private ArrayDeclPart arrDeclPart;
	private Type type;

	public ArrayDecl(ArrayDeclPart adp, Type type) {
		this.arrDeclPart = adp;
		this.type = type;
	}


}
