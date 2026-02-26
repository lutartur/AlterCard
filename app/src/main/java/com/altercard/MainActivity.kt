@file:Suppress("DEPRECATION")

package com.altercard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.net.toUri
import com.altercard.databinding.ActivityMainBinding
import com.altercard.databinding.DialogSupportBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

private const val PREFS_NAME = "altercard_prefs"
private const val KEY_SIGN_IN_PROMPTED = "sign_in_prompted"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val app get() = application as AltercardApplication

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory(app.repository, app)
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            task.getResult(ApiException::class.java)
            app.buildSyncManager()
            cardViewModel.restoreFromDrive()
        } catch (_: ApiException) {
            Toast.makeText(this, R.string.toast_sign_in_failed, Toast.LENGTH_LONG).show()
        }
    }

    private val drivePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            cardViewModel.restoreFromDrive()
        }
    }

    private val addCardLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
                val name = it.getStringExtra(AddCardActivity.EXTRA_NAME)
                val number = it.getStringExtra(AddCardActivity.EXTRA_NUMBER)
                val barcodeData = it.getStringExtra(AddCardActivity.EXTRA_BARCODE_DATA)
                val barcodeFormat = it.getStringExtra(AddCardActivity.EXTRA_BARCODE_FORMAT)
                if (name != null && number != null) {
                    cardViewModel.insert(
                        Card(name = name, number = number,
                            barcodeData = barcodeData, barcodeFormat = barcodeFormat)
                    )
                }
            }
        }
    }

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = Intent(this, AddCardActivity::class.java)
        when (result.resultCode) {
            RESULT_OK -> {
                val barcodeData = result.data?.getStringExtra(ScannerActivity.EXTRA_BARCODE_DATA)
                val barcodeFormat = result.data?.getStringExtra(ScannerActivity.EXTRA_BARCODE_FORMAT)
                if (barcodeData != null) {
                    intent.putExtra(AddCardActivity.EXTRA_PREFILL_BARCODE_DATA, barcodeData)
                    intent.putExtra(AddCardActivity.EXTRA_PREFILL_BARCODE_FORMAT, barcodeFormat)
                }
                addCardLauncher.launch(intent)
            }
            ScannerActivity.RESULT_MANUAL_INPUT -> addCardLauncher.launch(intent)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) scannerLauncher.launch(Intent(this, ScannerActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { showSupportDialog() }

        val adapter = CardAdapter()
        binding.cardsRecyclerView.adapter = adapter
        binding.cardsRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY && binding.addCardFab.isExtended) binding.addCardFab.shrink()
                else if (scrollY < oldScrollY && !binding.addCardFab.isExtended) binding.addCardFab.extend()
            }
        )

        cardViewModel.allCards.observe(this) { cards ->
            cards?.let { adapter.submitList(it) }
        }

        binding.addCardFab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                scannerLauncher.launch(Intent(this, ScannerActivity::class.java))
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        lifecycleScope.launch {
            cardViewModel.syncState.collect { state ->
                when (state) {
                    is SyncState.Idle, is SyncState.Syncing -> {}
                    is SyncState.Success -> {
                        Toast.makeText(this@MainActivity,
                            R.string.toast_sync_success, Toast.LENGTH_SHORT).show()
                        cardViewModel.resetSyncState()
                    }
                    is SyncState.NeedsPermission -> {
                        drivePermissionLauncher.launch(state.intent)
                        cardViewModel.resetSyncState()
                    }
                    is SyncState.Error -> {
                        Toast.makeText(this@MainActivity,
                            state.message, Toast.LENGTH_LONG).show()
                        cardViewModel.resetSyncState()
                    }
                }
            }
        }

        maybeShowSignInDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                if (app.driveAuthManager.isSignedIn()) {
                    cardViewModel.manualSync()
                } else {
                    startSignIn()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun maybeShowSignInDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SIGN_IN_PROMPTED, false)) return
        prefs.edit { putBoolean(KEY_SIGN_IN_PROMPTED, true) }

        if (app.driveAuthManager.isSignedIn()) return

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_sync_title)
            .setMessage(R.string.dialog_sync_message)
            .setPositiveButton(R.string.dialog_sync_sign_in) { _, _ -> startSignIn() }
            .setNegativeButton(R.string.dialog_sync_skip, null)
            .show()
    }

    private fun startSignIn() {
        signInLauncher.launch(app.driveAuthManager.signInClient.signInIntent)
    }

    private fun showSupportDialog() {
        val dialogBinding = DialogSupportBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.support_dialog_title)
            .setView(dialogBinding.root)
            .show()
        dialogBinding.itemBmac.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, URL_BUYMEACOFFEE.toUri()))
            dialog.dismiss()
        }
        dialogBinding.itemBoosty.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, URL_BOOSTY.toUri()))
            dialog.dismiss()
        }
    }

    companion object {
        private const val URL_BUYMEACOFFEE = "https://buymeacoffee.com/your_username"
        private const val URL_BOOSTY = "https://boosty.to/your_username"
    }
}
