package com.labellens.app.data.repository

import com.labellens.app.data.source.preferences.UserPreferences
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AttentionFlag
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal
import com.labellens.app.domain.model.Market
import com.labellens.app.domain.model.UserProfile
import com.labellens.app.util.applyPlatformLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface UserProfileRepository {
    val profileFlow: Flow<UserProfile>
    val languageFlow: StateFlow<AppLanguage>
    suspend fun getProfile(): UserProfile
    suspend fun getLanguage(): AppLanguage
    suspend fun setLanguage(language: AppLanguage)
    suspend fun updateAllergies(allergies: Set<Allergen>)
    suspend fun updateAvoidList(avoidList: Set<AvoidPreference>)
    suspend fun updateGoals(goals: Set<DietaryGoal>)
    suspend fun updateMarket(market: Market)
    suspend fun updateImportedFoodMode(imported: Boolean)
    suspend fun updateCustomAllergens(customAllergens: List<String>)
    suspend fun updateAttentionFlags(flags: Set<AttentionFlag>)
    suspend fun addIngredientException(ingredient: String)
    suspend fun removeIngredientException(ingredient: String)
    suspend fun updateProfile(profile: UserProfile)
    suspend fun isConsentGiven(): Boolean
    suspend fun setConsentGiven(consent: Boolean)
}

class UserProfileRepositoryImpl(
    private val preferences: UserPreferences,
) : UserProfileRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _profileFlow = MutableStateFlow(UserProfile())
    override val profileFlow: Flow<UserProfile> = _profileFlow.asStateFlow()

    private val _languageFlow = MutableStateFlow(AppLanguage.DEFAULT)
    override val languageFlow: StateFlow<AppLanguage> = _languageFlow.asStateFlow()

    init {
        // Initial load
        val scope = kotlinx.coroutines.CoroutineScope(com.labellens.app.util.defaultAsyncDispatcher)
        scope.launch {
            _profileFlow.value = loadProfileFromDisk()
            val lang = loadLanguageFromDisk()
            _languageFlow.value = lang
            applyPlatformLanguage(lang)
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

    private suspend fun loadLanguageFromDisk(): AppLanguage {
        val code = preferences.getString(UserPreferences.KEY_APP_LANGUAGE, null)
        return if (code == null) {
            AppLanguage.VIETNAMESE
        } else {
            AppLanguage.fromCode(code)
        }
    }

    override suspend fun getLanguage(): AppLanguage {
        val lang = loadLanguageFromDisk()
        _languageFlow.value = lang
        return lang
    }

    override suspend fun setLanguage(language: AppLanguage) {
        preferences.putString(UserPreferences.KEY_APP_LANGUAGE, language.code)
        _languageFlow.value = language
        applyPlatformLanguage(language)
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

    override suspend fun updateMarket(market: Market) {
        val updated = getProfile().copy(selectedMarket = market)
        persist(updated)
    }

    override suspend fun updateImportedFoodMode(imported: Boolean) {
        val updated = getProfile().copy(importedFoodMode = imported)
        persist(updated)
    }

    override suspend fun updateCustomAllergens(customAllergens: List<String>) {
        val updated = getProfile().copy(customAllergens = customAllergens)
        persist(updated)
    }

    override suspend fun updateAttentionFlags(flags: Set<AttentionFlag>) {
        val updated = getProfile().copy(enabledAttentionFlags = flags)
        persist(updated)
    }

    override suspend fun addIngredientException(ingredient: String) {
        val clean = ingredient.trim().lowercase()
        if (clean.isBlank()) return
        val current = getProfile()
        val updated = current.copy(ingredientExceptions = current.ingredientExceptions + clean)
        persist(updated)
    }

    override suspend fun removeIngredientException(ingredient: String) {
        val clean = ingredient.trim().lowercase()
        val current = getProfile()
        val updated = current.copy(ingredientExceptions = current.ingredientExceptions - clean)
        persist(updated)
    }

    override suspend fun updateProfile(profile: UserProfile) {
        persist(profile)
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
