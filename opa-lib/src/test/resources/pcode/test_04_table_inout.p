/*
    test for temp-tables (input, output, input-output etc)

    copy ttin to ttout
    ttinout same except intVal = intVal + 1.
*/


def temp-table ttx no-undo
    field charVal  as char init "Character value"
    field intVal   as int  init 1234
    field int64Val as int64 init -123456789012
    field decVal   as dec decimals 5 init 1.23
    field dateVal  as date
    field logVal   as log  init true
    field tmVal    as datetime init 02/15/2014
    field tmtzVal  as datetime-tz init 02/15/2014
    field rowid    as rowid
    field recid1   as recid
    /* TODO
    field raw      as raw
    */
    index charVal charVal
    .

def temp-table ttin like ttx.
def temp-table ttout like ttx.
def temp-table ttinout like ttx.

/*********************************************************************/
def input param pCharVal  as char no-undo.
def output param table for ttout.
def input-output param table for ttinout.
def input param table for ttin.
def output param errorCode as char no-undo init ?.
def output param errorMessage as char no-undo init ?.
/*********************************************************************/


for each ttin:
    create ttout.
    buffer-copy ttin to ttout.
end.



for each ttinout:
    ttinout.intVal = ttinout.intVal + 1.
end.


return "this is return-value".
