package com.smsbridge.ui.webhook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsbridge.data.db.entity.FilterRuleEntity
import com.smsbridge.databinding.ItemFilterRuleBinding

class FilterRuleAdapter(
    private val onDelete: (FilterRuleEntity) -> Unit
) : ListAdapter<FilterRuleEntity, FilterRuleAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemFilterRuleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: FilterRuleEntity) {
            val negatePrefix = if (item.negate) "NOT " else ""
            b.tvRule.text = "${item.field} ${negatePrefix}${item.matchType} \"${item.value}\""
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFilterRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FilterRuleEntity>() {
            override fun areItemsTheSame(a: FilterRuleEntity, b: FilterRuleEntity) = a.id == b.id
            override fun areContentsTheSame(a: FilterRuleEntity, b: FilterRuleEntity) = a == b
        }
    }
}
