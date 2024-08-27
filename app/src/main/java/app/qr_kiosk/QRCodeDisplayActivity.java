package app.qr_kiosk;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class QRCodeDisplayActivity extends AppCompatActivity {
    Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_display);

        logout = findViewById(R.id.logout_button);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String encodedString = sharedPreferences.getString("EncodedByteArray", null);
        if (encodedString != null) {
            byte[] byteArray =  Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap qrBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            ImageView imageViewQRCode = findViewById(R.id.imageViewQRCode);
            imageViewQRCode.setImageBitmap(qrBitmap);
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QRCodeDisplayActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}