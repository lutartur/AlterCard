package com.altercard

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
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
import com.google.zxing.EncodeHintType
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
        setContentView(R.layout.activity_card_detail)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val dp16 = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_detail_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(dp16, systemBars.top, dp16, systemBars.bottom)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_card_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_settings -> {
                showSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun generateBarcode(barcodeData: String?, barcodeFormatStr: String?) {
        val barcodeImageView = findViewById<ImageView>(R.id.barcode_image_view)
        if (barcodeData != null && barcodeFormatStr != null) {
            try {
                val format = BarcodeFormat.valueOf(barcodeFormatStr)
                val multiFormatWriter = MultiFormatWriter()
                val (w, h) = if (format == BarcodeFormat.QR_CODE) 800 to 800 else 800 to 200
                val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
                val bitMatrix = multiFormatWriter.encode(barcodeData, format, w, h, hints)
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

    private fun showSettingsDialog() {
        val items = arrayOf(getString(R.string.action_rename), getString(R.string.action_delete))
        MaterialAlertDialogBuilder(this)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showRenameDialog()
                    1 -> showDeleteConfirmationDialog()
                }
            }
            .show()
    }

    private fun showRenameDialog() {
        val editText = EditText(this)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_rename)
            .setView(editText)
            .setPositiveButton(R.string.button_add) { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    currentCard?.let {
                        val updatedCard = it.copy(name = newName)
                        cardViewModel.update(updatedCard)
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_card_title)
            .setMessage(R.string.delete_card_message)
            .setNegativeButton(R.string.button_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                currentCard?.let { cardViewModel.delete(it) }
            }
            .show()
    }

    companion object {
        const val EXTRA_ID = "com.altercard.card.ID"
    }
}
