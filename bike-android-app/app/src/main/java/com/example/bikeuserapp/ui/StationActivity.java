package com.example.bikeuserapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bikeuserapp.R;
import com.example.bikeuserapp.util.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 站点列表：GET /api/stations/all
 */
public class StationActivity extends AppCompatActivity {

    private ListView lvStations;
    private final List<String> items = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        lvStations = findViewById(R.id.lv_stations);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lvStations.setAdapter(adapter);

        loadStations();
    }

    private void loadStations() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/stations/all"))
                .get()
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> Toast.makeText(StationActivity.this,
                        "网络连接失败，请检查IP和后端是否启动", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                handler.post(() -> {
                    try {
                        JSONObject res = new JSONObject(body);
                        int code = res.getInt("code");
                        if (code == 401) {
                            Toast.makeText(StationActivity.this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (code != 200) {
                            Toast.makeText(StationActivity.this,
                                    res.optString("msg", "查询失败"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray data = res.getJSONArray("data");
                        items.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject station = data.getJSONObject(i);
                            String name = station.optString("name", "-");
                            String area = station.optString("area", "-");
                            int current = station.optInt("currentCount", 0);
                            int capacity = station.optInt("capacity", 0);
                            items.add(name + "（" + area + "）\n在站 " + current + " / 容量 " + capacity);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(StationActivity.this, "数据解析错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
