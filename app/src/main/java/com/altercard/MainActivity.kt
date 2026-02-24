package com.altercard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory((application as AltercardApplication).repository)
    }

    private val addCardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
                val name = it.getStringExtra(AddCardActivity.EXTRA_NAME)
                val number = it.getStringExtra(AddCardActivity.EXTRA_NUMBER)
                val barcodeData = it.getStringExtra(AddCardActivity.EXTRA_BARCODE_DATA)
                val barcodeFormat = it.getStringExtra(AddCardActivity.EXTRA_BARCODE_FORMAT)

                if (name != null && number != null) {
                    cardViewModel.insert(Card(name = name, number = number, barcodeData = barcodeData, barcodeFormat = barcodeFormat))
                }
            }
        }
    }

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = Intent(this, AddCardActivity::class.java)
        when (result.resultCode) {
            RESULT_OK -> {
                val barcodeData = result.data?.getStringExtra(ScannerActivity.EXTRA_BARCODE_DATA)
                if (barcodeData != null) {
                    intent.putExtra(AddCardActivity.EXTRA_PREFILL_BARCODE_DATA, barcodeData)
                }
                addCardLauncher.launch(intent)
            }
            ScannerActivity.RESULT_MANUAL_INPUT -> {
                addCardLauncher.launch(intent)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            scannerLauncher.launch(Intent(this, ScannerActivity::class.java))
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                scannerLauncher.launch(Intent(this, ScannerActivity::class.java))
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
