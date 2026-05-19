package com.example.health.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.UserProfile
import com.example.domain.usecase.profile.GetCurrentUserProfileUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SosViewModel(
    getProfile: GetCurrentUserProfileUseCase,
) : ViewModel() {

    val profile: StateFlow<UserProfile?> =
        getProfile(Session.USER_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
