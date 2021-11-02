package opalib.opa.impl;

import opalib.api.OpaException;

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

}
