package com.mattihew.project_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    private BluetoothLeAdvertiser advertiser;
    private AdvertiseSettings settings;

    private AdvertiseCallback callback;
    private AdvertiseData data;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported())
        {
            Toast.makeText(this, "BLE not supported",Toast.LENGTH_SHORT);
        }

        bleSetup();

        WebView webview = (WebView) findViewById(R.id.webView1);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("https://example.com");
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v)
    {
        if (v.getId() == R.id.startBtn)
        {
            System.out.println("start");
            advertiser.startAdvertising(settings, data, callback);
        }
        else if(v.getId() == R.id.stopBtn)
        {
            System.out.println("stop");
            advertiser.stopAdvertising(callback);
        }
    }

    private BluetoothLeAdvertiser bleSetup()
    {
        this.advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        this.settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
                .setConnectable(false)
                .build();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)));

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
        return this.advertiser;
    }
}
