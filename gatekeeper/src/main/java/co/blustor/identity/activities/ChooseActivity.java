package co.blustor.identity.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Collections;
import java.util.List;

import co.blustor.identity.R;
import co.blustor.identity.adapters.ScanResultAdapter;
import co.blustor.identity.gatekeeper.GKCard;
import co.blustor.identity.vault.Vault;

import static android.bluetooth.le.ScanSettings.MATCH_MODE_AGGRESSIVE;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_BALANCED;

public class ChooseActivity extends AppCompatActivity {

    private static final String TAG = "ChooseActivity";

    private BluetoothLeScanner mBlutoothLeScanner;
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            mScanResultAdapter.addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                mScanResultAdapter.addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed: " + errorCode);

            mScanToggleButton.setChecked(false);
            mProgressBar.setVisibility(View.GONE);
        }
    };
    private ScanResultAdapter mScanResultAdapter = new ScanResultAdapter();
    private ToggleButton mScanToggleButton;
    private ProgressBar mProgressBar;
    private RecyclerView mCardsRecyclerView;
    private boolean isScanning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_cardchooser);
        setTitle(getString(R.string.title_choose_card));

        // Views

        mScanToggleButton = findViewById(R.id.button_scan_toggle);
        mScanToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScanning) {
                    stopScanning();
                } else {
                    startScanning();
                }

                mScanToggleButton.setChecked(isScanning);
            }
        });

        mProgressBar = findViewById(R.id.progressbar_results);
        mProgressBar.setEnabled(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mCardsRecyclerView = findViewById(R.id.recyclerview_results);
        mCardsRecyclerView.setLayoutManager(linearLayoutManager);
        mCardsRecyclerView.setAdapter(mScanResultAdapter);

        mScanResultAdapter.setOnAdapterItemClickListener(view -> {
            stopScanning();

            int position = mCardsRecyclerView.getChildAdapterPosition(view);
            ScanResult scanResult = mScanResultAdapter.getItemAtPosition(position);

            String address = scanResult.getDevice().getAddress();
            Vault.setCardAddress(this, address);

            Intent splashActivity = new Intent(this, SplashActivity.class);
            startActivity(splashActivity);
            finish();
        });

        // Scanner

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBlutoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner();
        }

        // Scan

        startScanning();
    }

    private void startScanning() {
        Log.d(TAG, "startScanning");

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            if (!bluetoothManager.getAdapter().isEnabled()) {
                Toast.makeText(this, "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
            } else {
                Dexter.withActivity(this)
                        .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    ParcelUuid serviceUuid = new ParcelUuid(GKCard.SERVICE_UUID);

                                    ScanFilter scanFilter = new ScanFilter.Builder()
                                            .setServiceUuid(serviceUuid)
                                            .build();

                                    ScanSettings scanSettings = new ScanSettings.Builder()
                                            .setScanMode(SCAN_MODE_BALANCED)
                                            .setMatchMode(MATCH_MODE_AGGRESSIVE)
                                            .build();

                                    List<ScanFilter> scanFilters = Collections.singletonList(scanFilter);

                                    mBlutoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);

                                    isScanning = true;
                                    mScanToggleButton.setChecked(true);
                                    mProgressBar.setVisibility(View.VISIBLE);
                                } else {
                                    if (report.isAnyPermissionPermanentlyDenied()) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ChooseActivity.this)
                                                .setMessage(R.string.error_permission_location_scan)
                                                .setPositiveButton("Settings", (dialogInterface, i) -> {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                                    intent.setData(uri);
                                                    startActivity(intent);

                                                    dialogInterface.cancel();
                                                })
                                                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

                                        builder.create().show();
                                    }
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        }
    }

    private void stopScanning() {
        mBlutoothLeScanner.stopScan(mScanCallback);
        isScanning = false;
        mScanToggleButton.setChecked(false);
        mProgressBar.setVisibility(View.GONE);
    }
}
