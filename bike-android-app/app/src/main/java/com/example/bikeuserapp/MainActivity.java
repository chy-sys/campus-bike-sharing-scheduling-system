package com.example.bikeuserapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bikeuserapp.ui.DispatchActivity;
import com.example.bikeuserapp.ui.DispatchTaskActivity;
import com.example.bikeuserapp.ui.LoginActivity;
import com.example.bikeuserapp.ui.RepairActivity;
import com.example.bikeuserapp.ui.RideActivity;
import com.example.bikeuserapp.ui.StationActivity;
import com.example.bikeuserapp.ui.VehicleActivity;
import com.example.bikeuserapp.util.SpUtil;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserInfo;
    private Button btnRide;
    private Button btnRepair;
    private Button btnVehicle;
    private Button btnStations;
    private Button btnDispatch;
    private Button btnDispatchTask;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化SpUtil
        SpUtil.init(this);

        // 绑定控件
        tvUserInfo = findViewById(R.id.tv_user_info);
        btnRide = findViewById(R.id.btn_ride);
        btnRepair = findViewById(R.id.btn_repair);
        btnVehicle = findViewById(R.id.btn_vehicle);
        btnStations = findViewById(R.id.btn_stations);
        btnDispatch = findViewById(R.id.btn_dispatch);
        btnDispatchTask = findViewById(R.id.btn_dispatch_task);
        btnLogout = findViewById(R.id.btn_logout);

        // 读取本地存储的用户信息，验证登录态
        String userName = SpUtil.getString(SpUtil.KEY_USER_NAME, "");
        String phone = SpUtil.getString(SpUtil.KEY_PHONE, "");
        tvUserInfo.setText("用户名：" + userName + "\n手机号：" + phone);

        // 骑行用车（开锁 / 还车）
        btnRide.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RideActivity.class)));

        // 故障报修
        btnRepair.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RepairActivity.class)));

        // 站点列表
        btnStations.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, StationActivity.class)));

        // 车辆管理（查询与状态变更）
        btnVehicle.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, VehicleActivity.class)));

        // 调度建议
        btnDispatch.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DispatchActivity.class)));

        // 调度任务（创建与推进）
        btnDispatchTask.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DispatchTaskActivity.class)));

        // 退出登录
        btnLogout.setOnClickListener(v -> {
            SpUtil.clear();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        });
    }
}
