package com.sponsorflow.nexus.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class FeatureFlags(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "feature_flags")
        private val FEATURE_ADVANCED_ANALYTICS = booleanPreferencesKey("feature_advanced_analytics")
        private val FEATURE_ENHANCED_SECURITY = booleanPreferencesKey("feature_enhanced_security")
        private val FEATURE_BETA_UI = booleanPreferencesKey("feature_beta_ui")
    }
    
    val isAdvancedAnalyticsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FEATURE_ADVANCED_ANALYTICS] ?: false
        }
    
    val isEnhancedSecurityEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FEATURE_ENHANCED_SECURITY] ?: true
        }
    
    val isBetaUIEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FEATURE_BETA_UI] ?: false
        }
    
    suspend fun enableAdvancedAnalytics(enable: Boolean) {
        context.dataStore.edit { settings ->
            settings[FEATURE_ADVANCED_ANALYTICS] = enable
        }
    }
    
    suspend fun enableEnhancedSecurity(enable: Boolean) {
        context.dataStore.edit { settings ->
            settings[FEATURE_ENHANCED_SECURITY] = enable
        }
    }
    
    suspend fun enableBetaUI(enable: Boolean) {
        context.dataStore.edit { settings ->
            settings[FEATURE_BETA_UI] = enable
        }
    }
}