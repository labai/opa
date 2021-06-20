package opalib.api;

/**
 * Progress data types
 */
public enum DataType {
    // Pseudo type for annotations - recognize progress type by java type and use default from matched
    AUTO,

    // allowed in params and temp-tables
    CHARACTER,
    INTEGER,
    INT64,
    DECIMAL,
    DATE,
    LOGICAL,
    DATETIME,
    DATETIMETZ,
    ROWID,

    // allowed in temp-table only
    RECID,
    BLOB,
    CLOB,

    // allowed in params only
    LONGCHAR,
    MEMPTR;

}
