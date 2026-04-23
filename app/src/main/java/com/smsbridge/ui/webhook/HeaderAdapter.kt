package com.smsbridge.ui.webhook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsbridge.data.db.entity.WebhookHeaderEntity
import com.smsbridge.databinding.ItemHeaderBinding

class HeaderAdapter(
    private val onDelete: (WebhookHeaderEntity) -> Unit,
    private val onDecrypt: (String) -> String
) : ListAdapter<WebhookHeaderEntity, HeaderAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemHeaderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: WebhookHeaderEntity) {
            b.tvKey.text = item.key
            if (item.isSecret) {
                b.tvValue.text = "••••••••"
                b.btnReveal.visibility = android.view.View.VISIBLE
                b.btnReveal.setOnClickListener {
                    val revealed = onDecrypt(item.encryptedValue)
                    b.tvValue.text = revealed
                    b.btnReveal.visibility = android.view.View.GONE
                }
            } else {
                b.tvValue.text = onDecrypt(item.encryptedValue)
                b.btnReveal.visibility = android.view.View.GONE
            }
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WebhookHeaderEntity>() {
            override fun areItemsTheSame(a: WebhookHeaderEntity, b: WebhookHeaderEntity) = a.id == b.id
            override fun areContentsTheSame(a: WebhookHeaderEntity, b: WebhookHeaderEntity) = a == b
        }
    }
}
