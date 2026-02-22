/*
 * SponsorFlow Nexus v2.3 - Payment Manager (USDT TRC20)
 */
package com.sponsorflow.nexus.subscription

import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.entity.SubscriptionEntity
import com.sponsorflow.nexus.data.repositories.SubscriptionRepository
import kotlinx.coroutines.delay
import java.util.UUID

class PaymentManager(
    private val tronScanVerifier: TronScanVerifier,
    private val subscriptionRepo: SubscriptionRepository,
    private val walletAddress: String
) {

    suspend fun initPayment(tier: SubscriptionTier): AppResult<PaymentIntent> = try {
        val amount = tier.price
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
            val result = tronScanVerifier.verifyPayment(
                txHash = intent.pendingTxHash ?: continue,
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

    suspend fun confirmPayment(@Suppress("UNUSED_PARAMETER") verification: PaymentVerification, tier: SubscriptionTier): AppResult<Unit> {
        val id = UUID.randomUUID().toString()
        return subscriptionRepo.activate(id, tier, null, 30)
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