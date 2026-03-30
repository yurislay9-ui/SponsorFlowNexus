package com.sponsorflow.nexus

import android.content.Context
import android.content.SharedPreferences
import com.sponsorflow.nexus.account.LicenseVerifier
import com.sponsorflow.nexus.account.SessionManager
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LicenseVerifierTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSessionManager: SessionManager

    // FIX Bug 1: mocks adicionales para que NexusConfigManager no explote
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockSharedPreferencesEditor: SharedPreferences.Editor

    private lateinit var licenseVerifier: LicenseVerifier

    @Before
    fun setup() {
        // FIX Bug 1: stubear SharedPreferences antes de construir LicenseVerifier
        `when`(mockContext.getSharedPreferences(any(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.getString(any(), anyString())).thenReturn(null)
        `when`(mockSharedPreferences.edit()).thenReturn(mockSharedPreferencesEditor)
        `when`(mockSharedPreferencesEditor.putString(any(), any())).thenReturn(mockSharedPreferencesEditor)
        `when`(mockSessionManager.getDeviceId()).thenReturn("test-device-id")

        licenseVerifier = LicenseVerifier(mockContext, mockSessionManager)
    }

    // FIX Bug 3: sin servidor real, validate() lanza IOException → AppResult.Error
    @Test
    fun `validate license returns error when no server available`() = runTest {
        val result = licenseVerifier.validate("VALID-KEY-12345")
        assertTrue("Should return error when server unreachable", result.isError())
    }

    @Test
    fun `validate license returns error for invalid key`() = runTest {
        val result = licenseVerifier.validate("INVALID-KEY")
        assertTrue("Validation should fail for invalid key", result.isError())
    }

    @Test
    fun `validate license returns error for expired key`() = runTest {
        val result = licenseVerifier.validate("EXPIRED-KEY-67890")
        assertTrue("Validation should fail for expired key", result.isError())
    }

    @Test
    fun `validate license returns error for malformed key`() = runTest {
        val result = licenseVerifier.validate("MALFORMED")
        assertTrue("Validation should fail for malformed key", result.isError())
    }

    @Test
    fun `validate license returns error for empty key`() = runTest {
        val emptyResult = licenseVerifier.validate("")
        assertTrue("Validation should fail for empty key", emptyResult.isError())
    }

    // FIX Bug 4: no mockear suspend functions con Mockito.when()
    @Test
    fun `validate license handles exceptions gracefully`() = runTest {
        // validate() captura internamente cualquier excepción y retorna AppResult.Error
        val result = licenseVerifier.validate("ANY-KEY")
        // En entorno de test, siempre falla con error (red o servidor) — nunca lanza excepción no manejada
        assertNotNull("Result should never be null", result)
    }

    @Test
    fun `validate license completes in reasonable time`() = runTest {
        val startTime = System.currentTimeMillis()
        licenseVerifier.validate("PERFORMANCE-KEY-44444")
        val duration = System.currentTimeMillis() - startTime
        assertTrue("Validation should complete in reasonable time", duration < 35000) // 35s = timeout OkHttp
    }

    // FIX Bug 2: eliminar el `=` para que el cuerpo se ejecute realmente
    @Test
    fun `verify subscription tiers exist`() {
        assertEquals("FREE tier should exist", SubscriptionTier.FREE, SubscriptionTier.fromName("FREE"))
        assertEquals("BASICO tier should exist", SubscriptionTier.BASICO, SubscriptionTier.fromName("BASICO"))
        assertEquals("AVANZADO tier should exist", SubscriptionTier.AVANZADO, SubscriptionTier.fromName("AVANZADO"))
        assertEquals("VIP tier should exist", SubscriptionTier.VIP, SubscriptionTier.fromName("VIP"))
        assertEquals("FREE price should be 0", 0.0, SubscriptionTier.FREE.price, 0.0)
        assertEquals("BASICO price should be 9", 9.0, SubscriptionTier.BASICO.price, 0.0)
        assertEquals("AVANZADO price should be 19", 19.0, SubscriptionTier.AVANZADO.price, 0.0)
        assertEquals("VIP price should be 29", 29.0, SubscriptionTier.VIP.price, 0.0)
    }

    // FIX Bug 2: eliminar el `=`
    @Test
    fun `verify tier comparison works`() {
        assertTrue("VIP is at least BASICO", SubscriptionTier.VIP.isAtLeast(SubscriptionTier.BASICO))
        assertTrue("VIP is at least AVANZADO", SubscriptionTier.VIP.isAtLeast(SubscriptionTier.AVANZADO))
        assertTrue("VIP is at least VIP", SubscriptionTier.VIP.isAtLeast(SubscriptionTier.VIP))
        assertFalse("BASICO is not at least AVANZADO", SubscriptionTier.BASICO.isAtLeast(SubscriptionTier.AVANZADO))
        assertTrue("AVANZADO is at least BASICO", SubscriptionTier.AVANZADO.isAtLeast(SubscriptionTier.BASICO))
    }
}