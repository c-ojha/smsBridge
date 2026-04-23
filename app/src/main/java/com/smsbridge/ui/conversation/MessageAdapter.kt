package com.smsbridge.ui.conversation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsbridge.data.db.entity.MessageEntity
import com.smsbridge.data.db.entity.MessageType
import com.smsbridge.databinding.ItemMessageReceivedBinding
import com.smsbridge.databinding.ItemMessageSentBinding
import com.smsbridge.util.SmsUtils

class MessageAdapter : ListAdapter<MessageEntity, RecyclerView.ViewHolder>(DIFF) {

    inner class SentViewHolder(private val b: ItemMessageSentBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MessageEntity) {
            b.tvBody.text = item.body
            b.tvTime.text = SmsUtils.formatTimestamp(item.timestamp)
        }
    }

    inner class ReceivedViewHolder(private val b: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MessageEntity) {
            b.tvBody.text = item.body
            b.tvTime.text = SmsUtils.formatTimestamp(item.timestamp)
        }
    }

    override fun getItemViewType(position: Int) = getItem(position).type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == MessageType.SENT) {
            SentViewHolder(ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ReceivedViewHolder(ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentViewHolder -> holder.bind(getItem(position))
            is ReceivedViewHolder -> holder.bind(getItem(position))
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MessageEntity>() {
            override fun areItemsTheSame(a: MessageEntity, b: MessageEntity) = a.id == b.id
            override fun areContentsTheSame(a: MessageEntity, b: MessageEntity) = a == b
        }
    }
}
