/*
    just wait n seconds
*/

/*********************************************************************/
def input param pWaitSec as dec no-undo.
def output param errorCode as char no-undo init ?.
def output param errorMessage as char no-undo init "".
/*********************************************************************/

if pWaitSec = 0 then do:
    message "0 sec - no wait".
    return.
end.

message "Start to wait for" pWaitSec "seconds".

pause pWaitSec.

message "Finish wait".

errorCode = ?.
errorMessage = "done".
return.
