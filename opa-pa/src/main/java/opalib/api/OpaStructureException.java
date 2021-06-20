package opalib.api;

/**
 * @author Augustus
 * created on 2021.06.12
 */

/**
 * Data, data type, structure and etc exceptions.
 */
public class OpaStructureException extends OpaException {
    private static final long serialVersionUID = 1L;
    public OpaStructureException() {
    }
    public OpaStructureException(String message) {
        super(message);
    }
    public OpaStructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
