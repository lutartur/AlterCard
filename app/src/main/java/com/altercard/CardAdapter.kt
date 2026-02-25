package com.altercard

import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class CardAdapter : ListAdapter<Card, CardAdapter.CardViewHolder>(CardsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_view)
        private val cardName: TextView = itemView.findViewById(R.id.card_name)
        private val cardNumber: TextView = itemView.findViewById(R.id.card_number)

        fun bind(card: Card) {
            cardName.text = card.name
            cardNumber.text = card.number

            val context = itemView.context

            if (card.customBackgroundColor != null) {
                cardView.setCardBackgroundColor(card.customBackgroundColor)
            } else {
                val tv = TypedValue()
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, tv, true)
                cardView.setCardBackgroundColor(tv.data)
            }

            if (card.customTextColor != null) {
                cardName.setTextColor(card.customTextColor)
                cardNumber.setTextColor(card.customTextColor)
            } else {
                val tv = TypedValue()
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, tv, true)
                cardName.setTextColor(tv.data)
                cardNumber.setTextColor(tv.data)
            }

            itemView.setOnClickListener {
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
