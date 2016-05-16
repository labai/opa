/*
    hello, <user>

*/

def input param who as char no-undo .
def output param answer as char no-undo.

answer = subst("Hello, &1!", who).

return .
