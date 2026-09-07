package com.spinel.pdftools.ui.splitpdf

import java.util.TreeSet

object PageRangeParser {
    /**
     * Parses a custom page range string like "1-3, 5, 8" into a sorted list of unique page numbers.
     * Throws IllegalArgumentException if the input is malformed, contains 0 or negative numbers,
     * or exceeds maxPages.
     */
    fun parse(input: String, maxPages: Int): List<Int> {
        if (input.isBlank()) {
            throw IllegalArgumentException("empty")
        }

        val pages = TreeSet<Int>()
        val parts = input.split(",")

        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.isEmpty()) {
                throw IllegalArgumentException("malformed")
            }

            if (trimmed.contains("-")) {
                val rangeParts = trimmed.split("-")
                if (rangeParts.size != 2) {
                    throw IllegalArgumentException("malformed")
                }

                val startStr = rangeParts[0].trim()
                val endStr = rangeParts[1].trim()

                if (startStr.isEmpty() || endStr.isEmpty()) {
                    throw IllegalArgumentException("malformed")
                }

                val start = startStr.toIntOrNull() ?: throw IllegalArgumentException("malformed")
                val end = endStr.toIntOrNull() ?: throw IllegalArgumentException("malformed")

                if (start <= 0 || end <= 0) {
                    throw IllegalArgumentException("invalid_page")
                }
                if (start > end) {
                    throw IllegalArgumentException("malformed")
                }
                if (end > maxPages) {
                    throw IllegalArgumentException("exceeds_max")
                }

                for (i in start..end) {
                    pages.add(i)
                }
            } else {
                val page = trimmed.toIntOrNull() ?: throw IllegalArgumentException("malformed")
                if (page <= 0) {
                    throw IllegalArgumentException("invalid_page")
                }
                if (page > maxPages) {
                    throw IllegalArgumentException("exceeds_max")
                }
                pages.add(page)
            }
        }

        if (pages.isEmpty()) {
            throw IllegalArgumentException("empty")
        }

        return pages.toList()
    }
}
