package run.wallet.iota.helper;

/**
 * Created by coops on 23/12/17.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;


public class QR {

    private Bitmap qrImage;
    public static void generateImage(final String text, final Runnable callback, Activity activity){

        if(text.trim().isEmpty()){
            //alert("Ketik dulu data yang ingin dibuat QR Code");
            return;
        }

        //endEditing();
        //showLoadingVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = 260;
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
                            callback.run();

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
    public static final Bitmap generateImage(final String text, Activity activity){

        if(text.trim().isEmpty()){
            //alert("Ketik dulu data yang ingin dibuat QR Code");
            return null;
        }

        int size = 260;
        if( size > 1){
            Log.e("QR", "size is set manually");
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

            return qrImage;
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
                    Log.e("QR", "size is set manually");
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
