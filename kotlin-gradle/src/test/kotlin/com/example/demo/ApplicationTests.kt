package com.example.demo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo


class ApplicationTests {

    @Test
    @DisplayName("My 1st JUnit 5 test! 😎")
    fun `my first Junit 5 test`(testInfo: TestInfo) {
        assertEquals(2, 1 + 1, "1 + 1 should equal 2")
        assertEquals("My 1st JUnit 5 test! 😎", testInfo.displayName) { "TestInfo is injected correctly" }
    }
}