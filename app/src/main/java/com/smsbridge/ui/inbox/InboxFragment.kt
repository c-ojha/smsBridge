package com.smsbridge.ui.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsbridge.R
import com.smsbridge.databinding.FragmentInboxBinding

class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InboxViewModel by viewModels()
    private lateinit var adapter: ConversationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ConversationAdapter { message ->
            findNavController().navigate(
                R.id.action_inbox_to_conversation,
                bundleOf("address" to message.address)
            )
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InboxFragment.adapter
        }
        binding.fabCompose.setOnClickListener {
            findNavController().navigate(R.id.action_inbox_to_compose)
        }
        viewModel.threads.observe(viewLifecycleOwner) { threads ->
            adapter.submitList(threads)
            binding.emptyState.visibility = if (threads.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
