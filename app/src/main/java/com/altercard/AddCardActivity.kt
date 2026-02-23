package com.altercard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.zxing.BarcodeFormat

class AddCardActivity : AppCompatActivity() {

    private lateinit var editCardName: EditText
    private lateinit var editCardNumber: EditText
    private var barcodeData: String? = null
    private var barcodeFormat: String? = null

    private val scanBarcodeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                barcodeData = it.getStringExtra(ScannerActivity.EXTRA_BARCODE_DATA)
                barcodeFormat = it.getStringExtra(ScannerActivity.EXTRA_BARCODE_FORMAT)
                editCardNumber.setText(barcodeData)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean ->
        if (isGranted) {
            startScanner()
        } else {
            Toast.makeText(this, "Camera permission is required to scan barcodes", Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_card_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editCardName = findViewById(R.id.edit_card_name)
        editCardNumber = findViewById(R.id.edit_card_number)

        val scanButton = findViewById<Button>(R.id.button_scan)
        scanButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    startScanner()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        val addButton = findViewById<Button>(R.id.button_add)
        addButton.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editCardName.text) || TextUtils.isEmpty(editCardNumber.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val cardName = editCardName.text.toString()
                val cardNumber = editCardNumber.text.toString()
                replyIntent.putExtra(EXTRA_NAME, cardName)
                replyIntent.putExtra(EXTRA_NUMBER, cardNumber)
                replyIntent.putExtra(EXTRA_BARCODE_DATA, barcodeData ?: cardNumber)
                // If no barcode was scanned, default to CODE_128
                replyIntent.putExtra(EXTRA_BARCODE_FORMAT, barcodeFormat ?: BarcodeFormat.CODE_128.name)
                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startScanner() {
        val intent = Intent(this, ScannerActivity::class.java)
        scanBarcodeLauncher.launch(intent)
    }

    companion object {
        const val EXTRA_NAME = "com.altercard.card.NAME"
        const val EXTRA_NUMBER = "com.altercard.card.NUMBER"
        const val EXTRA_BARCODE_DATA = "com.altercard.card.BARCODE_DATA"
        const val EXTRA_BARCODE_FORMAT = "com.altercard.card.BARCODE_FORMAT"
    }
}
