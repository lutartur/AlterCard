package com.altercard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory((application as AltercardApplication).repository)
    }

    private val addCardActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
            if (result.resultCode == RESULT_OK) {
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

        cardViewModel.allCards.observe(this) { cards ->
            cards?.let { adapter.submitList(it) }
        }
        
        fab.setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)
            addCardActivityResult.launch(intent)
        }
    }
}
