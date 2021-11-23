package com.kiienkoromaniuk.stepscounter.model.repository

import android.util.Log
import com.google.android.gms.fitness.*
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.tasks.OnSuccessListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StepsRepository @Inject constructor(
    private val recordingClient: RecordingClient,
    private val historyClient: HistoryClient,
    private val sensorsClient: SensorsClient
) {
    private val TAG= "StepsRepository"

     fun subscribe(listener: OnSuccessListener<DataSet>) {
            // To create a subscription, invoke the Recording API. As soon as the subscription is
            // active, fitness data will start recording.
            recordingClient
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG,
                            "Successfully subscribed!"
                        )
                       readData(listener)
                    } else {
                        Log.w(TAG,
                            "There was a problem subscribing.",
                            task.exception
                        )
                    }
                }

            }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
   private fun readData(listener: OnSuccessListener<DataSet>) {
            historyClient
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(listener)
                .addOnFailureListener { e ->
                    Log.w(TAG,
                        "There was a problem getting the step count.",
                        e
                    )
                }

    }
    fun setDataPointListener(listener: OnDataPointListener)
    {
        sensorsClient.add(
            SensorRequest.Builder()
                // data sets.
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setSamplingRate(10, TimeUnit.SECONDS)
                .build(),
            listener
        )
            .addOnSuccessListener {
                Log.i(TAG, "Data Point Listener registered!")
            }
            .addOnFailureListener {
                Log.e(TAG, "Data Point Listener not registered.")
            }
    }
}