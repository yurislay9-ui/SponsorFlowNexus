/*
 * SponsorFlow Nexus v2.4 - Payment Manager (USDT TRC20)
 * CORREGIDO: Validación de wallet, txHash en confirmPayment, fix pollPayment
 */
package com.sponsorflow.nexus.subscription

import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.entity.SubscriptionEntity
import com.sponsorflow.nexus.data.repositories.SubscriptionRepository
import com.sponsorflow.nexus.rust.RustBridge
import kotlinx.coroutines.delay
import java.util.UUID

class PaymentManager(
    private val tronScanVerifier: TronScanVerifier,
    private val subscriptionRepo: SubscriptionRepository,
    private val walletAddress: String
) {

    init {
        // CORREGIDO: Validar wallet al inicializar
        require(validateWalletAddress(walletAddress)) {
            "Dirección de wallet TRON inválida"
        }
    }

    // Validador de dirección TRON
    private fun validateWalletAddress(address: String): Boolean {
        if (address.isBlank()) return false
        // Longitud típica de dirección TRON (Base58)
        if (address.length < 34 || address.length > 44) return false
        // startsWith T para direcciones TRON
        if (!address.startsWith("T")) return false
        
        // Si está disponible RustBridge, usar validación nativa
        return try {
            if (RustBridge.isAvailable()) {
                RustBridge.validateTronAddress(address)
            } else {
                // Validación básica como fallback
                address.matches(Regex("^T[a-zA-Z0-9]{33}$"))
            }
        } catch (e: Exception) {
            // Fallback a validación básica
            address.matches(Regex("^T[a-zA-Z0-9]{33}$"))
        }
    }

    suspend fun initPayment(tier: SubscriptionTier): AppResult<PaymentIntent> = try {
        val amount = tier.price
        
        // Validar tier
        require(amount > 0) { "Precio inválido para el tier" }
        
        val intent = PaymentIntent(
            id = UUID.randomUUID().toString(),
            walletAddress = walletAddress,
            amount = amount,
            tier = tier,
            qrData = generateQRData(amount),
            createdAt = System.currentTimeMillis()
        )
        AppResult.Success(intent)
    } catch (e: Exception) {
        AppResult.Error(AppError.fromException(e))
    }

    suspend fun pollPayment(intent: PaymentIntent): AppResult<PaymentVerification> {
        val timeout = 30 * 60 * 1000L // 30 minutos
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeout) {
            // CORREGIDO: Verificar txHash antes de continuar
            val txHash = intent.pendingTxHash
            if (txHash.isNullOrBlank()) {
                delay(POLL_INTERVAL)
                continue
            }
            
            val result = tronScanVerifier.verifyPayment(
                txHash = txHash,
                expectedAmount = intent.amount,
                recipientAddress = walletAddress
            )

            result.onSuccess { verification ->
                if (verification.isValid) {
                    return AppResult.Success(verification)
                }
            }

            delay(POLL_INTERVAL)
        }

        return AppResult.Error(AppError.PaymentError("Tiempo de espera agotado"))
    }

    // CORREGIDO: Pasar txHash verificado a activate
    suspend fun confirmPayment(verification: PaymentVerification, tier: SubscriptionTier): AppResult<Unit> {
        val id = UUID.randomUUID().toString()
        
        // CORREGIDO: Usar el txHash de la verificación
        val txHash = verification.txHash
        
        return subscriptionRepo.activate(id, tier, txHash, 30)
    }

    private fun generateQRData(amount: Double): String {
        return "tron:$walletAddress?amount=$amount&token=USDT"
    }

    companion object {
        private const val POLL_INTERVAL = 15000L // 15 segundos
    }
}

data class PaymentIntent(
    val id: String,
    val walletAddress: String,
    val amount: Double,
    val tier: SubscriptionTier,
    val qrData: String,
    val createdAt: Long,
    var pendingTxHash: String? = null
)
