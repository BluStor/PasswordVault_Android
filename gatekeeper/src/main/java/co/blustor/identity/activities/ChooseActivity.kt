package co.blustor.identity.activities

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.bluetooth.le.ScanSettings.MATCH_MODE_AGGRESSIVE
import android.bluetooth.le.ScanSettings.SCAN_MODE_BALANCED
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelUuid
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import co.blustor.identity.R
import co.blustor.identity.adapters.ScanResultAdapter
import co.blustor.identity.gatekeeper.BluetoothClient
import co.blustor.identity.gatekeeper.GKCard
import co.blustor.identity.vault.Vault
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_cardchooser.*

class ChooseActivity : AppCompatActivity() {

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val scanResultAdapter = ScanResultAdapter()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            scanResultAdapter.addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                scanResultAdapter.addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(tag, "onScanFailed: " + errorCode)

            buttonScanToggle.isChecked = false
            progressBarResults.visibility = View.GONE
        }
    }
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")

        setContentView(R.layout.activity_cardchooser)
        title = getString(R.string.title_choose_card)

        // Views

        buttonScanToggle.setOnClickListener {
            if (isScanning) {
                stopScanning()
            } else {
                startScanning()
            }

            buttonScanToggle.isChecked = isScanning
        }

        progressBarResults.isEnabled = false

        val linearLayoutManager = LinearLayoutManager(this)

        recyclerViewResults.layoutManager = linearLayoutManager
        recyclerViewResults.adapter = scanResultAdapter

        scanResultAdapter.setOnAdapterItemClickListener(object : ScanResultAdapter.OnAdapterItemClickListener {
            override fun onAdapterViewClick(view: View) {
                val position = recyclerViewResults.getChildAdapterPosition(view)

                val scanResult = scanResultAdapter.getItemAtPosition(position)

                val address = scanResult.device.address

                // Make sure paired

                if (BluetoothClient.getBondState(address) != BluetoothDevice.BOND_BONDED) {
                    val alertDialog = AlertDialog.Builder(this@ChooseActivity).create()
                    alertDialog.setTitle("Alert")
                    alertDialog.setMessage(resources.getString(R.string.bluetooth_not_paired))
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
                    ) { dialog, _ -> dialog.dismiss() }
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    return
                }

                Vault.setCardAddress(this@ChooseActivity, address)
                stopScanning()

                val splashActivity = Intent(this@ChooseActivity, SplashActivity::class.java)
                startActivity(splashActivity)
                finish()
            }
        })

        // Scanner

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner

        // Scan

        startScanning()
    }

    private fun startScanning() {
        Log.d(tag, "startScanning")

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter.isEnabled) {
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            if (report.areAllPermissionsGranted()) {
                                val serviceUuid = ParcelUuid(GKCard.serviceUUID)

                                val scanFilter = ScanFilter.Builder()
                                        .setServiceUuid(serviceUuid)
                                        .build()

                                val scanSettings = ScanSettings.Builder()
                                        .setScanMode(SCAN_MODE_BALANCED)
                                        .setMatchMode(MATCH_MODE_AGGRESSIVE)
                                        .build()

                                val scanFilters = listOf<ScanFilter>(scanFilter)

                                bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)

                                isScanning = true
                                buttonScanToggle.isChecked = true
                                progressBarResults.visibility = View.VISIBLE
                            } else {
                                if (report.isAnyPermissionPermanentlyDenied) {
                                    val builder = AlertDialog.Builder(this@ChooseActivity)
                                            .setMessage(R.string.error_permission_location_scan)
                                            .setPositiveButton("Settings") { dialogInterface, _ ->
                                                val intent = Intent()
                                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                                val uri = Uri.fromParts("package", packageName, null)
                                                intent.data = uri
                                                startActivity(intent)

                                                dialogInterface.cancel()
                                            }
                                            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.cancel() }

                                    builder.create().show()
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                            token.continuePermissionRequest()
                        }
                    }).check()
        } else {
            val alertDialog = AlertDialog.Builder(this@ChooseActivity).create()
            alertDialog.setTitle("Alert")
            alertDialog.setMessage(resources.getString(R.string.bluetooth_not_enabled))
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    private fun stopScanning() {
        scanResultAdapter.clearScanResults()
        bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
        buttonScanToggle.isChecked = false
        progressBarResults.visibility = View.GONE
    }

    companion object {
        private val tag = "ChooseActivity"
    }
}
