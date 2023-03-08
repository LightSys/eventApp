package org.lightsys.eventApp.tools.qr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

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

public class launchQRScanner extends AppCompatActivity implements BarcodeRetriever {

    private static final String QR_DATA_EXTRA = "qr_data";
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscan_layout);

        initiatePopupWindow();

        BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);

        barcodeCapture.setRetrieval(launchQRScanner.this);
//        Log.d("launchQRScanner", ": " + );

    }

    // for one time scan
    @Override
    public void onRetrieved(final Barcode barcode) {
        Log.d("launchQRScanner", "Barcode read: " + barcode.displayValue);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(QR_DATA_EXTRA, barcode.displayValue);
                setResult(Activity.RESULT_OK, resultIntent);
                dialog.dismiss();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(launchQRScanner.this)
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

    private void initiatePopupWindow() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.TOP;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

            wlp.y = Math.round(400*getResources().getDisplayMetrics().density/2);
            window.setAttributes(wlp);
        }
        dialog.show();
    }

    @Override
    protected void onDestroy(){
        try {
            super.onDestroy();
        } catch (Exception e) {
            super.finishAndRemoveTask();
        }

    }

}
