/*
    test for temp-tables (simple output)
    (typical case)

    add row with some normal values
    add row with null field to ttout
    add row with default values field to ttout

*/

def temp-table tt no-undo
    field charVal2  as char init "Character value2"
    field charVal  as char init "Character value"
    field intVal   as int  init 1234
    field int64Val as int64 init -123456789012
    field decVal   as dec decimals 5 init 1.23
    field dateVal  as date init 02/15/2014
    field logVal   as log  init true
    field tmVal    as datetime init 02/15/2014
    field tmtzVal  as datetime-tz init 02/15/2014
    field rowid    as rowid
    /* TODO
    field raw      as raw
    */
    index charVal charVal
    .

/* def var hTtOut as handle no-undo. */

/*********************************************************************/
def input param pCharVal  as char no-undo.
def output param table-handle hTtOut.
def output param errorCode as char no-undo init ?.
def output param errorMessage as char no-undo init ?.
/*********************************************************************/

hTtOut = temp-table tt:handle.

/* default values */
create tt.

/* nulls */
create tt.
tt.charVal  = ?.
tt.intVal   = ?.
tt.int64Val = ?.
tt.decVal   = ?.
tt.dateVal  = ?.
tt.logVal   = ?.
tt.tmVal    = ?.
tt.tmtzVal  = ?.
tt.rowid    = ?.

/* normal case */
create tt.
tt.charVal  = "string".
tt.intVal   = 1.
tt.int64Val = 1.
tt.decVal   = 1.1.
tt.dateVal  = 01/15/2014.
tt.logVal   = true.
tt.tmVal    = 01/15/2014.
tt.tmtzVal  = 01/15/2014.
tt.rowid    = rowid(tt).

return "this is return-value".

finally:
    delete object hTtOut. /* required to avoid leaks (?) */
end.
