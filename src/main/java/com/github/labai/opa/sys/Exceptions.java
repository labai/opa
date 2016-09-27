package com.github.labai.opa.sys;

import com.github.labai.opa.OpaException;

/**
 * For internal usage only (is not part of api)
 *
 * @author Augustus
 */
class Exceptions {

	/**
	 * Session timeout.
	 */
	static class OpaSessionTimeoutException extends OpaException {
		public OpaSessionTimeoutException() {
			super("Session timeout");
		}
	}

	/**
	 * Data, data type, structure and etc exceptions.
	 */
	static class OpaStructureException extends OpaException {
		private static final long serialVersionUID = 1L;
		public OpaStructureException() {
		}
		public OpaStructureException(String message) {
			super(message);
		}
		public OpaStructureException(Throwable cause) {
			super(cause);
		}
		public OpaStructureException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
