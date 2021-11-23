package com.kiienkoromaniuk.stepscounter.ui.steps

import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.lifecycle.*
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.Field
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
    val steps: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    private var currentSteps = 0
    fun subscribe(){
        val listener = OnSuccessListener<DataSet> { dataSet->
            val total = when {
                dataSet.isEmpty -> 0
                else -> dataSet.dataPoints.first().getValue(Field.FIELD_STEPS).asInt()
            }
            Log.i(TAG, "Total steps: $total")
            currentSteps = total
            steps.postValue(total)
        }
            repository.subscribe(listener)
    }
    fun setDataPointListener() {
        val listener = OnDataPointListener { dataPoint ->
            for (field in dataPoint.dataType.fields) {
                val value = dataPoint.getValue(field)
                Log.i(TAG, "Detected DataPoint field: ${field.name}")
                Log.i(TAG, "Detected DataPoint value: $value")
                currentSteps +=value.asInt()

                steps.postValue(currentSteps + value.asInt())
                Log.i(TAG,"Live Data updated by $value")
            }
        }
        repository.setDataPointListener(listener)
    }
}