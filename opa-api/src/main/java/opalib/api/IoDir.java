package opalib.api;

/**
 * Procedure's parameter direction
 */
public enum IoDir {
    IN,
    OUT,

    // is it really good style to have input-output parameter..
    // will not be used in opa-pa, left for backward compatibility with opa
    @Deprecated
    INOUT;
}
