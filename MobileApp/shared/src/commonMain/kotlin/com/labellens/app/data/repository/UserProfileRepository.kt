package com.labellens.app.data.repository

import com.labellens.app.data.source.preferences.UserPreferences
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal
import com.labellens.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface UserProfileRepository {
    val profileFlow: Flow<UserProfile>
    suspend fun getProfile(): UserProfile
    suspend fun updateAllergies(allergies: Set<Allergen>)
    suspend fun updateAvoidList(avoidList: Set<AvoidPreference>)
    suspend fun updateGoals(goals: Set<DietaryGoal>)
    suspend fun isConsentGiven(): Boolean
    suspend fun setConsentGiven(consent: Boolean)
}

class UserProfileRepositoryImpl(
    private val preferences: UserPreferences,
) : UserProfileRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _profileFlow = MutableStateFlow(UserProfile())
    override val profileFlow: Flow<UserProfile> = _profileFlow.asStateFlow()

    init {
        // Initial load
        val scope = kotlinx.coroutines.CoroutineScope(com.labellens.app.util.defaultAsyncDispatcher)
        scope.launch {
            _profileFlow.value = loadProfileFromDisk()
        }
    }

    private suspend fun loadProfileFromDisk(): UserProfile {
        val jsonStr = preferences.getString("KEY_USER_PROFILE_JSON", null) ?: return UserProfile()
        return try {
            json.decodeFromString<UserProfile>(jsonStr)
        } catch (e: Exception) {
            UserProfile()
        }
    }

    override suspend fun getProfile(): UserProfile {
        val profile = loadProfileFromDisk()
        _profileFlow.value = profile
        return profile
    }

    override suspend fun updateAllergies(allergies: Set<Allergen>) {
        val updated = getProfile().copy(allergies = allergies)
        persist(updated)
    }

    override suspend fun updateAvoidList(avoidList: Set<AvoidPreference>) {
        val updated = getProfile().copy(avoidList = avoidList)
        persist(updated)
    }

    override suspend fun updateGoals(goals: Set<DietaryGoal>) {
        val updated = getProfile().copy(goals = goals)
        persist(updated)
    }

    private suspend fun persist(profile: UserProfile) {
        val str = json.encodeToString(profile)
        preferences.putString("KEY_USER_PROFILE_JSON", str)
        _profileFlow.value = profile
    }

    override suspend fun isConsentGiven(): Boolean = preferences.getBoolean("KEY_ANONYMOUS_CONSENT", false)

    override suspend fun setConsentGiven(consent: Boolean) {
        preferences.putBoolean("KEY_ANONYMOUS_CONSENT", consent)
    }
}
