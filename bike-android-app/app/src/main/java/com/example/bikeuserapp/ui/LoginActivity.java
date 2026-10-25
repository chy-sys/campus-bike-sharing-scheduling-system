// 必须是文件第一行，声明这个类属于 ui 包
package com.example.bikeuserapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// R 类在主包下，子包需要显式导入
import com.example.bikeuserapp.MainActivity;
import com.example.bikeuserapp.R;
import com.example.bikeuserapp.util.SpUtil;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvToRegister;

    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // 后端接口地址：改成你电脑的局域网IP，不能用localhost
    // 后端地址：真机调试填电脑的局域网 IP，模拟器用 10.0.2.2
    private static final String BASE_URL = "http://172.27.104.169:8001";

    private static final String LOGIN_URL = BASE_URL + "/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SpUtil.init(LoginActivity.this);

        setContentView(R.layout.activity_login);

        // 绑定控件
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvToRegister = findViewById(R.id.tv_to_register);

        // 登录按钮点击
        btnLogin.setOnClickListener(v -> login());

        // 跳转到注册页：同包下的类直接用类名即可
        tvToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 非空校验
        if (phone.isEmpty()) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 组装JSON请求参数，和后端LoginRequest字段完全对应
        try {
            JSONObject json = new JSONObject();
            json.put("phone", phone);
            json.put("password", password);
            RequestBody body = RequestBody.create(json.toString(), JSON);

            // 构建POST请求
            Request request = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(body)
                    .build();

            // 异步发送请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 网络失败，切回主线程弹提示
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "网络连接失败，请检查IP和后端是否启动", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            // 解析后端返回的统一格式
                            JSONObject res = new JSONObject(result);
                            int code = res.getInt("code");
                            String msg = res.getString("msg");

                            if (code == 200) {
                                // 登录成功：解析data里的用户信息和token
                                JSONObject data = res.getJSONObject("data");
                                String token = data.getString("token");
                                long userId = data.getLong("userId");
                                String userName = data.getString("userName");
                                String phone = data.getString("phone");

                                // 存到本地登录信息
                                SpUtil.putString(SpUtil.KEY_TOKEN, token);
                                SpUtil.putString(SpUtil.KEY_USER_ID, String.valueOf(userId));
                                SpUtil.putString(SpUtil.KEY_USER_NAME, userName);
                                SpUtil.putString(SpUtil.KEY_PHONE, phone);
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                // 跳转到主页面
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish(); // 关闭登录页，按返回键不会回来
                            } else {
                                // 登录失败，显示后端返回的错误信息
                                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "数据解析错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
