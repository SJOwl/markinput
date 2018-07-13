package com.example.sj.formattableedittext

class StringDifference(cursorStartBefore: Int,
                       cursorEndBefore: Int,
                       cursorStartAfter: Int,
                       cursorEndAfter: Int
) {
//    var start: Int = Math.min(cursorStartBefore, cursorStartAfter)
//    var count: Int = cursorEndBefore - start
//    var after: Int = cursorEndAfter - start
//    var added: Int = Math.max(0, after - count)
//    var removed = Math.max(0, count - after)

    var start: Int = Math.min(cursorStartBefore, cursorStartAfter)
    var count: Int = cursorEndBefore - cursorStartBefore
    var after: Int = cursorEndAfter - cursorStartBefore

    var added: Int = if (after < 0) 0 else after
    var removed = if (after < 0) -after else count
}