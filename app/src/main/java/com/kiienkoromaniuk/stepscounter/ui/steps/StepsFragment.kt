package com.kiienkoromaniuk.stepscounter.ui.steps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.material.snackbar.Snackbar
import com.kiienkoromaniuk.stepscounter.BuildConfig
import com.kiienkoromaniuk.stepscounter.R
import com.kiienkoromaniuk.stepscounter.databinding.FragmentStepsBinding
import dagger.hilt.android.AndroidEntryPoint

const val TAG="StepsCounter"

@AndroidEntryPoint
class StepsFragment : Fragment() {

    private val requestCode = 1

    //Setting fitness options for Google Fit API
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .build()

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val viewModel: StepsViewModel by viewModels()

    private var _binding: FragmentStepsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkPermissionsAndRun()

        viewModel.steps.observe(viewLifecycleOwner,{steps->
            binding.steps.text = "Steps: $steps"
        })
    }

    private fun checkPermissionsAndRun(){
        if (permissionApproved()) {
            fitSignIn()
        } else {
            requestRuntimePermissions()
        }
    }
    /**
     * Checks that the user is signed in, and if so, executes the specified function. If the user is
     * not signed in, initiates the sign in flow, specifying the post-sign in function to execute.
     */
    private fun fitSignIn() {
        if (oAuthPermissionsApproved()) {
            getSteps()
        } else {
            requestCode.let {
                GoogleSignIn.requestPermissions(
                    this,
                    requestCode,
                    getGoogleAccount(), fitnessOptions)
            }
        }
    }

    /**
     * Sets the result launcher for getting callback from the QAuth sign in
     * TODO fix bug with infinity activity launching
     */
   /* private fun setResultLauncher() {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    getSteps()
                }
                else -> oAuthErrorMsg( result.resultCode)
            }
        }
        val intent = Intent(requireContext(), MainActivity::class.java)
        resultLauncher.launch(intent)
    }*/
    /**
     * Handles the callback from the OAuth sign in flow, executing the post sign in function
     * TODO replace this function to result launcher
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                getSteps()
            }
            else -> oAuthErrorMsg(resultCode)
        }
    }
    //Gets steps from HistoryClient
    private fun getSteps() {
        getStepsFromHistoryClient()
        setDataPointListener()
    }
    private fun getStepsFromHistoryClient() {
        viewModel.subscribe()
    }

    //Gets steps from SensorsClient
    private fun setDataPointListener(){
        viewModel.setDataPointListener()
    }

    private fun oAuthErrorMsg( resultCode: Int) {
        val message = """
            There was an error signing into Fit. Check the troubleshooting section of the README
            for potential issues.
            Result code was: $resultCode
        """.trimIndent()
        Log.e(TAG, message)
    }

    private fun oAuthPermissionsApproved() = GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)

    //------------------------
    //PERMISSIONS-------------
    //------------------------

    private fun permissionApproved(): Boolean {
        val approved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            true
        }
        return approved
    }

    private fun requestRuntimePermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user.
        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.")
                Snackbar.make(
                    binding.root,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACTIVITY_RECOGNITION),
                            requestCode)
                    }
                    .show()
            } else {
                Log.i(TAG, "Requesting permission")
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACTIVITY_RECOGNITION),requestCode)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when {
            grantResults.isEmpty() -> {
                Log.i(TAG, "User interaction was cancelled.")
            }
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
            }
            else -> {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package",
                            BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }


}