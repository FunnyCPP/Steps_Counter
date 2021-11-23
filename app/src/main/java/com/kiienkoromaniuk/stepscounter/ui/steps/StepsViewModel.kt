package com.kiienkoromaniuk.stepscounter.ui.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.tasks.OnSuccessListener
import com.kiienkoromaniuk.stepscounter.model.repository.StepsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StepsViewModel @Inject constructor(
    private val repository: StepsRepository
): ViewModel() {

    fun subscribe(listener: OnSuccessListener<DataSet>){
            repository.subscribe(listener)
    }
}