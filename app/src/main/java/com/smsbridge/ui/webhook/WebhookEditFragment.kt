package com.smsbridge.ui.webhook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsbridge.R
import com.smsbridge.data.db.entity.*
import com.smsbridge.databinding.FragmentWebhookEditBinding
import com.smsbridge.databinding.DialogAddFilterBinding
import com.smsbridge.databinding.DialogAddHeaderBinding
import kotlinx.coroutines.launch

class WebhookEditFragment : Fragment() {

    private var _binding: FragmentWebhookEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WebhookEditViewModel by viewModels()
    private var webhookId: Long = -1L
    private lateinit var filterAdapter: FilterRuleAdapter
    private lateinit var headerAdapter: HeaderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWebhookEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webhookId = arguments?.getLong("webhookId", -1L) ?: -1L

        filterAdapter = FilterRuleAdapter { rule -> viewModel.deleteFilter(rule) }
        headerAdapter = HeaderAdapter(
            onDelete = { header -> viewModel.deleteHeader(header) },
            onDecrypt = { encVal -> viewModel.decryptHeader(encVal) }
        )
        binding.rvFilters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = filterAdapter
        }
        binding.rvHeaders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = headerAdapter
        }

        setupFilterOperatorSpinner()

        if (webhookId > 0) {
            viewModel.load(webhookId)
            lifecycleScope.launch {
                viewModel.getWebhook(webhookId)?.let { populateForm(it) }
            }
        }

        viewModel.filters.observe(viewLifecycleOwner) { filterAdapter.submitList(it) }
        viewModel.headers.observe(viewLifecycleOwner) { headerAdapter.submitList(it) }

        // Observe once here — not inside saveWebhook() to avoid stacking observers on each save tap
        viewModel.saved.observe(viewLifecycleOwner) { savedId ->
            if (savedId != null && savedId > 0) {
                // Update local webhookId so filters/headers added after first save work correctly
                webhookId = savedId
                viewModel.load(savedId)
            }
        }

        binding.btnAddFilter.setOnClickListener { showAddFilterDialog() }
        binding.btnAddHeader.setOnClickListener { showAddHeaderDialog() }
        binding.btnSave.setOnClickListener { saveWebhook() }
    }

    private fun setupFilterOperatorSpinner() {
        val options = listOf("AND (match all rules)", "OR (match any rule)")
        binding.spinnerOperator.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun populateForm(webhook: WebhookEntity) {
        binding.etName.setText(webhook.name)
        binding.etUrl.setText(webhook.url)
        binding.etRetryCount.setText(webhook.retryCount.toString())
        binding.etRetryDelay.setText((webhook.retryDelayMs / 1000).toString())
        binding.spinnerOperator.setSelection(if (webhook.filterOperator == "OR") 1 else 0)
    }

    private fun saveWebhook() {
        val name = binding.etName.text.toString().trim()
        val url = binding.etUrl.text.toString().trim()
        if (name.isBlank()) { binding.etName.error = "Required"; return }
        if (url.isBlank()) { binding.etUrl.error = "Required"; return }
        if (!url.startsWith("http")) { binding.etUrl.error = "Must start with http/https"; return }

        val retryCount = binding.etRetryCount.text.toString().toIntOrNull() ?: 3
        val retryDelayMs = (binding.etRetryDelay.text.toString().toLongOrNull() ?: 5L) * 1000L
        val operator = if (binding.spinnerOperator.selectedItemPosition == 1) "OR" else "AND"

        val webhook = WebhookEntity(
            id = if (webhookId > 0) webhookId else 0,
            name = name,
            url = url,
            retryCount = retryCount.coerceIn(1, 10),
            retryDelayMs = retryDelayMs.coerceIn(1000L, 60_000L),
            filterOperator = operator
        )
        viewModel.saveWebhook(webhook)
        Toast.makeText(requireContext(), "Webhook saved", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun showAddFilterDialog() {
        val currentId = if (webhookId > 0) webhookId else {
            Toast.makeText(requireContext(), "Save the webhook first", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogBinding = DialogAddFilterBinding.inflate(layoutInflater)

        val fields = listOf(FilterField.SENDER, FilterField.BODY)
        val matchTypes = listOf(MatchType.CONTAINS, MatchType.EXACT, MatchType.STARTS_WITH, MatchType.ENDS_WITH, MatchType.REGEX)

        dialogBinding.spinnerField.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fields)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        dialogBinding.spinnerMatchType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, matchTypes)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Filter Rule")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val value = dialogBinding.etValue.text.toString().trim()
                if (value.isBlank()) return@setPositiveButton
                viewModel.addFilter(FilterRuleEntity(
                    webhookId = currentId,
                    field = fields[dialogBinding.spinnerField.selectedItemPosition],
                    matchType = matchTypes[dialogBinding.spinnerMatchType.selectedItemPosition],
                    value = value,
                    negate = dialogBinding.cbNegate.isChecked
                ))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddHeaderDialog() {
        val currentId = if (webhookId > 0) webhookId else {
            Toast.makeText(requireContext(), "Save the webhook first", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogBinding = DialogAddHeaderBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Header")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val key = dialogBinding.etKey.text.toString().trim()
                val value = dialogBinding.etValue.text.toString().trim()
                if (key.isBlank() || value.isBlank()) return@setPositiveButton
                viewModel.addHeader(currentId, key, value, dialogBinding.cbSecret.isChecked)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
