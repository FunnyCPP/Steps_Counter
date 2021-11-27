package com.kiienkoromaniuk.stepscounter.ui.steps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import com.kiienkoromaniuk.stepscounter.utils.PermissionStatus
import com.kiienkoromaniuk.stepscounter.utils.requestPermissionLauncher
import dagger.hilt.android.AndroidEntryPoint

const val TAG="StepsCounter"

@AndroidEntryPoint
class StepsFragment : Fragment() {

    private val requestCode = 155

    private val permissionsLauncher by requestPermissionLauncher { status ->
        when(status) {
            PermissionStatus.Granted -> {
                Log.i(TAG,"Permission status: Granted")
                checkPermissionsAndRun()
            }
            PermissionStatus.Denied -> {
                Log.i(TAG,"Permission status: Denied")
                requestRuntimePermissions(PermissionStatus.Denied)
            }
            PermissionStatus.ShowRationale -> {
                Log.i(TAG,"Permission status: ShowRationale")
                requestRuntimePermissions(PermissionStatus.ShowRationale)
            }
        }
    }

    //Setting fitness options for Google Fit API
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .build()


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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkPermissionsAndRun()

        viewModel.steps.observe(viewLifecycleOwner,{steps->
            binding.steps.text = "$steps / ${getTargetSteps(steps)} "+ requireContext().getString(R.string.steps)
            binding.progressBar.max = getTargetSteps(steps)
            binding.progressBar.progress = steps
        })
    }

    private fun getTargetSteps(steps: Int): Int{
            var target = 10000
            while(steps > target)
                target+=5000
        return target

    }
    private fun checkPermissionsAndRun(){
        if (permissionApproved()) {
            fitSignIn()
        } else {
            requestRuntimePermissions(null)
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
     * Handles the callback from the OAuth sign in flow, executing the post sign in function
     * TODO replace this function to result launcher
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                Log.i(TAG, "QAuth sign in: RESULT_OK")
                getSteps()
            }
            else -> {
                oAuthErrorMsg(resultCode)
                showSnackBarError()
            }
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
    //QAuth error
    private fun showSnackBarError(){
        Snackbar.make(
            binding.root,
            R.string.qauth_error,
            Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok) {
                fitSignIn()
            }
            .show()
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
        var approved = true
        for(i in getPermissionsArray())
        {
            if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                    requireContext(), i))
                        approved =false
        }
        return approved
    }
    private fun getPermissionsArray(): Array<String>
    {
           return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACTIVITY_RECOGNITION)
           } else {
               arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
           }
    }

    private fun requestRuntimePermissions(permissionStatus: PermissionStatus?) {
        when(permissionStatus)
        {
            null ->{
                var shouldProvideRationale = false
                for (i in getPermissionsArray()) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), i))
                        shouldProvideRationale = true
                }
                if (shouldProvideRationale)
                    PermissionStatus.ShowRationale
                permissionsLauncher.launch(getPermissionsArray())
            }
            PermissionStatus.ShowRationale -> {
                Snackbar.make(
                    binding.root,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        // Request permission
                        permissionsLauncher.launch(getPermissionsArray())
                    }
                    .show()
            }
            PermissionStatus.Denied ->{
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
            PermissionStatus.Granted ->{
               checkPermissionsAndRun()
            }
        }
    }

}