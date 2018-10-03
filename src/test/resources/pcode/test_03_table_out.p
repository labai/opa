/*
    test for temp-tables (simple output)
    (typical case)

    add row with some normal values
    add row with null field to ttout
    add row with default values field to ttout

*/

def temp-table tt1 no-undo
    field charVal  as char init "Character value"
    field intVal   as int  init 1234
    field int64Val as int64 init -123456789012
    field decVal   as dec decimals 5 init 1.23
    field dateVal  as date init 02/15/2014
    field logVal   as log  init true
    field tmVal    as datetime init 02/15/2014
    field tmtzVal  as datetime-tz init 02/15/2014
    field rowid    as rowid
    field recid1   as recid
    /* TODO
    field raw      as raw
    */
    index recid1 recid1
    .

/*********************************************************************/
def input param pCharVal  as char no-undo.
def output param table for tt1.
def output param errorCode as char no-undo init ?.
def output param errorMessage as char no-undo init ?.
/*********************************************************************/

/* default values */
create tt1.
tt1.recid1 = 100.

/* nulls */
create tt1.
tt1.charVal  = ?.
tt1.intVal   = ?.
tt1.int64Val = ?.
tt1.decVal   = ?.
tt1.dateVal  = ?.
tt1.logVal   = ?.
tt1.tmVal    = ?.
tt1.tmtzVal  = ?.
tt1.rowid    = ?.
tt1.recid1   = 101.
message "recid(tt1)" recid(tt1).

/* normal case */
create tt1.
tt1.charVal  = "string".
tt1.intVal   = 1.
tt1.int64Val = 1.
tt1.decVal   = 1.1.
tt1.dateVal  = 01/15/2014.
tt1.logVal   = true.
tt1.tmVal    = 01/15/2014.
tt1.tmtzVal  = 01/15/2014.
tt1.rowid    = rowid(tt1).
tt1.recid1   = 102. // recid(tt1)
message "recid(tt1-2)" recid(tt1).

return "this is return-value".
