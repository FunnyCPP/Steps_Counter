package com.kiienkoromaniuk.stepscounter.ui.steps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.tasks.OnSuccessListener
import com.kiienkoromaniuk.stepscounter.databinding.FragmentStepsBinding
import dagger.hilt.android.AndroidEntryPoint

const val TAG="StepsCounter"

/*enum class FitActionRequestCode {
    SUBSCRIBE,
    READ_DATA
}*/

@AndroidEntryPoint
class StepsFragment : Fragment() {

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .build()

    private val viewModel: StepsViewModel by viewModels()

    private var _binding: FragmentStepsBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStepsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fitSignIn()
        setDataPointListener()
        viewModel.steps.observe(viewLifecycleOwner,{steps->
            binding.steps.text = "Steps: $steps"
        })
    }


    /**
     * Checks that the user is signed in, and if so, executes the specified function. If the user is
     * not signed in, initiates the sign in flow, specifying the post-sign in function to execute.
     *
     */
    private fun fitSignIn() {
        if (oAuthPermissionsApproved()) {
            getSteps()
        } /*else {
            requestCode.let {
                GoogleSignIn.requestPermissions(
                    this,
                    requestCode.ordinal,
                    getGoogleAccount(), fitnessOptions)
            }
        }*/
    }

    /**
     * Handles the callback from the OAuth sign in flow, executing the post sign in function
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                getSteps()
            }
            else -> oAuthErrorMsg(requestCode, resultCode)
        }
    }

    private fun getSteps() {
        viewModel.subscribe()
    }

    private fun setDataPointListener(){
        viewModel.setDataPointListener()
    }

    private fun oAuthErrorMsg(requestCode: Int, resultCode: Int) {
        val message = """
            There was an error signing into Fit. Check the troubleshooting section of the README
            for potential issues.
            Request code was: $requestCode
            Result code was: $resultCode
        """.trimIndent()
        Log.e(TAG, message)
    }

    private fun oAuthPermissionsApproved() = GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)
}