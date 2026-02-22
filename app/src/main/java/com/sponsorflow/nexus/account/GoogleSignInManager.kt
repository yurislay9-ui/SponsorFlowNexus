/*
 * SponsorFlow Nexus v2.3 - Google Sign-In Manager
 */
package com.sponsorflow.nexus.account

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Tasks
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import kotlinx.coroutines.tasks.await

class GoogleSignInManager(private val context: Context, private val clientId: String) {

    private val signInClient = Identity.getSignInClient(context)

    suspend fun beginSignIn(): AppResult<IntentSender> = try {
        val request = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(clientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
        val result = signInClient.beginSignIn(request).await()
        AppResult.Success(result.pendingIntent.intentSender)
    } catch (e: Exception) {
        AppResult.Error(AppError.fromException(e))
    }

    suspend fun handleResult(data: Intent): AppResult<UserSession> = try {
        val credential = signInClient.getSignInCredentialFromIntent(data)
        val session = UserSession(
            userId = credential.id,
            email = credential.id ?: "",
            displayName = credential.displayName ?: "",
            idToken = credential.googleIdToken ?: ""
        )
        AppResult.Success(session)
    } catch (e: Exception) {
        AppResult.Error(AppError.fromException(e))
    }

    companion object {
        private const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID"
    }
}

data class UserSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val idToken: String
)
