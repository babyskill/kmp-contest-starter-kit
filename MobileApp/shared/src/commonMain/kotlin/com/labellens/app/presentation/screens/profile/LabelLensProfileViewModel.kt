package com.labellens.app.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal
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
