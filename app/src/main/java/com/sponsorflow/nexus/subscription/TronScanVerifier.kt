/*
 * SponsorFlow Nexus v2.4 - TronScan Verifier (USDT TRC20)
 * CORREGIDO: Timeouts, withContext(Dispatchers.IO), tolerancia bilateral
 */
package com.sponsorflow.nexus.subscription

import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit

class TronScanVerifier {

    // CORREGIDO: Timeouts configurados
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()

    // CORREGIDO: withContext(Dispatchers.IO)
    suspend fun verifyPayment(
        txHash: String,
        expectedAmount: Double,
        recipientAddress: String
    ): AppResult<PaymentVerification> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$TRONSCAN_API/transaction-info?hash=$txHash")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                AppResult.Error(AppError.PaymentError("Transacción no encontrada"))
            } else {
                val txInfo = parseTransaction(response.body?.string() ?: "")
                val isValid = validateTransaction(txInfo, expectedAmount, recipientAddress)
                AppResult.Success(isValid)
            }
        } catch (e: Exception) {
            AppResult.Error(AppError.fromException(e))
        }
    }

    private fun parseTransaction(json: String): TransactionInfo {
        val obj = gson.fromJson(json, TronScanResponse::class.java)
        return TransactionInfo(
            hash = obj.hash ?: "",
            from = obj.ownerAddress ?: "",
            to = obj.toAddress ?: "",
            amount = (obj.contractData?.amount ?: 0) / 1_000_000.0,
            token = obj.contractData?.tokenInfo?.symbol ?: "",
            confirmations = obj.confirmations ?: 0,
            timestamp = obj.timestamp ?: 0
        )
    }

    private fun validateTransaction(tx: TransactionInfo, expected: Double, recipient: String): PaymentVerification {
        // Verificar double spend
        if (TxHashRegistry.isUsed(tx.hash)) {
            return PaymentVerification(
                isValid = false,
                amount = tx.amount,
                timestamp = tx.timestamp,
                confirmations = tx.confirmations,
                error = "Transacción ya usada"
            )
        }
        
        val isCorrectRecipient = tx.to.equals(recipient, ignoreCase = true)
        // CORREGIDO: Tolerancia bilateral (0.01 menos, 0.10 más)
        val isCorrectAmount = tx.amount >= (expected - 0.01) && tx.amount <= (expected + 0.10)
        val isUSDT = tx.token == "USDT"
        val isConfirmed = tx.confirmations >= MIN_CONFIRMATIONS
        
        val isValid = isCorrectRecipient && isCorrectAmount && isUSDT && isConfirmed
        
        // Marcar como usado si es válido
        if (isValid) {
            TxHashRegistry.markUsedWithTimestamp(tx.hash)
        }
        
        return PaymentVerification(
            isValid = isValid,
            amount = tx.amount,
            timestamp = tx.timestamp,
            confirmations = tx.confirmations,
            error = when {
                !isCorrectRecipient -> "Wallet incorrecta"
                !isCorrectAmount -> "Monto incorrecto: ${tx.amount} USDT (esperado: $expected)"
                !isUSDT -> "Token incorrecto"
                !isConfirmed -> "Pendiente confirmaciones: ${tx.confirmations}/$MIN_CONFIRMATIONS"
                else -> null
            }
        )
    }

    companion object {
        private const val TRONSCAN_API = "https://apilist.tronscanapi.com/api"
        private const val MIN_CONFIRMATIONS = 20
        const val USDT_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
    }
}

data class PaymentVerification(
    val isValid: Boolean,
    val amount: Double,
    val timestamp: Long,
    val confirmations: Int,
    val error: String? = null
)

private data class TronScanResponse(
    val hash: String? = null,
    val ownerAddress: String? = null,
    val toAddress: String? = null,
    val confirmations: Int? = null,
    val timestamp: Long? = null,
    val contractData: ContractData? = null
)

private data class ContractData(
    val amount: Long? = null,
    val tokenInfo: TokenInfo? = null
)

private data class TokenInfo(
    val symbol: String? = null
)

private data class TransactionInfo(
    val hash: String,
    val from: String,
    val to: String,
    val amount: Double,
    val token: String,
    val confirmations: Int,
    val timestamp: Long
)
