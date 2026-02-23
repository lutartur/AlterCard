package com.altercard

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class CardDetailActivity : AppCompatActivity() {

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory((application as AltercardApplication).repository)
    }

    private var currentCard: Card? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        title = "" // Set the title to an empty string
        setContentView(R.layout.activity_card_detail)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_detail_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cardId = intent.getIntExtra(EXTRA_ID, -1)

        cardViewModel.getCard(cardId).observe(this) { card ->
            if (card == null) {
                // The card has been deleted, so finish this activity
                finish()
                return@observe
            }
            currentCard = card
            findViewById<TextView>(R.id.detail_card_name).text = card.name
            findViewById<TextView>(R.id.detail_card_number).text = card.number
            generateBarcode(card.barcodeData, card.barcodeFormat)
        }
    }

    private fun generateBarcode(barcodeData: String?, barcodeFormatStr: String?) {
        val barcodeImageView = findViewById<ImageView>(R.id.barcode_image_view)
        if (barcodeData != null && barcodeFormatStr != null) {
            try {
                val format = BarcodeFormat.valueOf(barcodeFormatStr)
                val multiFormatWriter = MultiFormatWriter()
                val bitMatrix = multiFormatWriter.encode(barcodeData, format, 800, 200)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
                barcodeImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Error generating barcode", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            // Optionally handle the case where there is no barcode data
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_card_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Удалить карту")
            .setMessage("Вы уверены, что хотите удалить эту карту? Это действие нельзя будет отменить.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                currentCard?.let { cardViewModel.delete(it) }
            }
            .show()
    }

    companion object {
        const val EXTRA_ID = "com.altercard.card.ID"
    }
}
