package com.example.sj.formattableedittext

import org.apache.commons.lang3.StringUtils
import org.junit.Test

class ExampleUnitTest {
    /*@Test
    fun addition_isCorrect() {
        testDiff("x", "xxx", 1, 2, 0)
        testDiff("jj", "jjj", 1, 2, 0)
        testDiff("xxxxxxxxx", "xxxxxxxxxxxxxx", 3, 5, 0)
        testDiff("ttttt", "tttttttt", 1, 3, 0)
        testDiff("xxx", "x", 1, 0, 2)
        testDiff("f", "fafa", 1, 3, 0)
        testDiff("@Serg", "@@Serg", 0, 1, 0)
        testDiff("@Serg", " @@Serg", 0, 2, 0)
        testDiff("@S", "@S @", 2, 2, 0)
        testDiff("x @Serg", "x @@Serg", 2, 1, 0)
        testDiff("@Sergey Petrov ", "@Sergey Petrov g", 15, 1, 0)
    }

    fun testDiff(s1: String, s2: String, strt: Int, add: Int, rem: Int) {
        println("add \"$s1\" \"$s2\"")
        val diff = StringDifference(s1, s2)
        with(diff) { println("added=${added}($add), removed=${removed}($rem), start=${start}($strt), count=${count}, after=${after}") }
//        assertTrue(strt == diff.start)
//        assertTrue(diff.count >= 0)
//        assertTrue(diff.after >= 0)
//        assertTrue(diff.added == add)
//        assertTrue(diff.removed == rem)
    }

    @Test fun someTest(){

    }

    fun testDiff2(sb:Int, sa:Int, eb:Int, ea:Int){
        val diff = StringDifference("", "", c)
    }*/

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


    fun test(wasS: String, newS: String) {
        val start = StringUtils.indexOfDifference(wasS, newS)
        val count = StringUtils.difference(newS, wasS).length
        val after = StringUtils.difference(wasS, newS).length
        println("start=$start, count=$count, after=$after")
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
