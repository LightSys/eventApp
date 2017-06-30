package org.lightsys.eventApp.tools.qr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;

import org.lightsys.eventApp.R;

import java.util.List;

import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;

/**
 * Created by otter57 on 5/9/17.
 * https://android-arsenal.com/details/1/4516
 */

public class myTest extends AppCompatActivity implements BarcodeRetriever{

    private static final String QR_DATA_EXTRA = "qr_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscan_layout);

        BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);
        //barcodeCapture.refresh();
        barcodeCapture.setRetrieval(myTest.this);

    }

    // for one time scan
    @Override
    public void onRetrieved(final Barcode barcode) {
        Log.d("myTest", "Barcode read: " + barcode.displayValue);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(QR_DATA_EXTRA, barcode.displayValue);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });


    }

    // for multiple callback
    @Override
    public void onRetrievedMultiple(final Barcode closetToClick, final List<BarcodeGraphic> barcodeGraphics) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = "Code selected : " + closetToClick.displayValue + "\n\nother " +
                        "codes in frame include : \n";
                for (int index = 0; index < barcodeGraphics.size(); index++) {
                    Barcode barcode = barcodeGraphics.get(index).getBarcode();
                    message += (index + 1) + ". " + barcode.displayValue + "\n";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(myTest.this)
                        .setTitle("code retrieved")
                        .setMessage(message);
                builder.show();
            }
        });

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
        // when image is scanned and processed
    }

    @Override
    public void onRetrievedFailed(String reason) {
        // in case of failure
    }
}
