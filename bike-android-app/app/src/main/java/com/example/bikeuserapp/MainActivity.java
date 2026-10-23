package com.example.bikeuserapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bikeuserapp.ui.LoginActivity;
import com.example.bikeuserapp.util.SpUtil;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserInfo;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化SpUtil
        SpUtil.init(this);

        // 绑定控件
        tvUserInfo = findViewById(R.id.tv_user_info);
        btnLogout = findViewById(R.id.btn_logout);

        // 读取本地存储的用户信息，验证登录态
        String userName = SpUtil.getString(SpUtil.KEY_USER_NAME, "");
        String phone = SpUtil.getString(SpUtil.KEY_PHONE, "");
        tvUserInfo.setText("用户名：" + userName + "\n手机号：" + phone);

        // 退出登录
        btnLogout.setOnClickListener(v -> {
            SpUtil.clear();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        });
    }
}