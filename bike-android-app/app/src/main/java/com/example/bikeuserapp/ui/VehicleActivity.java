package com.example.bikeuserapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bikeuserapp.R;
import com.example.bikeuserapp.util.ApiClient;
import com.example.bikeuserapp.util.SpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 车辆管理页：按状态/编号查询车辆，并可变更车辆状态
 * 对应后端 /api/bikes
 */
public class VehicleActivity extends AppCompatActivity {

    /** 车辆状态，与后端 Constants 一致：0故障 1可用 2使用中 3维修中 */
    private static final int STATUS_FAULT = 0;
    private static final int STATUS_AVAILABLE = 1;
    private static final int STATUS_IN_USE = 2;
    private static final int STATUS_REPAIRING = 3;

    private Spinner spStatus;
    private EditText etKeyword;
    private Button btnSearch;
    private TextView tvSummary;
    private ListView lvBikes;

    private final List<RideActivity.Option> statusList = new ArrayList<>();
    private final List<BikeItem> bikeList = new ArrayList<>();
    private ArrayAdapter<BikeItem> bikeAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpUtil.init(this);
        setContentView(R.layout.activity_vehicle);

        spStatus = findViewById(R.id.sp_status);
        etKeyword = findViewById(R.id.et_keyword);
        btnSearch = findViewById(R.id.btn_search);
        tvSummary = findViewById(R.id.tv_summary);
        lvBikes = findViewById(R.id.lv_bikes);

        statusList.add(new RideActivity.Option(-1, "全部状态"));
        statusList.add(new RideActivity.Option(STATUS_AVAILABLE, "可用"));
        statusList.add(new RideActivity.Option(STATUS_IN_USE, "使用中"));
        statusList.add(new RideActivity.Option(STATUS_FAULT, "故障"));
        statusList.add(new RideActivity.Option(STATUS_REPAIRING, "维修中"));
        bindSpinner(spStatus, statusList);

        bikeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bikeList);
        lvBikes.setAdapter(bikeAdapter);

        btnSearch.setOnClickListener(v -> loadBikes());
        spStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view,
                                       int position, long id) {
                loadBikes();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // 点击车辆变更状态
        lvBikes.setOnItemClickListener((parent, view, position, id) -> showStatusDialog(bikeList.get(position)));

        loadBikes();
    }

    private void loadBikes() {
        StringBuilder path = new StringBuilder("/api/bikes?pageNum=1&pageSize=100");
        RideActivity.Option status = (RideActivity.Option) spStatus.getSelectedItem();
        if (status != null && status.id >= 0) {
            path.append("&status=").append(status.id);
        }
        String keyword = etKeyword.getText().toString().trim();
        if (!keyword.isEmpty()) {
            try {
                path.append("&keyword=").append(URLEncoder.encode(keyword, "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Request request = new Request.Builder()
                .url(ApiClient.url(path.toString()))
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
                            toast(res.optString("msg", "车辆查询失败"));
                            return;
                        }
                        JSONObject data = res.getJSONObject("data");
                        JSONArray records = data.getJSONArray("records");
                        bikeList.clear();
                        for (int i = 0; i < records.length(); i++) {
                            JSONObject bike = records.getJSONObject(i);
                            long bikeId = bike.getLong("id");
                            String number = bike.optString("bikenumber", "-");
                            int battery = bike.optInt("battery", 0);
                            int status = bike.optInt("status", 0);
                            long stationId = bike.optLong("stationId", 0L);

                            String text = number + "  " + statusName(status)
                                    + "\n电量 " + battery + "% · 所在站点ID " + stationId;
                            bikeList.add(new BikeItem(bikeId, text));
                        }
                        bikeAdapter.notifyDataSetChanged();
                        tvSummary.setText("共 " + data.optInt("total", bikeList.size()) + " 辆");
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("车辆数据解析错误");
                    }
                });
            }
        });
    }

    private void showStatusDialog(BikeItem item) {
        String[] actions = {"设为可用", "标记故障", "送修（维修中）"};
        final int[] values = {STATUS_AVAILABLE, STATUS_FAULT, STATUS_REPAIRING};

        new AlertDialog.Builder(this)
                .setTitle("变更车辆状态")
                .setItems(actions, (dialog, which) -> updateStatus(item.id, values[which]))
                .show();
    }

    private void updateStatus(long bikeId, int status) {
        RequestBody emptyBody = RequestBody.create(new byte[0], (MediaType) null);
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/bikes/" + bikeId + "/status?status=" + status))
                .put(emptyBody)
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> toast("网络连接失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                handler.post(() -> {
                    try {
                        JSONObject res = new JSONObject(body);
                        toast(res.optString("msg", "操作完成"));
                        loadBikes();
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("数据解析错误");
                    }
                });
            }
        });
    }

    private String statusName(int status) {
        switch (status) {
            case STATUS_AVAILABLE: return "可用";
            case STATUS_IN_USE: return "使用中";
            case STATUS_REPAIRING: return "维修中";
            default: return "故障";
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

    /** 列表项：显示文本 + 车辆ID */
    static class BikeItem {
        final long id;
        final String text;

        BikeItem(long id, String text) {
            this.id = id;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
