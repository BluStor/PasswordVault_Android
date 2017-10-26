package co.blustor.identity.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import co.blustor.identity.R;
import co.blustor.identity.adapters.ScanResultAdapter;
import co.blustor.identity.vault.Vault;
import co.blustor.identity.gatekeeper.GKBLECard;
import co.blustor.identity.utils.MyApplication;
import rx.Subscription;

public class ChooseActivity extends AppCompatActivity {

    private static ScanSettings SCAN_SETTINGS = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();
    private static ScanFilter SCAN_FILTER = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(GKBLECard.SERVICE_UUID)).build();

    @Nullable
    private Subscription mScanSubscription;
    private ScanResultAdapter mScanResultAdapter = new ScanResultAdapter();

    private ToggleButton mScanToggleButton;
    private ProgressBar mProgressBar;
    private RecyclerView mCardsRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cardchooser);
        setTitle(getString(R.string.title_choose_card));

        // Views

        mScanToggleButton = findViewById(R.id.button_scan_toggle);
        mScanToggleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        mProgressBar = findViewById(R.id.progressbar_results);
        mProgressBar.setEnabled(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mCardsRecyclerView = findViewById(R.id.recyclerview_results);
        mCardsRecyclerView.setLayoutManager(linearLayoutManager);
        mCardsRecyclerView.setAdapter(mScanResultAdapter);

        mScanResultAdapter.setOnAdapterItemClickListener(view -> {
            int position = mCardsRecyclerView.getChildAdapterPosition(view);
            ScanResult scanResult = mScanResultAdapter.getItemAtPosition(position);

            RxBleDevice device = scanResult.getBleDevice();
            String address = device.getMacAddress();
            String name = device.getName();

            Vault.setCardAddressName(this, address, name);
            Intent splashActivity = new Intent(this, SplashActivity.class);
            startActivity(splashActivity);
            finish();
        });

        // Scan

        startScanning();
    }

    private void startScanning() {
        mScanToggleButton.setChecked(true);
        mProgressBar.setVisibility(View.VISIBLE);

        if (mScanSubscription == null || mScanSubscription.isUnsubscribed()) {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            mScanSubscription = MyApplication.getBleClient(ChooseActivity.this)
                                    .scanBleDevices(SCAN_SETTINGS, SCAN_FILTER)
                                    .subscribe(
                                            scanResult -> {
                                                mProgressBar.setVisibility(View.GONE);
                                                mScanResultAdapter.addScanResult(scanResult);
                                            },
                                            throwable -> {
                                                onScanFailure(throwable);
                                                stopScanning();
                                            }
                                    );
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            if (response.isPermanentlyDenied()) {
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

                            stopScanning();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        }
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            handleBleException((BleScanException) throwable);
        }
    }

    private void handleBleException(BleScanException exception) {
        String error;
        switch (exception.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                error = "Bluetooth is not available.";
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                error = "Bluetooth is disabled.";
                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                error = "Scan already started.";
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                error = "Location services is disabled.";
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                error = "Location permission is missing.";
                break;
            default:
                error = "Unknown error.";
        }

        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private void stopScanning() {
        mScanToggleButton.setChecked(false);
        mProgressBar.setVisibility(View.GONE);

        mScanResultAdapter.clearScanResults();

        if (mScanSubscription != null && !mScanSubscription.isUnsubscribed()) {
            mScanSubscription.unsubscribe();
        }
    }
}
