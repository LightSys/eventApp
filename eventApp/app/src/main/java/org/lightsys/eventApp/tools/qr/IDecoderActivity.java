package org.lightsys.eventApp.tools.qr;

import android.graphics.Bitmap;
import android.os.Handler;

import org.lightsys.eventApp.tools.qr.camera.CameraManager;
import com.google.zxing.Result;

interface IDecoderActivity {

    ViewfinderView getViewfinder();

    Handler getHandler();

    CameraManager getCameraManager();

    void handleDecode(Result rawResult, Bitmap barcode);
}
