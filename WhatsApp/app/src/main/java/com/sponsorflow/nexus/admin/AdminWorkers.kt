/*
 * SponsorFlow Nexus - Admin Workers
 */
package com.sponsorflow.nexus.admin

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class HeartbeatWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val manager = AdminControlManager(applicationContext)
        return if (manager.sendHeartbeat()) Result.success() else Result.retry()
    }
}

class NexusCrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {
    
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val manager = AdminControlManager(context)
            kotlinx.coroutines.runBlocking {
                manager.reportError(
                    errorType = throwable::class.java.simpleName,
                    message = throwable.message ?: "Unknown error",
                    stackTrace = throwable.stackTraceToString()
                )
            }
        } catch (e: IOException) {
            Log.e("NexusCrashHandler", "Network error reporting crash", e)
        } catch (e: HttpException) {
            Log.e("NexusCrashHandler", "HTTP error reporting crash", e)
        } catch (e: JSONException) {
            Log.e("NexusCrashHandler", "JSON error reporting crash", e)
        } catch (e: SecurityException) {
            Log.e("NexusCrashHandler", "Security error reporting crash", e)
        } catch (e: Exception) {
            Log.e("NexusCrashHandler", "Unexpected error reporting crash", e)
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }
    
    companion object {
        fun install(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(NexusCrashHandler(context, defaultHandler))
        }
    }
}