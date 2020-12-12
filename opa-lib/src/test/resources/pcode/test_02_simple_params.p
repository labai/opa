/*
    test for basic type parameters
    for java not interesting:
        handle
        com-handle
        widget-handle
        blob
        clob
        memptr
*/

def input param charVal1  as char no-undo init "This is character value".
def input param intVal1   as int  no-undo init 1234.
def input param int64Val1 as int64 no-undo init -123456789012.
def input param decVal1   as dec decimals 5 no-undo init 22.55.
def input param dateVal1  as date no-undo init 02/15/2014.
def input param logVal1   as log no-undo init true.
def input param tmVal1    as datetime no-undo init 02/15/2014.
def input param tmtzVal1  as datetime-tz no-undo init 02/15/2014.
def input param longchar1 as longchar no-undo init "this is longchar".
def input param rowid1    as rowid no-undo.
/* def input param raw1      as raw no-undo. */

def output param charVal2  as char no-undo init "This is character value".
def output param intVal2   as int  no-undo init 1234.
def output param int64Val2 as int64 no-undo init -123456789012.
def output param decVal2   as dec decimals 5 no-undo init 22.54321.
def output param dateVal2  as date no-undo init 02/15/2014.
def output param logVal2   as log no-undo init true.
def output param tmVal2    as datetime no-undo init 02/15/2014.
def output param tmtzVal2  as datetime-tz no-undo init 02/15/2014.
def output param longchar2 as longchar no-undo init "this is longchar".
def output param rowid2    as rowid no-undo.
/* def output param raw2      as raw no-undo. */

def input-output param charVal3  as char no-undo init "This is character value".
def input-output param intVal3   as int  no-undo init 1234.
def input-output param int64Val3 as int64 no-undo init -123456789012.
def input-output param decVal3   as dec decimals 5 no-undo init 22.55.
def input-output param dateVal3  as date no-undo init 02/15/2014.
def input-output param logVal3   as log no-undo init true.
def input-output param tmVal3    as datetime no-undo init 02/15/2014.
def input-output param tmtzVal3  as datetime-tz no-undo init 02/15/2014.
def input-output param longchar3 as longchar no-undo init "this is longchar".
def input-output param rowid3    as rowid no-undo.
/* def input-output param raw3      as raw no-undo. */


charVal2  = charVal1.
intVal2   = intVal1.
int64Val2 = int64Val1.
decVal2   = decVal1.
dateVal2  = dateVal1.
logVal2   = logVal1.
tmVal2    = tmVal1.
tmtzVal2  = tmtzVal1.
longchar2 = longchar1.

charVal3  = "GOT:" + charVal3.
intVal3   = 1 + intVal3.
int64Val3 = 1 + int64Val3.
decVal3   = 1 + decVal3.
dateVal3  = 1 + dateVal3.
logVal3   = not logVal3.
tmVal3    = add-interval(tmVal3, 1, "days").
tmtzVal3  = add-interval(tmtzVal3, 1, "days").
longchar3 = "GOT:" + longchar3.

/*
rowid3    = 1 + rowid3.
raw3      = 1 + raw3.
*/



return "this is return-value".
