package run.wallet.iota.helper;

/**
 * Created by coops on 23/12/17.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;

import run.wallet.R;
import run.wallet.common.B;


public class QR {
    public static final Bitmap generateImage(final String text, Activity activity){
        if(text==null || text.trim().isEmpty()){
            return null;
        }

        int size = 390;

        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.MARGIN, 1);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size,
                    size, hintMap);
            int height = byteMatrix.getHeight();
            int width = byteMatrix.getWidth();
            final Bitmap qrImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++){
                for (int y = 0; y < height; y++){
                    qrImage.setPixel(x, y, byteMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                }
            }
            Bitmap background= Bitmap.createScaledBitmap(((BitmapDrawable) B.getDrawable(activity, R.drawable.qr_bg)).getBitmap(),390,90,false);
            Bitmap bmOverlay = Bitmap.createBitmap(qrImage.getWidth(),qrImage.getHeight()+90, qrImage.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(qrImage, 0, 90, null);
            canvas.drawBitmap(background, 0, 0,null );


            return bmOverlay;
        } catch (WriterException e) {
            Log.e("QR.ex",""+e.getMessage());

        }

        return null;
    }

    public static void generateImage(final String text, final ImageView showView, Activity activity){

        if(text.trim().isEmpty()){
            //alert("Ketik dulu data yang ingin dibuat QR Code");
            return;
        }

        //endEditing();
        //showLoadingVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = showView.getMeasuredWidth();
                if( size > 1){
                    //Log.e("QR", "size is set manually");
                    size = 260;
                }

                Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
                hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                hintMap.put(EncodeHintType.MARGIN, 1);
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                try {
                    BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size,
                            size, hintMap);
                    int height = byteMatrix.getHeight();
                    int width = byteMatrix.getWidth();
                    final Bitmap qrImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++){
                        for (int y = 0; y < height; y++){
                            qrImage.setPixel(x, y, byteMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showView.setImageBitmap(qrImage);
                            //activity.showImage(self.qrImage);
                            //self.showLoadingVisible(false);
                            //self.snackbar("QRCode telah dibuat");
                        }
                    });
                } catch (WriterException e) {
                    Log.e("QR.ex",""+e.getMessage());

                }
            }
        }).start();
    }
}
