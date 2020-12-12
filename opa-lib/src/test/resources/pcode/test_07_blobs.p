/*
    test for blobs

*/

def temp-table tto no-undo
field charVal  as char init "Character value"
field blob1    as blob
field clob1    as clob
.
def temp-table ttin no-undo like tto.

/*********************************************************************/
def input param pCharVal  as char no-undo.
def input param pLongCharVal  as longchar no-undo.
def input param mmIn as memptr no-undo.
def input param table for ttin.
def output param table for tto.
def output param pLongCharValOut  as longchar no-undo.
def output param mmOut as memptr no-undo.
/*********************************************************************/

def var ls as longchar no-undo.
def var ls2 as longchar no-undo.

find ttin.

create tto.
buffer-copy ttin to tto.
tto.charVal  =  "aa0".

message "tto.blob1 isnull = " (tto.blob1 = ?) " mmIn is null =" mmIn = ? "mmOut is null =" mmOut = ?.

create tto.
tto.charVal  =  "aa1".

ls = "Blob value".
copy-lob ls to tto.blob1.
ls = "Clob value".
copy-lob ls to tto.clob1.


/* nulls */
create tto.
tto.charVal  = "aa2".

/* copy-lob mmIn to ls2. */
/* ls = "Memptr value from: " + ls2. */
copy-lob mmIn to mmOut.

pLongCharValOut = pLongCharVal + "-out".

return.

finally:
    /* set-size(mmIn) = 0. */
    /* set-size(mmOut) = 0. */
end.