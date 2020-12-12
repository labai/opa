/*
    test for enums

*/

def temp-table tt no-undo
    field enumVal as char init ""
    .

def temp-table ttin no-undo
    field enumVal as char init ""
    .


/*********************************************************************/
def input param pEnumIn as char no-undo.
def input param table for ttIn.
def output param pEnumOut1 as char no-undo.
def output param pEnumOut2 as char no-undo.
def output param pEnumOut3 as char no-undo.
def output param pEnumOut4 as char no-undo.
def output param table for tt.
def output param errorCode as char no-undo init ?.
def output param errorMessage as char no-undo init ?.
/*********************************************************************/

/* empty string */
create tt.

/* null */
create tt.
tt.enumVal = ?.

/* normal case */
create tt.
tt.enumVal = "ENUM_1".

find ttin.
create tt.
tt.enumVal = ttin.enumVal.

pEnumOut1 = pEnumIn.
pEnumOut2 = ?.
pEnumOut3 = "".
pEnumOut4 = "ENUM_1".


return "this is return-value".
