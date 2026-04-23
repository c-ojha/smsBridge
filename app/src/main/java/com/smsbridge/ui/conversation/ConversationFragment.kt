package com.smsbridge.ui.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsbridge.databinding.FragmentConversationBinding
import com.smsbridge.util.SmsUtils

class ConversationFragment : Fragment() {

    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ConversationViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val address = arguments?.getString("address")?.takeIf { it.isNotBlank() } ?: return
        viewModel.setAddress(address)

        val layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        adapter = MessageAdapter()
        binding.recyclerView.apply {
            this.layoutManager = layoutManager
            adapter = this@ConversationFragment.adapter
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                SmsUtils.sendSms(requireContext(), address, text)
                binding.etMessage.text?.clear()
            }
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            if (messages.isNotEmpty()) binding.recyclerView.scrollToPosition(messages.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
