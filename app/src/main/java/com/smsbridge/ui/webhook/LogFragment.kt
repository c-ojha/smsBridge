package com.smsbridge.ui.webhook

import android.app.Application
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsbridge.SMSBridgeApp
import com.smsbridge.data.db.entity.DeliveryLogEntity
import com.smsbridge.data.db.entity.DeliveryStatus
import com.smsbridge.data.repository.SmsRepository
import com.smsbridge.databinding.FragmentLogBinding
import com.smsbridge.databinding.ItemDeliveryLogBinding
import com.smsbridge.util.SmsUtils

class LogViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SmsRepository((app as SMSBridgeApp).database.messageDao(), app.database.deliveryLogDao())
    val logs = repo.observeDeliveryLogs().asLiveData()
}

class LogFragment : Fragment() {

    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogViewModel by viewModels()

    // Single class-level adapter — no local shadowing in onViewCreated
    private val logAdapter = LogAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = logAdapter
        }
        viewModel.logs.observe(viewLifecycleOwner) { logs ->
            logAdapter.submitList(logs)
            binding.emptyState.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class LogAdapter : ListAdapter<DeliveryLogEntity, LogAdapter.ViewHolder>(DIFF) {
    inner class ViewHolder(private val b: ItemDeliveryLogBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: DeliveryLogEntity) {
            b.tvWebhook.text = item.webhookName
            b.tvSender.text = item.sender
            b.tvPreview.text = item.bodyPreview
            b.tvTime.text = SmsUtils.formatTimestamp(item.createdAt)
            b.tvAttempts.text = "Attempts: ${item.attempts}"
            b.tvStatus.text = item.status
            val colorAttr = when (item.status) {
                DeliveryStatus.SUCCESS -> com.google.android.material.R.attr.colorPrimary
                DeliveryStatus.FAILED -> com.google.android.material.R.attr.colorError
                else -> com.google.android.material.R.attr.colorSecondary
            }
            val typedValue = TypedValue()
            b.root.context.theme.resolveAttribute(colorAttr, typedValue, true)
            b.tvStatus.setTextColor(typedValue.data)
            b.tvError.text = item.errorMessage ?: ""
            b.tvError.visibility = if (item.errorMessage != null) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemDeliveryLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DeliveryLogEntity>() {
            override fun areItemsTheSame(a: DeliveryLogEntity, b: DeliveryLogEntity) = a.id == b.id
            override fun areContentsTheSame(a: DeliveryLogEntity, b: DeliveryLogEntity) = a == b
        }
    }
}
