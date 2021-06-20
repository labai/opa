/*
    Base web handler
    Augustus, 2021-06-19

    for opa-pa

    can be used as base for creating own opaw webhandler

*/

block-level on error undo, throw.

using opalib.opawh.OpaProcCaller.

using OpenEdge.Web.WebResponseWriter.
using OpenEdge.Net.HTTP.StatusCodeEnum.
using OpenEdge.Web.WebHandler.
using OpenEdge.Net.HTTP.HttpHeader.
using Progress.Json.ObjectModel.JsonObject.
using Progress.Lang.*.

class opalib.opawh.BaseOpaWebHandler inherits WebHandler:

    def var oCaller as OpaProcCaller no-undo.
    def var baseUrlPath as char no-undo.
    def var baseProPath as char no-undo.

    constructor BaseOpaWebHandler(sBaseUrlPath as char, sBaseProPath as char):
        baseUrlPath = if sBaseUrlPath = ? then "" else sBaseUrlPath.
        baseProPath = if sBaseProPath = ? then "" else trim(sBaseProPath, "/").
        oCaller = new OpaProcCaller().
    end constructor.

	/* Handler for unsupported methods. The request being serviced and an optional status code is returned. A zero or null value means this method will deal with all errors. */
	method override protected int HandleNotAllowedMethod(oRequest as OpenEdge.Web.IWebRequest):
		/* Throwing an error from this method results in a 500/Internal Server Error response. The web handler will attempt to log this exception. */
		undo, throw new Progress.Lang.AppError("METHOD NOT IMPLEMENTED").
	end method.


	/* Handler for unknown methods. The request being serviced and an optional status code is returned. A zero or null value means this method will deal with all errors. */
	method override protected int HandleNotImplemented(oRequest as OpenEdge.Web.IWebRequest):
		undo, throw new Progress.Lang.AppError("METHOD NOT IMPLEMENTED").
   	end method.

 	method override protected int HandleGet(oRequest as OpenEdge.Web.IWebRequest):
 	    return HandlePost(oRequest).
   	end method.

	/* Default handler for the HTTP POST method. The request being serviced and an optional status code is returned. A zero or null value means this method will deal with all errors. */
 	method override protected int HandlePost(oRequest as OpenEdge.Web.IWebRequest):
        def var oOutputJson as JsonObject no-undo.
        def var oInputJson as JsonObject no-undo.
        def var oHeader as HttpHeader no-undo.

        def var sMeta as char no-undo.
        def var sProc as char no-undo.

        oHeader = oRequest:GetHeader("opa-opp-meta-v1").
        sMeta = oHeader:value.

        sProc = getProcedureName(oRequest:PathInfo).

        // If the content type is 'application/json' then the Entity property will already contain a JsonObject, but otherwise you can use the MessageWriter approach to convert the incoming Entity / body to a more strongly-type object
        if not type-of(oRequest:Entity, Progress.Json.ObjectModel.JsonConstruct) then
            throwAppException("Body must be JSON (use content-type: application/json)").

        oInputJson = cast(oRequest:Entity, JsonObject).

        oOutputJson = oCaller:callProc(sProc, oInputJson, sMeta).

        writeResponseOK(oOutputJson).

        /* A response of 0 means that this handler will build the entire response; a non-zero value is mapped to a static handler in the webapp"s /static/error folder. The mappings are maintained in the webapps"s WEB-INF/web.xml. A predefined set of HTTP status codes is provided in the OpenEdge.Net.HTTP.StatusCodeEnum enumeration */
        return 0.

        catch aer AS Progress.Lang.AppError:
            writeResponseErr(subst("ERR&1", aer:getMessageNum(1)), aer:getMessage(1), "").
            delete object aer no-error.
            return 0.
        end catch.

        catch per AS Progress.Lang.ProError:
            def var sErrStr1 as char no-undo.
            if per:getMessageNum(1) = 3230 then do:
                sErrStr1 = "Got parameters:~n" + oCaller:convertMetaForErrorDescription(sMeta).
            end.
            writeResponseErr("SYSERR", per:getMessage(1), sErrStr1).
            delete object per no-error.
            return 0.
        end catch.

        catch err AS Progress.Lang.Error:
            writeResponseErr("SYSERR", err:getMessage(1), "").
            delete object err no-error.
            return 0.
        end catch.

 	end method.


 	method private void writeResponseOK(oOutputJson as JsonObject):
 	    writeResponse(int(StatusCodeEnum:OK), oOutputJson).
 	end method.


 	method private void writeResponseErr(sErrCode as char, sErrMsg as char, sErrDetails as char):
        def var oOutputJson as JsonObject no-undo.
        def var sBody as longchar no-undo.
        oOutputJson = new JsonObject().
        oOutputJson:add("errorCode", sErrCode).
        oOutputJson:add("errorMessage", sErrMsg).
        if sErrDetails > "" then
            oOutputJson:add("errorDetailsText", sErrDetails).
 	    writeResponse(500, oOutputJson).
 	end method.


 	method private void writeResponse(iStatusCode as int, oOutputJson as JsonObject):
		def var oResponse as OpenEdge.Net.HTTP.IHttpResponse no-undo.
        def var oWriter   as OpenEdge.Web.WebResponseWriter  no-undo.
        def var oBody     as OpenEdge.Core.String            no-undo.

        def var sBody as longchar no-undo.

        oResponse = new OpenEdge.Web.WebResponse().
        oResponse:StatusCode = iStatusCode.
        oOutputJson:write(input-output sBody).
        oBody = new OpenEdge.Core.String(sBody).

        oResponse:Entity        = oBody.
        oResponse:ContentType   = "application/json".
        oResponse:ContentLength = oBody:Size.

        oWriter = new WebResponseWriter(oResponse).
        oWriter:Open().
        oWriter:Close().

    end method.

    /*
     * get procedure name from url path
    */
 	method private char getProcedureName(sPath as char):
        // def var sProcName as char no-undo.
        def var sProcShort as char no-undo.
        def var sAllowedChars as char no-undo init "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-/".

        if not sPath begins baseUrlPath then
            throwAppException(subst("Procedure path (&2) is not allowed (expected to start with '&1')", baseUrlPath, sPath)).
        sPath = trim(substring(sPath, length(baseUrlPath)), "/").

        // sProcName = entry(num-entries(sPath, "/"), sPath, "/").

        // Only allow *.p procedure names
        if not sPath matches "*~.p" then
            throwAppException(subst("Procedure in url should be with .p extension")).

        // Only allow characters [a-z][A-Z][0-9]_-/ in procedure name and path.
        // Relative paths with .. are not allowed
        sProcShort = substring(sPath, 1, length(sPath) - 2). // Remove file extension
        if trim(sProcShort, sAllowedChars) > "" then
            throwAppException(subst("Procedure with not allowed name/path: '&1'", sPath)).

        return baseProPath + "/" + sPath.
 	end method.


    /**
     * throw Exception
    */
    method private void throwAppException (sErrMsg as char):
		undo, throw new Progress.Lang.AppError(sErrMsg, 99).
    end method.

end class.
