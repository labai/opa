/*
    test for blobs

*/

def temp-table tt no-undo
field charVal  as char init "Character value"
field blob1    as blob
field clob1    as clob
.

/*********************************************************************/
def input param pCharVal  as char no-undo.
def input param mmIn as memptr no-undo.
def output param table for tt.
def output param mmOut as memptr no-undo.
/*********************************************************************/

def var ls as longchar no-undo.
def var ls2 as longchar no-undo.

create tt.
tt.charVal  = "aa1".

ls = "Blob value".
copy-lob ls to tt.blob1.
ls = "Clob value".
copy-lob ls to tt.clob1.

/* nulls */
create tt.
tt.charVal  = "aa2".

copy-lob mmIn to ls2.

ls = "Memptr value from: " + ls2.
copy-lob ls to mmOut.


return.

finally:
    /* set-size(mmIn) = 0. */
    /* set-size(mmOut) = 0. */
end.