package com.example.bikeuserapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bikeuserapp.MainActivity;
import com.example.bikeuserapp.R;
import com.example.bikeuserapp.util.SpUtil;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegPhone;
    private EditText etRegUsername;
    private EditText etRegPassword;
    private EditText etRegVerifyCode;
    private Button btnRegister;
    private TextView tvToLogin;

    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // 后端地址：真机调试填电脑的局域网 IP，模拟器用 10.0.2.2
    private static final String BASE_URL = "http://172.27.104.169:8001";

    private static final String REGISTER_URL = BASE_URL + "/auth/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegPhone = findViewById(R.id.et_reg_phone);
        etRegUsername = findViewById(R.id.et_reg_username);
        etRegPassword = findViewById(R.id.et_reg_password);
        etRegVerifyCode = findViewById(R.id.et_reg_verify_code);
        btnRegister = findViewById(R.id.btn_register);
        tvToLogin = findViewById(R.id.tv_to_login);

        btnRegister.setOnClickListener(v -> register());

        // 返回登录页
        tvToLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String phone = etRegPhone.getText().toString().trim();
        String username = etRegUsername.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String verifyCode = etRegVerifyCode.getText().toString().trim();

        // 非空校验
        if (phone.isEmpty()) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (verifyCode.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 组装注册参数，和后端RegisterRequest字段完全对应
        try {
            JSONObject json = new JSONObject();
            json.put("phone", phone);
            json.put("password", password);
            json.put("username", username);
            json.put("verifyCode", verifyCode);
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(REGISTER_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject res = new JSONObject(result);
                            int code = res.getInt("code");
                            String msg = res.getString("msg");

                            if (code == 200) {
                                // 注册成功，关闭当前页，回到登录页
                                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // 注册失败（手机号已存在、验证码错误等）
                                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "数据解析错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
