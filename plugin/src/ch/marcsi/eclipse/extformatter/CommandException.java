package ch.marcsi.eclipse.extformatter;

public class CommandException extends Exception {
	private static final long serialVersionUID = -6282755884025858129L;

	// Parameterless Constructor
	public CommandException() {
	}

	// Constructor that accepts a message
	public CommandException(String message) {
		super(message);
	}
}