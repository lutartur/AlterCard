package com.altercard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.altercard.databinding.ActivityAddCardBinding
import com.google.zxing.BarcodeFormat

class AddCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCardBinding
    private var barcodeData: String? = null
    private var barcodeFormat: String? = null

    private val scanBarcodeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
                barcodeData = it.getStringExtra(ScannerActivity.EXTRA_BARCODE_DATA)
                barcodeFormat = it.getStringExtra(ScannerActivity.EXTRA_BARCODE_FORMAT)
                binding.editCardNumber.setText(barcodeData)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean ->
        if (isGranted) {
            startScanner()
        } else {
            Toast.makeText(this, R.string.toast_camera_permission, Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAddCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.addCardContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + ime.bottom)
            insets
        }

        intent.getStringExtra(EXTRA_PREFILL_BARCODE_DATA)?.let { prefillData ->
            barcodeData = prefillData
            barcodeFormat = intent.getStringExtra(EXTRA_PREFILL_BARCODE_FORMAT) ?: BarcodeFormat.CODE_128.name
            binding.editCardNumber.setText(prefillData)
        }

        binding.buttonScan.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    startScanner()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        binding.buttonAdd.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(binding.editCardName.text) || TextUtils.isEmpty(binding.editCardNumber.text)) {
                setResult(RESULT_CANCELED, replyIntent)
            } else {
                val cardName = binding.editCardName.text.toString()
                val cardNumber = binding.editCardNumber.text.toString()
                replyIntent.putExtra(EXTRA_NAME, cardName)
                replyIntent.putExtra(EXTRA_NUMBER, cardNumber)
                replyIntent.putExtra(EXTRA_BARCODE_DATA, barcodeData ?: cardNumber)
                // If no barcode was scanned, default to CODE_128
                replyIntent.putExtra(EXTRA_BARCODE_FORMAT, barcodeFormat ?: BarcodeFormat.CODE_128.name)
                setResult(RESULT_OK, replyIntent)
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
        const val EXTRA_PREFILL_BARCODE_DATA = "com.altercard.add.PREFILL_BARCODE_DATA"
        const val EXTRA_PREFILL_BARCODE_FORMAT = "com.altercard.add.PREFILL_BARCODE_FORMAT"
    }
}
