package com.example.sj.formattableedittext

class StringDifference(cursorStartBefore: Int,
                       cursorEndBefore: Int,
                       cursorStartAfter: Int,
                       cursorEndAfter: Int
) {
    var start: Int = Math.min(cursorStartBefore, cursorStartAfter)
    var count: Int = cursorEndBefore - start
    var after: Int = cursorEndAfter - start

    var added: Int = Math.max(0, after - count)

    var removed = Math.max(0, count - after)
}