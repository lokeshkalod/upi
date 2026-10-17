package com.helper.upibridge;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class MainActivity extends Activity {

    private String fullUpiUri = "";
    private String payeeVpa = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivQr = findViewById(R.id.ivQrCode);
        TextView tvAmount = findViewById(R.id.tvAmount);
        TextView tvUpiId = findViewById(R.id.tvUpiId);
        Button btnCopyUpi = findViewById(R.id.btnCopyUpi);
        Button btnCopyLink = findViewById(R.id.btnCopyLink);
        Button btnDone = findViewById(R.id.btnDone);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null && "upi".equalsIgnoreCase(uri.getScheme())) {
            fullUpiUri = uri.toString();
            payeeVpa = uri.getQueryParameter("pa");
            String amount = uri.getQueryParameter("am");

            tvAmount.setText("Amount: ₹" + (amount != null ? amount : "0.00"));
            tvUpiId.setText("UPI ID: " + (payeeVpa != null ? payeeVpa : "N/A"));

            try {
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix bitMatrix = writer.encode(fullUpiUri, BarcodeFormat.QR_CODE, 512, 512);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
                ivQr.setImageBitmap(bmp);
            } catch (Exception e) {
                Toast.makeText(this, "QR Code error", Toast.LENGTH_SHORT).show();
            }
        }

        btnCopyUpi.setOnClickListener(v -> {
            if (payeeVpa != null && !payeeVpa.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", payeeVpa));
                Toast.makeText(this, "UPI ID Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCopyLink.setOnClickListener(v -> {
            if (!fullUpiUri.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("UPI Link", fullUpiUri));
                Toast.makeText(this, "UPI Link Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDone.setOnClickListener(v -> {
            Intent res = new Intent();
            res.putExtra("response", "Status=SUCCESS&responseCode=00");
            setResult(Activity.RESULT_OK, res);
            finish();
        });
    }
}
