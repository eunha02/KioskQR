package app.qr_kiosk;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.makeText;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {
    private EditText id;
    private EditText password;
    private EditText name;
    private Spinner gender;
    private Spinner role;
    private TextView birth;
    private TextView birthText;
    private Button signup;
    private Spinner branch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(new Locale("ko"));
        setContentView(R.layout.signup);

        id = findViewById(R.id.edit_id2);
        password = findViewById(R.id.edit_password2);
        name = findViewById(R.id.edit_name);
        gender = findViewById(R.id.gender);
        role = findViewById(R.id.role);
        birth = findViewById(R.id.birth);
        birthText = findViewById(R.id.birthText);
        signup = findViewById(R.id.signup);
        branch = findViewById(R.id.branch);

        birth.setOnClickListener(v -> datepicker());
        birthText.setOnClickListener(v -> datepicker());

        role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 1) {
                    showAdminCodeDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        signup.setOnClickListener(view -> processSignup());
    }

    private void datepicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                (datePicker, i, i1, i2) -> {
                    String monthStr = (i1 + 1 < 10) ? "0" + (i1 + 1) : String.valueOf(i1 + 1);
                    String dayStr = (i2 < 10) ? "0" + i2 : String.valueOf(i2);
                    birthText.setText(i + "-" + monthStr + "-" + dayStr);
                },
                year, month, day);

        datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        datePickerDialog.show();
    }

    private void showAdminCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);
        builder.setView(dialogView);

        EditText adminCodeInput = dialogView.findViewById(R.id.editTextText);
        builder.setPositiveButton("인증", null);
        builder.setNegativeButton("취소", (dialog, which) -> role.setSelection(0));
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String adminCode = adminCodeInput.getText().toString();
                if ("kiosk".equals(adminCode)) {
                    Toast.makeText(this, "인증이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "다시 인증해주세요.", Toast.LENGTH_SHORT).show();
                    role.setSelection(0);
                }
            });
        });
        dialog.show();
    }

    private void processSignup() {
        if (id.getText().toString().isEmpty() || password.getText().toString().isEmpty() || "선택".equals(birthText.getText().toString())) {
            Toast.makeText(this, "내용을 모두 입력하지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserId", id.getText().toString());
        editor.putString("Password", password.getText().toString());
        editor.apply();

        String birthDate = birthText.getText().toString().replace("-", "");
        String genderCode = gender.getSelectedItemPosition() == 0 ? "2" : "3"; // 성별 코드 (남성: 2, 여성: 3)
        String roleCode = role.getSelectedItemPosition() == 0 ? "0" : "1"; // 역할 코드 (고객: 0, 관리자: 1)
        String branchCode = branch.getSelectedItem().toString();
        String qrCodeData = id.getText().toString() + "," + birthDate + "," + genderCode + "," + roleCode + "," + branchCode;

        try {
            Bitmap qrBitmap = encodeAsBitmap(qrCodeData);
            if (qrBitmap != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                editor.putString("EncodedByteArray", encodedString);
                editor.apply();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("qrCodeBytes", byteArray);
                startActivity(intent);
                Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "QR 코드 생성에 실패했습니다. 입력 값을 확인해주세요.", Toast.LENGTH_LONG).show();
            }

        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR Code: ", e);
            Toast.makeText(this, "QR 코드 생성 중 에러 발생.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap encodeAsBitmap(String data) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 500, 500, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }
}
