/*
 * Created on 9 mai 2006
 */
package wjhk.jupload2.exception;

/**
 * A new kind of exceptions. Currently : no other specialization than its name. 
 * 
 * @author Etienne Gauthier
 */
public class JUploadException extends Exception {
	String functionName = null;

	/**
	 * @param arg0
	 */
	public JUploadException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0 The exception that was thrown
	 * @param functionName The name of the function, where the original exception occured.
	 */
	public JUploadException(Throwable arg0, String functionName) {
		super(arg0);
		this.functionName = functionName;
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public JUploadException(String arg0, Throwable arg1, String functionName) {
		super(arg0, arg1);
		this.functionName = functionName;
	}

}
