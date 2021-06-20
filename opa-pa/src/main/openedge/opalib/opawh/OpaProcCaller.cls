/*
    OpaProcCaller.cls
    Augustus, 2021-06-17

    helper class for procedure dynamic call

    example of meta
        i,s;i,s;i,t[s:inp1/i:f2];o,t,out2;o,s,errorCode
*/

block-level on error undo, throw.

using Progress.Json.ObjectModel.JsonArray.
using Progress.Json.ObjectModel.JsonObject.
using Progress.Lang.*.

class opalib.opawh.OpaProcCaller:

    def var ar-char       as char        no-undo extent 50.
    def var ar-longchar   as longchar    no-undo extent 50.
    def var ar-integer    as integer     no-undo extent 50.
    def var ar-int64      as int64       no-undo extent 50.
    def var ar-logical    as logical     no-undo extent 50.
    def var ar-decimal    as decimal     no-undo extent 50.
    def var ar-date       as date        no-undo extent 50.
    def var ar-datetime   as datetime    no-undo extent 50.
    def var ar-datetimetz as datetime-tz no-undo extent 50.
    def var ar-memptr     as memptr      no-undo extent 50.
    def var ar-tableh     as handle      no-undo extent 50.
    def var ar-rowid      as rowid       no-undo extent 50.
    def var ar-recid      as recid       no-undo extent 50.

    constructor public OpaProcCaller():
    end constructor.


 	method public JsonObject callProc (sProc as char, oInputJson as JsonObject, sMeta as char):

        def var hCall as handle no-undo.
        def var oResultJson as JsonObject no-undo.

        create call hCall.
        hCall:call-name = sProc.
        hCall:call-type = procedure-call-type.

        prepareCallInputParams(hCall, oInputJson, sMeta).

        out:
        do:
            inn:
            do on error undo, throw on stop undo, leave inn:
                hCall:invoke.
                leave out.

                catch err AS Progress.lang.Error:
                    message subst("System error: &1", err:getMessage(1)).
                    undo, throw err.
                end catch.
            end.
            if search(sProc) = ? then
                throwAppException(subst("STOP condition (unknown procedure '&1')", sProc)).
            throwAppException("STOP condition, please check AppServer logs").
        end.
        oResultJson = fillOutputJson(sMeta).

        return oResultJson.

        finally:
            delete object hCall no-error.
            cleanupArrays().
        end finally.

    end method.

    method private void cleanupArrays():
        def var i as int no-undo.
        do i = 1 to extent(ar-tableh):
            if ar-tableh[i] = ? then next.
            if valid-handle(ar-tableh[i]) then
                delete object ar-tableh[i] no-error.
            ar-tableh[i] = ?.
        end.
        // we could also cleanup values for all arrays, but is it necessary?
        ar-char       = ?.
        ar-longchar   = ?.
        ar-integer    = ?.
        ar-int64      = ?.
        ar-logical    = ?.
        ar-decimal    = ?.
        ar-date       = ?.
        ar-datetime   = ?.
        ar-datetimetz = ?.
        ar-memptr     = ?.
     // ar-tableh     = ?.
        ar-rowid      = ?.
        ar-recid      = ?.
    end method.


 	method private handle prepareCallInputParams(hCall as handle, oInputJson as JsonObject, sMeta as char):
        def var sType    as char no-undo.
        def var sIo      as char no-undo.
        def var sIoShort as char no-undo.
        def var sParam   as char no-undo.
        def var sName    as char no-undo.
        def var sTpShort as char no-undo.
        def var sTpExt   as char no-undo.
        def var s        as char no-undo.
        def var i        as int  no-undo.

        def var iInpNum as int no-undo init 0.
        def var iOutNum as int no-undo init 0.

        def var arNames as char no-undo extent.

        arNames = oInputJson:getNames().

        hCall:num-parameters = num-entries(sMeta, ";").

        do i = 1 to hCall:num-parameters:
            sParam   = entry(i, sMeta, ";") + ",,".
            sIoShort = entry(1, sParam, ",").
            sTpExt   = entry(2, sParam, ",").
            sTpShort = entry(1, sTpExt, "["). // t[s:inp1/i:f2] -> t
            if sTpShort = "" then
                sTpShort = "s".

            // input/output
            //
            case sIoShort:
            when "i" then do:
                sIo = "input".
                iInpNum = iInpNum + 1.
            end.
            when "o" then do:
                sIo = "output".
                iOutNum = iOutNum + 1.
            end.
            otherwise
                throwAppException(subst("Invalid ioDir code '&1'", sIoShort)).
            end case.

            // Type
            //
            case sTpShort:
            when "s" then do:
                sType = "character".
                if sIo = "input" then
                    ar-char[i] = oInputJson:getCharacter(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-char[i]).
            end.
            when "ss" then do:
                sType = "longchar".
                if sIo = "input" then
                    ar-longchar[i] = oInputJson:getLongchar(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-longchar[i]).
            end.
            when "i" then do:
                sType = "integer".
                if sIo = "input" then
                    ar-integer[i] = oInputJson:getInteger(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-integer[i]).
            end.
            when "ii" then do:
                sType = "int64".
                if sIo = "input" then
                    ar-int64[i] = oInputJson:getInt64(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-int64[i]).
            end.
            when "b" then do:
                sType = "logical".
                if sIo = "input" then
                    ar-logical[i] = oInputJson:getLogical(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-logical[i]).
            end.
            when "d" then do:
                sType = "decimal".
                if sIo = "input" then
                    ar-decimal[i] = oInputJson:getDecimal(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-decimal[i]).
            end.
            when "dt" then do:
                sType = "date".
                if sIo = "input" then
                    ar-date[i] = oInputJson:getDate(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-date[i]).
            end.
            when "tm" then do:
                sType = "datetime".
                if sIo = "input" then
                    ar-datetime[i] = oInputJson:getDatetime(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-datetime[i]).
            end.
            when "tz" then do:
                sType = "datetime-tz".
                if sIo = "input" then
                    ar-datetimetz[i] = oInputJson:getDatetimeTZ(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-datetimetz[i]).
            end.
            when "m" then do:
                sType = "memptr".
                if sIo = "input" then
                    ar-memptr[i] = oInputJson:getMemptr(arNames[iInpNum]). // reads base64
                hCall:set-parameter(i, sType, sIo, ar-memptr[i]).
            end.
            when "ro" then do:
                sType = "rowid".
                if sIo = "input" then
                    ar-rowid[i] = oInputJson:getRowid(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-rowid[i]).
            end.
            when "re" then do:
                sType = "recid".
                if sIo = "input" then
                    ar-recid[i] = oInputJson:getRecid(arNames[iInpNum]).
                hCall:set-parameter(i, sType, sIo, ar-recid[i]).
            end.
            when "t" then do:
                sType = "table-handle".
                if sIo = "input" then do:
                    s = entry(2, sTpExt + "[", "["). // t[s:inp1/i:f2] -> s:inp1/i:f2
                    s = entry(1, s, "]").
                    ar-tableh[i] = prepareInputTableHandle(s).

                    ar-tableh[i]:read-json("JsonArray", oInputJson:GetJsonArray(arNames[iInpNum])).
                end.
                hCall:set-parameter(i, sType, sIo, ar-tableh[i]).
            end.
            otherwise throwAppException(subst("Invalid dataType code '&1'", sTpShort)).
            end case.
        end.

    end method.


    // s:field1/i:field2
 	method private handle prepareInputTableHandle(sMeta as char):
        def var hTable as handle no-undo.
        def var sEntry as char no-undo.
        def var sName  as char no-undo.
        def var sType  as char no-undo.
        def var i      as int  no-undo.

        create temp-table hTable.

        do i = 1 to num-entries(sMeta, "/"):
            sEntry = entry(i, sMeta, "/") + ":".
            sType = getDataType(entry(1, sEntry, ":"), true).
            sName = entry(2, sEntry, ":").
            hTable:add-new-field(sName, sType).
        end.

        hTable:temp-table-prepare(subst("t-opawh-&1", hTable), no).

        return hTable.

        catch err AS Progress.lang.Error:
            delete object hTable no-error.
            undo, throw err.
        end catch.

    end method.


 	method private char getDataType(sShortType as char, bForTT as log):
        case sShortType:
        when "" or when "s" then return "character".
        when "ss" then return (if bForTT then "clob" else "longchar").
        when "i"  then return "integer".
        when "ii" then return "int64".
        when "b"  then return "logical".
        when "d"  then return "decimal".
        when "dt" then return "date".
        when "tm" then return "datetime".
        when "tz" then return "datetime-tz".
        when "m"  then return (if bForTT then "blob" else "memptr").
        when "ro" then return "rowid".
        when "re" then return "recid".
        when "t"  then return "table-handle".
        otherwise throwAppException(subst("Invalid dataType code '&1'", sShortType)).
        end.
    end method.


 	method private JsonObject fillOutputJson(sMeta as char):
        def var sParam   as char no-undo.
        def var sName    as char no-undo.
        def var sTpShort as char no-undo.
        def var i        as int no-undo.
        def var jsRes    as JsonObject no-undo.
        def var ja       as JsonArray no-undo.

        jsRes = new JsonObject().
        do i = 1 to num-entries(sMeta, ";"):
            sParam = entry(i, sMeta, ";") + ",,".
            if entry(1, sParam, ",") <> "o" then next.
            sTpShort = entry(1, entry(2, sParam, ","), "[").
            sName = entry(3, sParam, ",").
            if sName = "" then
                sName = subst("param_&1", i).

            case sTpShort:
            when "" or when "s" then jsRes:add(sName, ar-char[i]).
            when "ss" then jsRes:add(sName, ar-longchar[i]).
            when "i"  then jsRes:add(sName, ar-integer[i]).
            when "ii" then jsRes:add(sName, ar-int64[i]).
            when "b"  then jsRes:add(sName, ar-logical[i]).
            when "d"  then jsRes:add(sName, ar-decimal[i]).
            when "dt" then jsRes:add(sName, ar-date[i]).
            when "tm" then jsRes:add(sName, ar-datetime[i]).
            when "tz" then jsRes:add(sName, ar-datetimetz[i]).
            when "m"  then jsRes:add(sName, ar-memptr[i]).
            when "ro" then jsRes:add(sName, ar-rowid[i]).
            when "re" then jsRes:add(sName, ar-recid[i]).
            when "t"  then do:
                ja = new JsonArray().
                ar-tableh[i]:write-json("JsonArray", ja).
                jsRes:add(sName, ja).
            end.
            otherwise throwAppException(subst("Invalid dataType code '&1'", sTpShort)).
            end case.

        end.

        return jsRes.

    end method.


 	method public char convertMetaForErrorDescription(sMeta as char):
        def var sRes     as char no-undo.
        def var sParam   as char no-undo.
        def var sIoShort as char no-undo.
        def var sTpShort as char no-undo.
        def var i        as int no-undo.

        do i = 1 to num-entries(sMeta, ";"):
            sParam   = entry(i, sMeta, ";") + ",,".
            sIoShort = entry(1, sParam, ",").
            sTpShort = entry(1, entry(2, sParam, ","), "["). // t[s:inp1/i:f2] -> t

            sRes = subst("&1~n&2 &3", sRes, (if sIoShort = "i" then "input" else "output"), getDataType(sTpShort, false)).
        end.
        return trim(sRes).
    end method.


    /**
     * throw Exception
    */
    method private void throwAppException (sErrMsg as char):
		undo, throw new Progress.Lang.AppError(sErrMsg, 99).
    end method.

end class.
