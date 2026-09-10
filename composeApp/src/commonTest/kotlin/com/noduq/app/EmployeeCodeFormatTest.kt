package com.noduq.app

import kotlin.test.Test
import kotlin.test.assertEquals

class EmployeeCodeFormatTest {
    @Test
    fun hyphenatesAfterFiveCharacters() {
        assertEquals("ABCDE-FGHIJ", formatEmployeeCode("abcdefghij"))
        assertEquals("ABCDE", formatEmployeeCode("abcde"))
        assertEquals("ABCDE-FG", formatEmployeeCode("abcde-fg extra"))
    }
}
