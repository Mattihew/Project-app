package com.mattihew.project_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback callback;
    private AdvertiseData data;

    private String androidID;
    private String btName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (BluetoothAdapter.getDefaultAdapter() == null || !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported())
        {
            Toast.makeText(this, "BLE not supported",Toast.LENGTH_LONG).show();
            findViewById(R.id.startBtn).setEnabled(false);
        }
        else
        {
            bleSetup();
        }
        findViewById(R.id.stopBtn).setEnabled(false);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final WebView webview = (WebView) findViewById(R.id.webView1);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl(prefs.getString("server_address", "http://example.com"));
        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key)
            {
                if (key.equals("server_address"))
                {
                    webview.loadUrl(sharedPreferences.getString("server_address", "http://example.com"));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v)
    {
        if (v.getId() == R.id.startBtn)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            final int adMode = Integer.parseInt(prefs.getString(
                    "sync_frequency",
                    Integer.toString(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)));
            final int adPower = Integer.parseInt(prefs.getString(
                    "sync_power",
                    Integer.toString(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)));
            final AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(adMode)
                    .setTxPowerLevel(adPower)
                    .setConnectable(false)
                    .build();
            this.btName = BluetoothAdapter.getDefaultAdapter().getName();
            BluetoothAdapter.getDefaultAdapter().setName(androidID);
            advertiser.startAdvertising(settings, data, callback);
            v.setEnabled(false);
            findViewById(R.id.stopBtn).setEnabled(true);
            Toast.makeText(this, "Bluetooth advertising started", Toast.LENGTH_SHORT).show();
        }
        else if(v.getId() == R.id.stopBtn)
        {
            advertiser.stopAdvertising(callback);
            BluetoothAdapter.getDefaultAdapter().setName(this.btName);
            this.btName = null;
            v.setEnabled(false);
            findViewById(R.id.startBtn).setEnabled(true);
            Toast.makeText(this, "Bluetooth advertising stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void bleSetup()
    {
        this.advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        ParcelUuid pUuid = ParcelUuid.fromString(getString(R.string.ble_uuid));

        this.data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(pUuid)
                //.addServiceData(pUuid, "Data".getBytes(Charset.forName("UTF-8")))
                .build();
        this.callback = new AdvertiseCallback()
        {
            @Override
            public void onStartSuccess(final AdvertiseSettings settingsInEffect)
            {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(final int errorCode)
            {
                Log.e("BLE", "advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };
    }
}
