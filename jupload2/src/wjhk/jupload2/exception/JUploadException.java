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
    /**
     * 
     */
    private static final long serialVersionUID = -6386378085666411905L;

    private String location = null;

    /**
     * Creates a new instance with a specified message.
     * 
     * @param message The message to be associated with this instance.
     */
    public JUploadException(String message) {
        super(message);
        StackTraceElement[] trace = super.getStackTrace();
        if (trace.length > 0) {
            StackTraceElement se = trace[0];
            this.location = se.getClassName() + "." + se.getMethodName()
                    + " in " + se.getFileName() + ", line "
                    + se.getLineNumber();
        }
    }

    /**
     * Creates a new instance with a specified original exception.
     * 
     * @param ex The exception that was originally thrown.
     */
    public JUploadException(Throwable ex) {
        super(ex);
        StackTraceElement[] trace = ex.getStackTrace();
        if (trace.length > 0) {
            StackTraceElement se = trace[0];
            this.location = se.getClassName() + "." + se.getMethodName()
                    + " in " + se.getFileName() + ", line "
                    + se.getLineNumber();
        }
    }

    /**
     * Creates a new instance with a specified message and original exception.
     * 
     * @param message The message to be associated with this instance.
     * @param ex The exception that was originally thrown.
     */
    public JUploadException(String message, Throwable ex) {
        super(message, ex);
        StackTraceElement[] trace = ex.getStackTrace();
        if (trace.length > 0) {
            StackTraceElement se = trace[0];
            this.location = se.getClassName() + "." + se.getMethodName()
                    + " in " + se.getFileName() + ", line "
                    + se.getLineNumber();
        }
    }

    /**
     * Retrieves the human readable location of this exception (Class.method,
     * filename, linenumber)
     * 
     * @return The location where this exception was thrown.
     */
    public String getLocation() {
        return (null == this.location) ? "unknown location" : this.location;
    }
}
