package com.kiienkoromaniuk.stepscounter.ui.steps

import androidx.lifecycle.ViewModel
import com.kiienkoromaniuk.stepscounter.model.repository.StepsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StepsViewModel @Inject constructor(
    private val repository: StepsRepository
): ViewModel() {
    fun getSteps()= repository.getSteps()
}