package pnnl.goss.rdf;

/**
 * The InvalidArgumentException exception is a generic exception for specifying
 * that a given argument to a function/class is invalid.
 * 
 * @author Craig Allwardt
 *
 */
public class InvalidArgumentException extends Exception {

	private static final long serialVersionUID = -5379484972083760055L;
	
	public InvalidArgumentException(String message) {
		super(message);	
	}

}
