package com.kiienkoromaniuk.stepscounter.ui.steps

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.kiienkoromaniuk.stepscounter.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StepsFragment : Fragment() {

    private val viewModel: StepsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_steps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getSteps()
    }
}