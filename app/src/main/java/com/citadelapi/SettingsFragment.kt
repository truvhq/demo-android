package com.citadelapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.citadelapi.product.MainViewModel
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment(), AdapterView.OnItemSelectedListener  {
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        val envs: Spinner = rootView.findViewById(R.id.env)
        ArrayAdapter.createFromResource(
            rootView.context,
            R.array.envLabels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            envs.adapter = adapter
        }

        envs.onItemSelectedListener = this

        val clientId = rootView.findViewById<TextInputEditText>(R.id.client_id)
        clientId.doOnTextChanged { text, start, before, count ->
            viewModel.changeClientId(text.toString())
        }
        clientId.setText(viewModel.settingsUIState.value.clientId)

        val sandboxKey = rootView.findViewById<TextInputEditText>(R.id.sandbox)
        sandboxKey.doOnTextChanged { text, start, before, count ->
            viewModel.changeSandboxKey(text.toString())
        }
        sandboxKey.setText(viewModel.settingsUIState.value.sandbox)

        val devKey = rootView.findViewById<TextInputEditText>(R.id.dev)
        devKey.doOnTextChanged { text, start, before, count ->
            viewModel.changeDevKey(text.toString())
        }
        devKey.setText(viewModel.settingsUIState.value.dev)

        val prodKey = rootView.findViewById<TextInputEditText>(R.id.prod)
        prodKey.doOnTextChanged { text, start, before, count ->
            viewModel.changeProdKey(text.toString())
        }
        prodKey.setText(viewModel.settingsUIState.value.prod)

        envs.setSelection(when (viewModel.settingsUIState.value.env) {
            "dev" -> 1;
            "prod" -> 2;
            else -> 0;
        })

        return rootView
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when (p2) {
            0 ->  viewModel.changeEnv("sandbox")
            1 ->  viewModel.changeEnv("dev")
            2 ->  viewModel.changeEnv("prod")
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}