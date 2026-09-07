package com.spinel.pdftools.ui.splitpdf

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertThrows

class PageRangeParserTest {

    @Test
    fun parse_singlePage() {
        assertEquals(listOf(1), PageRangeParser.parse("1", 10))
    }

    @Test
    fun parse_multipleSinglePages() {
        assertEquals(listOf(1, 3, 5), PageRangeParser.parse("1,3,5", 10))
    }

    @Test
    fun parse_simpleRange() {
        assertEquals(listOf(1, 2, 3, 4, 5), PageRangeParser.parse("1-5", 10))
    }

    @Test
    fun parse_complexRange() {
        assertEquals(listOf(1, 2, 3, 7, 10, 11, 12), PageRangeParser.parse("1-3,7,10-12", 20))
    }

    @Test
    fun parse_unsortedInput_returnsSortedUnique() {
        assertEquals(listOf(1, 3, 5), PageRangeParser.parse("5,1,3,1", 10))
    }

    @Test
    fun parse_withWhitespace() {
        assertEquals(listOf(1, 2, 3, 5), PageRangeParser.parse(" 1 - 3 , 5 ", 10))
    }

    @Test
    fun parse_rejectsZero() {
        assertThrows(IllegalArgumentException::class.java) {
            PageRangeParser.parse("0", 10)
        }
    }

    @Test
    fun parse_rejectsNegative() {
        assertThrows(IllegalArgumentException::class.java) {
            PageRangeParser.parse("-1", 10)
        }
    }

    @Test
    fun parse_rejectsMalformedRangeReversed() {
        assertThrows(IllegalArgumentException::class.java) {
            PageRangeParser.parse("5-2", 10)
        }
    }

    @Test
    fun parse_rejectsMalformedRangeDoubleDash() {
        assertThrows(IllegalArgumentException::class.java) {
            PageRangeParser.parse("1--3", 10)
        }
    }

    @Test
    fun parse_rejectsNonNumeric() {
        assertThrows(IllegalArgumentException::class.java) {
            PageRangeParser.parse("abc", 10)
        }
    }

    @Test
    fun parse_rejectsEmptyComma() {
        assertThrows(IllegalArgumentException::class.java) {
            PageRangeParser.parse("1,,3", 10)
        }
    }

    @Test
    fun parse_rejectsExceedingMaxPages() {
        assertThrows(IllegalArgumentException::class.java) {
            PageRangeParser.parse("11", 10)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PageRangeParser.parse("8-12", 10)
        }
    }
}
