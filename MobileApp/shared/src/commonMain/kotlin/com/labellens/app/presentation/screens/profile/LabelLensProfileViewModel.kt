package com.labellens.app.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AttentionFlag
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal
import com.labellens.app.domain.model.Market
import com.labellens.app.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LabelLensProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val isSaved: Boolean = false,
    val anonymousConsent: Boolean = false,
    val selectedLanguage: AppLanguage = AppLanguage.DEFAULT,
    val customAllergenInput: String = "",
)

class LabelLensProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabelLensProfileUiState())
    val uiState: StateFlow<LabelLensProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeLanguage()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val p = userProfileRepository.getProfile()
            val consent = userProfileRepository.isConsentGiven()
            val lang = userProfileRepository.getLanguage()
            _uiState.value = _uiState.value.copy(
                userProfile = p,
                anonymousConsent = consent,
                selectedLanguage = lang,
            )
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            userProfileRepository.languageFlow.collect { lang ->
                _uiState.value = _uiState.value.copy(selectedLanguage = lang)
            }
        }
    }

    fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch {
            userProfileRepository.setLanguage(language)
            _uiState.value = _uiState.value.copy(selectedLanguage = language)
        }
    }

    fun selectMarket(market: Market) {
        viewModelScope.launch {
            userProfileRepository.updateMarket(market)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(selectedMarket = market),
                isSaved = true,
            )
        }
    }

    fun toggleImportedFoodMode() {
        val newMode = !_uiState.value.userProfile.importedFoodMode
        viewModelScope.launch {
            userProfileRepository.updateImportedFoodMode(newMode)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(importedFoodMode = newMode),
                isSaved = true,
            )
        }
    }

    fun toggleAiAssist() {
        val newMode = !_uiState.value.userProfile.aiAssistEnabled
        viewModelScope.launch {
            userProfileRepository.updateAiAssistEnabled(newMode)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(aiAssistEnabled = newMode),
                isSaved = true,
            )
        }
    }

    fun updateCustomAllergenInput(text: String) {
        _uiState.value = _uiState.value.copy(customAllergenInput = text)
    }

    fun addCustomAllergen() {
        val trimmed = _uiState.value.customAllergenInput.trim()
        if (trimmed.isBlank()) return
        val current = _uiState.value.userProfile.customAllergens.toMutableList()
        if (!current.any { it.equals(trimmed, ignoreCase = true) }) {
            current.add(trimmed)
            viewModelScope.launch {
                userProfileRepository.updateCustomAllergens(current)
                _uiState.value = _uiState.value.copy(
                    userProfile = _uiState.value.userProfile.copy(customAllergens = current),
                    customAllergenInput = "",
                    isSaved = true,
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(customAllergenInput = "")
        }
    }

    fun removeCustomAllergen(custom: String) {
        val current = _uiState.value.userProfile.customAllergens.filterNot { it.equals(custom, ignoreCase = true) }
        viewModelScope.launch {
            userProfileRepository.updateCustomAllergens(current)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(customAllergens = current),
                isSaved = true,
            )
        }
    }

    fun toggleAttentionFlag(flag: AttentionFlag) {
        val current = _uiState.value.userProfile.enabledAttentionFlags.toMutableSet()
        if (current.contains(flag)) current.remove(flag) else current.add(flag)
        viewModelScope.launch {
            userProfileRepository.updateAttentionFlags(current)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(enabledAttentionFlags = current),
                isSaved = true,
            )
        }
    }

    fun removeException(ingredient: String) {
        viewModelScope.launch {
            userProfileRepository.removeIngredientException(ingredient)
            val updated = _uiState.value.userProfile.ingredientExceptions.filterNot { it.equals(ingredient, ignoreCase = true) }.toSet()
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(ingredientExceptions = updated),
                isSaved = true,
            )
        }
    }

    fun toggleAllergen(allergen: Allergen) {
        val current = _uiState.value.userProfile.allergies.toMutableSet()
        if (current.contains(allergen)) current.remove(allergen) else current.add(allergen)
        viewModelScope.launch {
            userProfileRepository.updateAllergies(current)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(allergies = current),
                isSaved = true,
            )
        }
    }

    fun toggleAvoid(avoid: AvoidPreference) {
        val current = _uiState.value.userProfile.avoidList.toMutableSet()
        if (current.contains(avoid)) current.remove(avoid) else current.add(avoid)
        viewModelScope.launch {
            userProfileRepository.updateAvoidList(current)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(avoidList = current),
                isSaved = true,
            )
        }
    }

    fun toggleGoal(goal: DietaryGoal) {
        val current = _uiState.value.userProfile.goals.toMutableSet()
        if (current.contains(goal)) current.remove(goal) else current.add(goal)
        viewModelScope.launch {
            userProfileRepository.updateGoals(current)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(goals = current),
                isSaved = true,
            )
        }
    }

    fun toggleConsent(enabled: Boolean) {
        viewModelScope.launch {
            userProfileRepository.setConsentGiven(enabled)
            _uiState.value = _uiState.value.copy(anonymousConsent = enabled)
        }
    }
}
