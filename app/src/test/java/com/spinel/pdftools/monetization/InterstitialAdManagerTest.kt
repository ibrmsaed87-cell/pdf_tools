package com.spinel.pdftools.monetization

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
class InterstitialAdManagerTest {

    @Before
    fun setup() {
        InterstitialAdManager.resetState()
    }

    private fun getHasPendingOpportunity(): Boolean {
        val field = InterstitialAdManager::class.java.getDeclaredField("hasPendingOpportunity")
        field.isAccessible = true
        return field.getBoolean(InterstitialAdManager)
    }

    private fun consumeOpportunity() {
        val fieldHasPending = InterstitialAdManager::class.java.getDeclaredField("hasPendingOpportunity")
        fieldHasPending.isAccessible = true
        fieldHasPending.setBoolean(InterstitialAdManager, false)
    }

    @Test
    fun testInitialState_NotEligible() {
        assertFalse(getHasPendingOpportunity())
    }

    @Test
    fun testFirstSuccess_Eligible() {
        InterstitialAdManager.recordSuccessfulOperation()
        assertTrue(getHasPendingOpportunity())
    }

    @Test
    fun testSecondSuccessWhilePending_DoesNotStack() {
        InterstitialAdManager.recordSuccessfulOperation() // 1 (pending=true)
        assertTrue(getHasPendingOpportunity())
        
        InterstitialAdManager.recordSuccessfulOperation() // 2 (should ignore, still pending=true)
        assertTrue(getHasPendingOpportunity())
    }

    @Test
    fun testConsumingOpportunity_ResetsCycle() {
        InterstitialAdManager.recordSuccessfulOperation() // 1
        assertTrue(getHasPendingOpportunity())
        
        // Simulate consuming the opportunity (e.g., ad show or skipped because not loaded)
        consumeOpportunity()
        assertFalse(getHasPendingOpportunity())
        
        InterstitialAdManager.recordSuccessfulOperation() // new success creates new opp
        assertTrue(getHasPendingOpportunity())
    }
}
