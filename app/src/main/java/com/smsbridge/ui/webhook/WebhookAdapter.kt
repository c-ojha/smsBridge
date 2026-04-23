package com.smsbridge.ui.webhook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsbridge.data.db.entity.WebhookEntity
import com.smsbridge.databinding.ItemWebhookBinding

class WebhookAdapter(
    private val onEdit: (WebhookEntity) -> Unit,
    private val onToggle: (WebhookEntity, Boolean) -> Unit,
    private val onDelete: (WebhookEntity) -> Unit
) : ListAdapter<WebhookEntity, WebhookAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemWebhookBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: WebhookEntity) {
            b.tvName.text = item.name
            b.tvUrl.text = item.url
            b.switchEnabled.isChecked = item.enabled
            b.switchEnabled.setOnCheckedChangeListener { _, checked -> onToggle(item, checked) }
            b.btnEdit.setOnClickListener { onEdit(item) }
            b.btnDelete.setOnClickListener { onDelete(item) }
            b.root.setOnClickListener { onEdit(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemWebhookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WebhookEntity>() {
            override fun areItemsTheSame(a: WebhookEntity, b: WebhookEntity) = a.id == b.id
            override fun areContentsTheSame(a: WebhookEntity, b: WebhookEntity) = a == b
        }
    }
}
