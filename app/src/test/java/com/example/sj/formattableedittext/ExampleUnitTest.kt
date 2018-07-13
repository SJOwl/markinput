package com.example.sj.formattableedittext

import junit.framework.Assert.assertTrue
import org.apache.commons.lang3.StringUtils
import org.junit.Test

class ExampleUnitTest {

    @Test fun testDiffs() {
        testDiffs("abcd", "acbed", 1, 3, 4, 4, 2, 3)
        testDiffs("acbed", "abcd", 1, 4, 3, 3, 3, 2)
        testDiffs("abc", "abgc", 2, 2, 3, 3, 0, 1)
        testDiffs("abgc", "abc", 2, 2, 1, 1, 1, 0)
    }

    fun testDiffs(s1: String, s2: String, cursorStartBefore: Int,
                  cursorEndBefore: Int,
                  cursorStartAfter: Int,
                  cursorEndAfter: Int,
                  rem: Int, add: Int) {
        val diff = StringDifference(cursorStartBefore, cursorEndBefore, cursorStartAfter, cursorEndAfter)
        with(diff) {
            //            println("s1=$s1, s2=$s2, start=$start, count=$count, after=$after, added=$added($add), removed=$removed($rem)")
            println("s1=$s1, s2=$s2, removed=$removed($rem), added=$added($add)")
        }
    }

    @Test fun someTest() {

        testNotTrimmed("qwertyuiopasdfghjklzxcvbnm")
        testNotTrimmed("йцукенгшщзхфывапролдячсмить")

        testNotTrimmed("@")
        testNotTrimmed("!")
        testNotTrimmed("?")
        testNotTrimmed("(")
        testNotTrimmed(")")
        testNotTrimmed("[")
        testNotTrimmed("]")
        testNotTrimmed("{")
        testNotTrimmed("}")
        testNotTrimmed("<")
        testNotTrimmed(">")
        testNotTrimmed("\\")

        testTrimmed("`")
        testTrimmed("~")
        testTrimmed("#")
        testTrimmed("№")
        testTrimmed("$")
        testTrimmed("%")
        testTrimmed("^")
        testTrimmed("\"")
        testTrimmed("&")
        testTrimmed("*")
        testTrimmed("\n")
        testTrimmed("\t")
        testTrimmed("\r")
    }

    fun testTrimmed(s: String) = testTrimming(s, "")
    fun testNotTrimmed(s: String) = testTrimming(s, s)


    fun testTrimming(s1: String, s2: String) {
        val regex = Regex("[\\s-`~#№\$%^\"&*,]")
        val res = s1.trim { c: Char -> regex.containsMatchIn(c.toString()) }
        println("\"$res\"")
        assertTrue(res == s2)
    }

    @Test fun addMention() {
        var s = "fa"
        var start = 2
        val res = "${s.substring(0, start)} @${s.substring(start, s.length)}"
        println(res)
    }

    @Test fun whitespacesCheck() {
        checkWhitespaces(" ")
        checkWhitespaces("\t")
        checkWhitespaces("\r")
        checkWhitespaces("\n")
        checkWhitespaces(".")
        checkWhitespaces("*")
        checkWhitespaces("#")
        checkWhitespaces("@")
        checkWhitespaces("!")
        checkWhitespaces("~")
        checkWhitespaces("`")
        checkWhitespaces("'")
        checkWhitespaces("%")
        checkWhitespaces("^")
        checkWhitespaces("&")
        checkWhitespaces("?")
        checkWhitespaces(":")
        checkWhitespaces(";")
        checkWhitespaces(",")
        checkWhitespaces("/")
    }

    fun checkWhitespaces(s: String) {
        println("\"$s\" ${s.contains(Regex("\\s"))}")
    }
}
