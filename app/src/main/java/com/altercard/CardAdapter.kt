package com.altercard

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.altercard.databinding.ListItemCardBinding

class CardAdapter : ListAdapter<Card, CardAdapter.CardViewHolder>(CardsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CardViewHolder(private val binding: ListItemCardBinding) : RecyclerView.ViewHolder(binding.root) {

        private val defaultTextColor = ContextCompat.getColor(binding.root.context, R.color.avatar_letter)

        fun bind(card: Card) {
            binding.cardAvatar.text = card.name.firstOrNull()?.uppercaseChar()?.toString() ?: ""
            binding.cardName.text = card.name
            binding.cardNumber.text = card.number

            binding.cardAvatar.backgroundTintList = if (card.customBackgroundColor != null) {
                android.content.res.ColorStateList.valueOf(card.customBackgroundColor)
            } else {
                null
            }

            binding.cardAvatar.setTextColor(card.customTextColor ?: defaultTextColor)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, CardDetailActivity::class.java).apply {
                    putExtra(CardDetailActivity.EXTRA_ID, card.id)
                }
                context.startActivity(intent)
            }
        }

        companion object {
            fun create(parent: ViewGroup): CardViewHolder {
                val binding = ListItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CardViewHolder(binding)
            }
        }
    }

    class CardsComparator : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }
}
