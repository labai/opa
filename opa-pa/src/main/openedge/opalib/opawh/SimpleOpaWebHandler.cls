/*
    Opa web handler
    Augustus, 2021-06-19
*/

block-level on error undo, throw.

using opalib.opawh.BaseOpaWebHandler.

class opalib.opawh.SimpleOpaWebHandler inherits BaseOpaWebHandler:

    constructor SimpleOpaWebHandler():
        super(os-getenv("OPAW_WH_BASE_URLPATH"), os-getenv("OPAW_WH_BASE_PROPATH")).
    end constructor.

end class.
