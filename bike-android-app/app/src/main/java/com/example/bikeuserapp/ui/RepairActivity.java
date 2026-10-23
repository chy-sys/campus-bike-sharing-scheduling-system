package com.example.bikeuserapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bikeuserapp.R;
import com.example.bikeuserapp.util.ApiClient;
import com.example.bikeuserapp.util.SpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 故障报修页：提交报修工单、查看报修记录
 * 对应后端 /api/repairs、/api/bikes
 */
public class RepairActivity extends AppCompatActivity {

    private Spinner spBike;
    private Spinner spFaultType;
    private EditText etDescription;
    private Button btnSubmit;
    private ListView lvRepairs;

    private final List<RideActivity.Option> bikeList = new ArrayList<>();
    private final List<RideActivity.Option> faultTypes = new ArrayList<>();
    private final List<String> repairItems = new ArrayList<>();
    private ArrayAdapter<String> repairAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpUtil.init(this);
        setContentView(R.layout.activity_repair);

        spBike = findViewById(R.id.sp_bike);
        spFaultType = findViewById(R.id.sp_fault_type);
        etDescription = findViewById(R.id.et_description);
        btnSubmit = findViewById(R.id.btn_submit);
        lvRepairs = findViewById(R.id.lv_repairs);

        repairAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, repairItems);
        lvRepairs.setAdapter(repairAdapter);

        // 故障类型与后端 RepairOrder 注释一致：1刹车失灵 2轮胎损坏 3链条故障 4电量不足 5其他
        faultTypes.add(new RideActivity.Option(1, "刹车失灵"));
        faultTypes.add(new RideActivity.Option(2, "轮胎损坏"));
        faultTypes.add(new RideActivity.Option(3, "链条故障"));
        faultTypes.add(new RideActivity.Option(4, "电量不足"));
        faultTypes.add(new RideActivity.Option(5, "其他"));
        bindSpinner(spFaultType, faultTypes);

        btnSubmit.setOnClickListener(v -> submitRepair());

        loadBikes();
        loadRepairs();
    }

    /** 加载全部车辆供报修选择 */
    private void loadBikes() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/bikes?pageNum=1&pageSize=100"))
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
                            toast(res.optString("msg", "车辆加载失败"));
                            return;
                        }
                        JSONArray records = res.getJSONObject("data").getJSONArray("records");
                        bikeList.clear();
                        for (int i = 0; i < records.length(); i++) {
                            JSONObject bike = records.getJSONObject(i);
                            bikeList.add(new RideActivity.Option(
                                    bike.getLong("id"), bike.optString("bikenumber", "-")));
                        }
                        bindSpinner(spBike, bikeList);
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("车辆数据解析错误");
                    }
                });
            }
        });
    }

    /** 加载报修记录 */
    private void loadRepairs() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/repairs?pageNum=1&pageSize=20&mine=true"))
                .get()
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> toast("报修记录加载失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                handler.post(() -> {
                    try {
                        JSONObject res = new JSONObject(body);
                        if (res.getInt("code") != 200) {
                            toast(res.optString("msg", "报修记录加载失败"));
                            return;
                        }
                        JSONArray records = res.getJSONObject("data").getJSONArray("records");
                        repairItems.clear();
                        for (int i = 0; i < records.length(); i++) {
                            JSONObject order = records.getJSONObject(i);
                            int status = order.optInt("status", 0);
                            int faultType = order.optInt("faultType", 5);

                            String statusText;
                            switch (status) {
                                case 0: statusText = "待处理"; break;
                                case 1: statusText = "处理中"; break;
                                case 2: statusText = "已修复"; break;
                                default: statusText = "无法修复"; break;
                            }

                            repairItems.add("工单#" + order.getLong("id")
                                    + "  " + statusText
                                    + "\n车辆ID " + order.optLong("bikeId", 0)
                                    + " · " + faultTypeName(faultType)
                                    + "\n" + order.optString("description", ""));
                        }
                        repairAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("报修数据解析错误");
                    }
                });
            }
        });
    }

    /** 提交报修：POST /api/repairs */
    private void submitRepair() {
        RideActivity.Option bike = (RideActivity.Option) spBike.getSelectedItem();
        RideActivity.Option fault = (RideActivity.Option) spFaultType.getSelectedItem();
        String description = etDescription.getText().toString().trim();

        if (bike == null) {
            toast("请选择报修车辆");
            return;
        }
        if (fault == null) {
            toast("请选择故障类型");
            return;
        }
        if (description.isEmpty()) {
            toast("请填写故障描述");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("bikeId", bike.id);
            json.put("faultType", (int) fault.id);
            json.put("description", description);
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(ApiClient.url("/api/repairs"))
                    .post(body)
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
                            if (res.getInt("code") == 200) {
                                etDescription.setText("");
                                loadRepairs();
                                loadBikes();
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

    private String faultTypeName(int type) {
        switch (type) {
            case 1: return "刹车失灵";
            case 2: return "轮胎损坏";
            case 3: return "链条故障";
            case 4: return "电量不足";
            default: return "其他";
        }
    }

    private void bindSpinner(Spinner spinner, List<RideActivity.Option> options) {
        ArrayAdapter<RideActivity.Option> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
