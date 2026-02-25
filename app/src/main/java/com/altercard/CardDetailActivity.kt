package com.altercard

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.graphics.toColorInt
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
                finish()
                return@observe
            }
            currentCard = card
            findViewById<TextView>(R.id.detail_card_avatar).text = card.name.firstOrNull()?.uppercaseChar()?.toString() ?: ""
            findViewById<TextView>(R.id.detail_card_name).text = card.name
            findViewById<TextView>(R.id.detail_card_number).text = card.number
            applyCardColors(card)
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

    private fun applyCardColors(card: Card) {
        val avatarView = findViewById<TextView>(R.id.detail_card_avatar)

        avatarView.backgroundTintList = if (card.customBackgroundColor != null) {
            android.content.res.ColorStateList.valueOf(card.customBackgroundColor)
        } else {
            null
        }

        avatarView.setTextColor(
            card.customTextColor ?: "#4a5568".toColorInt()
        )
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
                if (format == BarcodeFormat.QR_CODE) {
                    barcodeImageView.doOnLayout { view ->
                        val lp = view.layoutParams
                        lp.height = view.width
                        view.layoutParams = lp
                    }
                }
                barcodeImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, R.string.toast_barcode_error, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun showSettingsDialog() {
        val items = arrayOf(
            getString(R.string.action_rename),
            getString(R.string.action_change_number),
            getString(R.string.action_change_color),
            getString(R.string.action_delete)
        )
        MaterialAlertDialogBuilder(this)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showRenameDialog()
                    1 -> showChangeNumberDialog()
                    2 -> showColorOptionsDialog()
                    3 -> showDeleteConfirmationDialog()
                }
            }
            .show()
    }

    private fun showRenameDialog() {
        val editText = EditText(this)
        editText.setText(currentCard?.name)
        editText.setSelection(editText.text.length)
        val container = FrameLayout(this)
        val margin = (24 * resources.displayMetrics.density).toInt()
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(margin, 0, margin, 0)
        editText.layoutParams = params
        container.addView(editText)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_rename)
            .setView(container)
            .setPositiveButton(R.string.button_add) { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    currentCard?.let {
                        cardViewModel.update(it.copy(name = newName))
                    }
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun showChangeNumberDialog() {
        val editText = EditText(this)
        editText.setText(currentCard?.number)
        editText.setSelection(editText.text.length)
        val container = FrameLayout(this)
        val margin = (24 * resources.displayMetrics.density).toInt()
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(margin, 0, margin, 0)
        editText.layoutParams = params
        container.addView(editText)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_change_number)
            .setView(container)
            .setPositiveButton(R.string.button_add) { _, _ ->
                val newNumber = editText.text.toString()
                if (newNumber.isNotEmpty()) {
                    currentCard?.let {
                        cardViewModel.update(it.copy(number = newNumber, barcodeData = newNumber))
                    }
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun showColorOptionsDialog() {
        val items = arrayOf(
            getString(R.string.color_option_default),
            getString(R.string.color_option_background),
            getString(R.string.color_option_text)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_change_color)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> currentCard?.let {
                        cardViewModel.update(it.copy(customBackgroundColor = null, customTextColor = null))
                    }
                    1 -> showColorPickerDialog(isBackground = true)
                    2 -> showColorPickerDialog(isBackground = false)
                }
            }
            .show()
    }

    private fun showColorPickerDialog(isBackground: Boolean) {
        val card = currentCard ?: return
        val title = if (isBackground) R.string.color_option_background else R.string.color_option_text

        val initialColor = if (isBackground) {
            card.customBackgroundColor ?: "#ebeff2".toColorInt()
        } else {
            card.customTextColor ?: "#4a5568".toColorInt()
        }

        val colorPicker = ColorPickerView(this)
        colorPicker.setColor(initialColor)

        val padding = (16 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(padding, padding, padding, padding)
        colorPicker.layoutParams = params
        container.addView(colorPicker)

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(container)
            .setPositiveButton(R.string.button_apply) { _, _ ->
                val selectedColor = colorPicker.getColor()
                val updated = if (isBackground) {
                    card.copy(customBackgroundColor = selectedColor)
                } else {
                    card.copy(customTextColor = selectedColor)
                }
                cardViewModel.update(updated)
            }
            .setNegativeButton(R.string.button_cancel, null)
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
