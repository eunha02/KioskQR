package app.qr_kiosk;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.makeText;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText id;
    private EditText password;
    private Button login;
    private Button signup;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        id = findViewById(R.id.edit_id);
        password = findViewById(R.id.edit_password);
        login = findViewById(R.id.login_button);
        signup = findViewById(R.id.signup_button);

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String savedUserId = sharedPreferences.getString("UserId", "").trim();
                String savedPassword = sharedPreferences.getString("Password", "").trim();

                String inputUserId = id.getText().toString().trim();
                String inputPassword = password.getText().toString().trim();

                if (savedUserId.equals(inputUserId) && savedPassword.equals(inputPassword)) {
                    byte[] byteArray = getIntent().getByteArrayExtra("qrCodeBytes");
                    Intent intent = new Intent(LoginActivity.this, QRCodeDisplayActivity.class);
                    intent.putExtra("qrCodeBytes", byteArray);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "아이디나 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        signup.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
