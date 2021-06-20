# OPA-PA - Openedge Procedure Adapter for Pacific App server

----
## ABOUT

OPA-PA is rewritten OPA version for Pacific App server (PAS).

Instead of using OpenEdge java library (o4glrt) and calling PAS sever with APSV mode,
it converts a call params to json format and then calls PAS as regular https POST request.
On PAS side there is special WebHandler, which
reads json, converts into ABL data types and dynamically invokes required procedure.     
Result of procedure is converted to json and is returned back to caller, where 
it is converted back to Java types.


