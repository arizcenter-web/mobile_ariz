package com.tecsup.app_ariz.feature.customer

import com.tecsup.app_ariz.feature.customer.domain.Membership
import com.tecsup.app_ariz.feature.customer.domain.MembershipPackage
import com.tecsup.app_ariz.feature.customer.domain.MembershipStatus
import com.tecsup.app_ariz.feature.customer.domain.MembershipSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MembershipTest {
    private val day = 86_400_000L

    private fun summary(status: MembershipStatus, expiresIn: Long?) = MembershipSummary(
        membership = Membership(status, propertyLimit = 2, expiresAtMillis = expiresIn?.let { 1_000_000L + it }, rejectionReason = null),
        publishedCount = 1,
    )

    @Test
    fun `dias restantes solo cuenta una membresia activa`() {
        assertEquals(10, summary(MembershipStatus.ACTIVE, 10 * day + 5).daysRemaining(1_000_000L))
        assertNull(summary(MembershipStatus.PENDING_PAYMENT, 10 * day).daysRemaining(1_000_000L))
        assertNull(MembershipSummary(null, 0).daysRemaining(1_000_000L))
    }

    @Test
    fun `una membresia vencida no da dias negativos`() {
        assertEquals(0, summary(MembershipStatus.ACTIVE, -3 * day).daysRemaining(1_000_000L))
    }

    @Test
    fun `los paquetes respetan los limites del backend`() {
        MembershipPackage.All.forEach {
            assertTrue(it.propertyLimit in 1..5)
            assertTrue(it.durationDays in 15..30)
            assertTrue(it.price > 0)
        }
        assertEquals(6, MembershipPackage.All.size)
    }
}
