package com.github.labai.opa.sys;

import com.github.labai.opa.OpaException;

/**
 * @author Augustus
 *
 * For internal usage only (is not part of api)
 *
 */
class Exceptions {

	/**
	 * Session timeout.
	 */
	static class OpaSessionTimeoutException extends OpaException {
		OpaSessionTimeoutException() {
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
		OpaStructureException(String message) {
			super(message);
		}
		OpaStructureException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
