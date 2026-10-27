@file:OptIn(ExperimentalTime::class)

package com.labellens.app.data.repository

import com.labellens.app.data.BackgroundExecutor
import com.labellens.app.data.source.preferences.UserPreferences
import com.labellens.app.data.source.preferences.UserPreferences.Keys.KEY_FIRST_TIME_USER
import com.labellens.app.domain.exceptions.UnAuthorizedException
import com.labellens.app.domain.model.User
import com.labellens.app.identity.SessionManager
import com.labellens.app.root.AppConfiguration
import com.labellens.app.util.ApplicationScope
import com.labellens.app.util.logging.AppLogger
import com.mmk.kmpauth.core.KMPAuth
import com.mmk.kmpauth.core.auth.KMPAuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Owns the authenticated user. Exposes [currentUser] as a hot flow that merges the auth state from
 * [KMPAuth] with premium status from [SubscriptionRepository], and signs guests in anonymously on
 * first launch. Emits `Result.failure(UnAuthorizedException)` when signed out.
 *
 * Auth is handled entirely by [KMPAuth] (Firebase backend) — cross-platform, including desktop and web.
 */
class UserRepository(
    private val subscriptionRepository: SubscriptionRepository,
    private val userPreferences: UserPreferences,
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO,
    private val applicationScope: ApplicationScope,
    private val sessionManager: SessionManager? = null,
) {

    init {
        if (AppConfiguration.isAuthEnabled) {
            signInAnonymouslyIfNecessary()
        }
    }

    private val authTrigger = MutableStateFlow(Clock.System.now().toEpochMilliseconds())

    val currentUser: SharedFlow<Result<User>> = if (!AppConfiguration.isAuthEnabled) {
        authTrigger.map {
            val anonymousId = sessionManager?.getAnonymousId()
                ?: userPreferences.getString(UserPreferences.KEY_ANONYMOUS_ID)
                ?: ""
            if (anonymousId.isNotBlank()) {
                subscriptionRepository.login(userId = anonymousId)
            }
            val user = User(
                id = anonymousId,
                isAnonymous = true,
                hasPremiumAccess = subscriptionRepository.hasPremiumAccess(),
            )
            Result.success(user)
        }.shareIn(applicationScope, SharingStarted.Eagerly, 1)
    } else {
        combine(authTrigger, KMPAuth.currentUserFlow) { _, currentUser -> currentUser }
            .map { currentUser ->
                AppLogger.d("Current user is updated: $currentUser")
                if (currentUser == null) {
                    Result.failure(UnAuthorizedException())
                } else {
                    subscriptionRepository.login(userId = currentUser.uid)
                    val user = currentUser.asUser()
                        .copy(hasPremiumAccess = subscriptionRepository.hasPremiumAccess())
                    Result.success(user)
                }
            }.shareIn(applicationScope, SharingStarted.Eagerly, 1)
    }

    fun signInAnonymouslyIfNecessary() = applicationScope.launch {
        if (!AppConfiguration.isAuthEnabled) return@launch
        backgroundExecutor.execute {
            val isFirstTimeUser = userPreferences.getBoolean(KEY_FIRST_TIME_USER, true)
            if (KMPAuth.currentUser() == null && isFirstTimeUser) {
                KMPAuth.signInAnonymously().getOrThrow()
                userPreferences.putBoolean(KEY_FIRST_TIME_USER, false)
                AppLogger.d("Signed in anonymously")
            }
            Result.success(Unit)
        }.onFailure {
            AppLogger.e("signInAnonymouslyIfNecessary exception ${it.message}")
        }
    }

    // This is added because when linking anonymous account with google account, firebase listener is not triggered
    fun onSuccessfulOauthSign() {
        if (!AppConfiguration.isAuthEnabled) return
        applicationScope.launch { authTrigger.emit(Clock.System.now().toEpochMilliseconds()) }
    }

    suspend fun continueAsGuest(): Result<Unit> = backgroundExecutor.execute {
        if (!AppConfiguration.isAuthEnabled) return@execute Result.success(Unit)
        if (KMPAuth.currentUser() == null) {
            KMPAuth.signInAnonymously().getOrThrow()
        }
        Result.success(Unit)
    }

    suspend fun logOut(): Result<Unit> = backgroundExecutor.execute {
        if (!AppConfiguration.isAuthEnabled) return@execute Result.success(Unit)
        subscriptionRepository.logOut()
        KMPAuth.signOut()
        Result.success(Unit)
    }

    suspend fun deleteAccount(): Result<Unit> = backgroundExecutor.execute {
        if (!AppConfiguration.isAuthEnabled) return@execute Result.success(Unit)
        KMPAuth.deleteAccount().getOrThrow()
        logOut()
        Result.success(Unit)
    }

    private fun KMPAuthUser.asUser(): User = User(
        id = uid,
        isAnonymous = isAnonymous,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
    )
}
