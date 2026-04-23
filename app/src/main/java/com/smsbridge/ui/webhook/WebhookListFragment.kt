package com.smsbridge.ui.webhook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsbridge.R
import com.smsbridge.data.db.entity.WebhookEntity
import com.smsbridge.databinding.FragmentWebhookListBinding

class WebhookListFragment : Fragment() {

    private var _binding: FragmentWebhookListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WebhookListViewModel by viewModels()
    private lateinit var adapter: WebhookAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWebhookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = WebhookAdapter(
            onEdit = { webhook ->
                findNavController().navigate(R.id.action_webhookList_to_webhookEdit, bundleOf("webhookId" to webhook.id))
            },
            onToggle = { webhook, enabled ->
                viewModel.toggleEnabled(webhook, enabled)
            },
            onDelete = { webhook -> confirmDelete(webhook) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@WebhookListFragment.adapter
        }
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_webhookList_to_webhookEdit, bundleOf("webhookId" to -1L))
        }
        viewModel.webhooks.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun confirmDelete(webhook: WebhookEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Webhook")
            .setMessage("Delete \"${webhook.name}\"? All its filters and headers will be removed.")
            .setPositiveButton("Delete") { _, _ -> viewModel.delete(webhook) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
