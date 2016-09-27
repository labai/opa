package com.github.labai.opa;

/**
 * @author Augustus
 */
public class OpaException extends Exception {

	public OpaException() {
	}

	public OpaException(String message) {
		super(message);
	}

	public OpaException(Throwable cause) {
		super(cause);
	}

	public OpaException(String message, Throwable cause) {
		super(message, cause);
	}
}
