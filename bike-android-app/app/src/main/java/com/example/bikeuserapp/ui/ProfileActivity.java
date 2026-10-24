package com.example.bikeuserapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bikeuserapp.R;
import com.example.bikeuserapp.util.ApiClient;
import com.example.bikeuserapp.util.SpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 个人中心：个人信息、虚拟账户余额、骑行记录与账单、修改资料与密码
 * 对应后端 /api/users、/api/orders
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfile;
    private EditText etUsername;
    private Button btnSaveProfile;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private Button btnChangePassword;
    private TextView tvOrderSummary;
    private ListView lvOrders;

    private final List<String> orderItems = new ArrayList<>();
    private ArrayAdapter<String> orderAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpUtil.init(this);
        setContentView(R.layout.activity_profile);

        tvProfile = findViewById(R.id.tv_profile);
        etUsername = findViewById(R.id.et_username);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
        tvOrderSummary = findViewById(R.id.tv_order_summary);
        lvOrders = findViewById(R.id.lv_orders);

        orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderItems);
        lvOrders.setAdapter(orderAdapter);

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());

        loadProfile();
        loadOrders();
    }

    /** 个人资料：GET /api/users/profile */
    private void loadProfile() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/users/profile"))
                .get()
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> toast("网络连接失败，请检查IP和后端是否启动"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                handler.post(() -> {
                    try {
                        JSONObject res = new JSONObject(body);
                        if (res.getInt("code") != 200) {
                            toast(res.optString("msg", "资料加载失败"));
                            return;
                        }
                        JSONObject user = res.getJSONObject("data");
                        String username = user.optString("username", "-");
                        String phone = user.optString("phone", "-");
                        int role = user.optInt("role", 0);
                        BigDecimal balance = new BigDecimal(user.optString("balance", "0"));

                        tvProfile.setText("用户名：" + username
                                + "\n手机号：" + phone
                                + "\n角色：" + roleName(role)
                                + "\n账户余额：" + balance.setScale(2, BigDecimal.ROUND_HALF_UP) + " 元");
                        etUsername.setText(username);
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("资料解析错误");
                    }
                });
            }
        });
    }

    /** 骑行记录与消费账单：GET /api/orders */
    private void loadOrders() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/orders?pageNum=1&pageSize=50"))
                .get()
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> toast("骑行记录加载失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                handler.post(() -> {
                    try {
                        JSONObject res = new JSONObject(body);
                        if (res.getInt("code") != 200) {
                            toast(res.optString("msg", "骑行记录加载失败"));
                            return;
                        }
                        JSONArray records = res.getJSONObject("data").getJSONArray("records");
                        orderItems.clear();
                        BigDecimal total = new BigDecimal("0");

                        for (int i = 0; i < records.length(); i++) {
                            JSONObject order = records.getJSONObject(i);
                            int status = order.optInt("status", -1);
                            BigDecimal amount = new BigDecimal(order.optString("amount", "0"));

                            String statusText;
                            if (status == 0) {
                                statusText = "骑行中";
                            } else if (status == 1) {
                                statusText = "已完成";
                                total = total.add(amount);
                            } else {
                                statusText = "已取消";
                            }

                            orderItems.add(order.optString("orderNo", "-") + "  " + statusText
                                    + "\n时长 " + order.optInt("duration", 0) + " 分钟"
                                    + " · 费用 " + amount.setScale(2, BigDecimal.ROUND_HALF_UP) + " 元");
                        }
                        orderAdapter.notifyDataSetChanged();
                        tvOrderSummary.setText("我的骑行记录（累计消费 "
                                + total.setScale(2, BigDecimal.ROUND_HALF_UP) + " 元）");
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("骑行记录解析错误");
                    }
                });
            }
        });
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            toast("请输入用户名");
            return;
        }
        sendPut("/api/users/profile", "{\"username\":\"" + escape(username) + "\"}", true);
    }

    private void changePassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            toast("请填写原密码与新密码");
            return;
        }
        String body = "{\"oldPassword\":\"" + escape(oldPassword)
                + "\",\"newPassword\":\"" + escape(newPassword) + "\"}";
        sendPut("/api/users/password", body, false);
    }

    private void sendPut(String path, String json, boolean isProfile) {
        try {
            RequestBody requestBody = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(ApiClient.url(path))
                    .put(requestBody)
                    .build();

            ApiClient.client().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handler.post(() -> toast("网络连接失败"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    handler.post(() -> {
                        try {
                            JSONObject res = new JSONObject(result);
                            toast(res.optString("msg", "操作完成"));
                            if (res.getInt("code") == 200 && !isProfile) {
                                etOldPassword.setText("");
                                etNewPassword.setText("");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            toast("数据解析错误");
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String roleName(int role) {
        switch (role) {
            case 1: return "运维人员";
            case 2: return "管理员";
            default: return "普通用户";
        }
    }

    /** JSON 字符串转义，避免引号导致请求体非法 */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
