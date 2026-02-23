package com.altercard

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory((application as altercardApplication).repository)
    }

    private val addCardActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val name = it.getStringExtra(AddCardActivity.EXTRA_NAME)
                    val number = it.getStringExtra(AddCardActivity.EXTRA_NUMBER)
                    val barcodeData = it.getStringExtra(AddCardActivity.EXTRA_BARCODE_DATA)
                    val barcodeFormat = it.getStringExtra(AddCardActivity.EXTRA_BARCODE_FORMAT)

                    if(name != null && number != null){
                         val card = Card(name = name, number = number, barcodeData = barcodeData, barcodeFormat = barcodeFormat)
                         cardViewModel.insert(card)
                    }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.cards_recycler_view)
        val fab = findViewById<ExtendedFloatingActionButton>(R.id.add_card_fab)
        val nestedScrollView = findViewById<NestedScrollView>(R.id.nested_scroll_view)
        val adapter = CardAdapter()
        
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && fab.isExtended) {
                fab.shrink()
            } else if (scrollY < oldScrollY && !fab.isExtended) {
                fab.extend()
            }
        })

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private val background = ContextCompat.getDrawable(this@MainActivity, R.drawable.swipe_background)
            private val deleteIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)
            
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // We don't want to support drag and drop
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val card = adapter.currentList[position]
                
                cardViewModel.delete(card)

                Snackbar.make(recyclerView, "Карта удалена", Snackbar.LENGTH_LONG)
                    .setAction("Отменить") { 
                        cardViewModel.insert(card)
                    }
                    .show()
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                val itemView = viewHolder.itemView
                
                if (dX > 0) { // Swiping to the right
                    background?.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                    val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconBottom = iconTop + deleteIcon.intrinsicHeight
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + deleteIcon.intrinsicWidth
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                } else if (dX < 0) { // Swiping to the left
                    background?.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                    val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconBottom = iconTop + deleteIcon.intrinsicHeight
                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                } else { // view is unSwiped
                    background?.setBounds(0, 0, 0, 0)
                }

                background?.draw(c)
                deleteIcon!!.draw(c)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        cardViewModel.allCards.observe(this) { cards ->
            cards?.let { adapter.submitList(it) }
        }
        
        fab.setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)
            addCardActivityResult.launch(intent)
        }
    }
}
