package com.altercard

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CardAdapter : ListAdapter<Card, CardAdapter.CardViewHolder>(CardsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardAvatar: TextView = itemView.findViewById(R.id.card_avatar)
        private val cardName: TextView = itemView.findViewById(R.id.card_name)
        private val cardNumber: TextView = itemView.findViewById(R.id.card_number)

        fun bind(card: Card) {
            cardAvatar.text = card.name.firstOrNull()?.uppercaseChar()?.toString() ?: ""
            cardName.text = card.name
            cardNumber.text = card.number

            cardAvatar.backgroundTintList = if (card.customBackgroundColor != null) {
                android.content.res.ColorStateList.valueOf(card.customBackgroundColor)
            } else {
                null
            }

            cardAvatar.setTextColor(
                card.customTextColor ?: ContextCompat.getColor(itemView.context, R.color.avatar_letter)
            )

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, CardDetailActivity::class.java).apply {
                    putExtra(CardDetailActivity.EXTRA_ID, card.id)
                }
                context.startActivity(intent)
            }
        }

        companion object {
            fun create(parent: ViewGroup): CardViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_card, parent, false)
                return CardViewHolder(view)
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
