package com.smsbridge.ui.inbox

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsbridge.data.db.entity.MessageEntity
import com.smsbridge.databinding.ItemConversationBinding
import com.smsbridge.util.SmsUtils

class ConversationAdapter(
    private val onClick: (MessageEntity) -> Unit
) : ListAdapter<MessageEntity, ConversationAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemConversationBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MessageEntity) {
            b.tvSender.text = SmsUtils.getContactName(b.root.context, item.address)
            b.tvPreview.text = item.body
            b.tvTime.text = SmsUtils.formatTimestamp(item.timestamp)
            b.tvUnread.visibility = if (!item.read) android.view.View.VISIBLE else android.view.View.GONE
            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MessageEntity>() {
            override fun areItemsTheSame(a: MessageEntity, b: MessageEntity) = a.address == b.address
            override fun areContentsTheSame(a: MessageEntity, b: MessageEntity) = a == b
        }
    }
}
