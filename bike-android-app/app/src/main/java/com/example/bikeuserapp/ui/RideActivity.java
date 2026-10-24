package com.example.bikeuserapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bikeuserapp.R;
import com.example.bikeuserapp.util.ApiClient;
import com.example.bikeuserapp.util.SpUtil;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

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
 * 骑行用车页：开锁用车、结束骑行（还车结算）、我的订单
 * 对应后端 /api/orders、/api/bikes、/api/stations
 */
public class RideActivity extends AppCompatActivity {

    private TextView tvCurrentOrder;
    private Spinner spBike;
    private Spinner spStartStation;
    private Spinner spEndStation;
    private Button btnScan;
    private Button btnStart;
    private Button btnEnd;
    private ListView lvOrders;

    private static final int REQUEST_CAMERA = 1001;

    /** 扫码结果回调：解析二维码中的车辆编号并开锁 */
    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    toast("已取消扫码");
                    return;
                }
                String content = result.getContents().trim();
                // 二维码内容支持 SMOKE001 或 bike:SMOKE001 两种格式
                String bikeNumber = content.contains(":")
                        ? content.substring(content.lastIndexOf(':') + 1)
                        : content;
                startRideByBikeNumber(bikeNumber);
            });

    private final List<Option> bikeList = new ArrayList<>();
    private final List<Option> stationList = new ArrayList<>();
    private final List<String> orderItems = new ArrayList<>();
    private ArrayAdapter<String> orderAdapter;

    /** 当前进行中的订单，为空表示未在骑行 */
    private Long ridingOrderId = null;
    private String ridingOrderNo = "";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpUtil.init(this);
        setContentView(R.layout.activity_ride);

        btnScan = findViewById(R.id.btn_scan);
        tvCurrentOrder = findViewById(R.id.tv_current_order);
        spBike = findViewById(R.id.sp_bike);
        spStartStation = findViewById(R.id.sp_start_station);
        spEndStation = findViewById(R.id.sp_end_station);
        btnStart = findViewById(R.id.btn_start);
        btnEnd = findViewById(R.id.btn_end);
        lvOrders = findViewById(R.id.lv_orders);

        orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderItems);
        lvOrders.setAdapter(orderAdapter);

        btnScan.setOnClickListener(v -> scanBike());
        btnStart.setOnClickListener(v -> startRide());
        btnEnd.setOnClickListener(v -> endRide());

        loadStations();
        loadBikes();
        loadOrders();
    }

    /** 加载站点，用于取车/还车下拉选择 */
    private void loadStations() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/stations/all"))
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
                            toast(res.optString("msg", "站点加载失败"));
                            return;
                        }
                        JSONArray data = res.getJSONArray("data");
                        stationList.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject station = data.getJSONObject(i);
                            stationList.add(new Option(station.getLong("id"), station.optString("name", "-")));
                        }
                        bindSpinner(spStartStation, stationList);
                        bindSpinner(spEndStation, stationList);
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("站点数据解析错误");
                    }
                });
            }
        });
    }

    /** 加载可用车辆（status=1） */
    private void loadBikes() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/bikes?pageNum=1&pageSize=100&status=1"))
                .get()
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> toast("车辆加载失败"));
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
                            String number = bike.optString("bikenumber", "-");
                            int battery = bike.optInt("battery", 0);
                            bikeList.add(new Option(bike.getLong("id"), number + "（电量 " + battery + "%）"));
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

    /** 加载我的订单，同时判断是否存在进行中的订单 */
    private void loadOrders() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/orders?pageNum=1&pageSize=20"))
                .get()
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> toast("订单加载失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                handler.post(() -> {
                    try {
                        JSONObject res = new JSONObject(body);
                        if (res.getInt("code") != 200) {
                            toast(res.optString("msg", "订单加载失败"));
                            return;
                        }
                        JSONArray records = res.getJSONObject("data").getJSONArray("records");
                        orderItems.clear();
                        ridingOrderId = null;
                        ridingOrderNo = "";

                        for (int i = 0; i < records.length(); i++) {
                            JSONObject order = records.getJSONObject(i);
                            long id = order.getLong("id");
                            String orderNo = order.optString("orderNo", "-");
                            int status = order.optInt("status", -1);

                            // status 0=骑行中，取第一条作为当前进行中订单
                            if (status == 0 && ridingOrderId == null) {
                                ridingOrderId = id;
                                ridingOrderNo = orderNo;
                            }

                            String statusText;
                            if (status == 0) {
                                statusText = "骑行中";
                            } else if (status == 1) {
                                statusText = "已完成";
                            } else {
                                statusText = "已取消";
                            }

                            orderItems.add(orderNo + "  " + statusText
                                    + "\n金额 " + order.optString("amount", "0") + " 元"
                                    + " · 时长 " + order.optInt("duration", 0) + " 分钟");
                        }
                        orderAdapter.notifyDataSetChanged();
                        updateCurrentOrder();
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("订单数据解析错误");
                    }
                });
            }
        });
    }

    /** 开锁用车：POST /api/orders/start */
    private void startRide() {
        Option bike = (Option) spBike.getSelectedItem();
        Option station = (Option) spStartStation.getSelectedItem();
        if (bike == null) {
            toast("请先选择车辆");
            return;
        }
        if (station == null) {
            toast("请先选择取车站点");
            return;
        }
        doStartRide(bike.id, station.id);
    }

    /** 扫码开锁：按二维码中的车辆编号查询车辆后开锁 */
    private void scanBike() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return;
        }
        ScanOptions options = new ScanOptions();
        options.setPrompt("对准车身二维码");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        scanLauncher.launch(options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanBike();
            } else {
                toast("需要相机权限才能扫码");
            }
        }
    }

    private void startRideByBikeNumber(String bikeNumber) {
        String path;
        try {
            path = "/api/bikes?pageNum=1&pageSize=10&keyword=" + URLEncoder.encode(bikeNumber, "UTF-8");
        } catch (Exception e) {
            path = "/api/bikes?pageNum=1&pageSize=10";
        }

        Request request = new Request.Builder()
                .url(ApiClient.url(path))
                .get()
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
                        if (res.getInt("code") != 200) {
                            toast(res.optString("msg", "车辆查询失败"));
                            return;
                        }
                        JSONArray records = res.getJSONObject("data").getJSONArray("records");
                        if (records.length() == 0) {
                            toast("未找到二维码对应的车辆：" + bikeNumber);
                            return;
                        }
                        JSONObject bike = records.getJSONObject(0);
                        if (bike.optInt("status", 0) != 1) {
                            toast("该车辆当前不可使用");
                            return;
                        }
                        long bikeId = bike.getLong("id");
                        long stationId = bike.optLong("stationId", 0L);
                        if (stationId == 0L) {
                            Option selected = (Option) spStartStation.getSelectedItem();
                            if (selected == null) {
                                toast("该车辆未绑定站点，请先选择取车站点");
                                return;
                            }
                            stationId = selected.id;
                        }
                        doStartRide(bikeId, stationId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("车辆数据解析错误");
                    }
                });
            }
        });
    }

    /** 开锁用车：POST /api/orders/start */
    private void doStartRide(long bikeId, long stationId) {
        try {
            JSONObject json = new JSONObject();
            json.put("bikeId", bikeId);
            json.put("startStationId", stationId);
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(ApiClient.url("/api/orders/start"))
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
                                loadOrders();
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

    /** 结束骑行并还车：PUT /api/orders/{id}/end */
    private void endRide() {
        if (ridingOrderId == null) {
            toast("当前没有进行中的订单");
            return;
        }
        Option station = (Option) spEndStation.getSelectedItem();
        if (station == null) {
            toast("请选择还车站点");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("endStationId", station.id);
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(ApiClient.url("/api/orders/" + ridingOrderId + "/end"))
                    .put(body)
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
                                loadOrders();
                                loadBikes();
                                loadStations();
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

    /** 根据是否有进行中订单，刷新提示与按钮状态 */
    private void updateCurrentOrder() {
        if (ridingOrderId == null) {
            tvCurrentOrder.setText("当前状态：未骑行，请选择车辆后开锁用车");
            btnStart.setEnabled(true);
            btnEnd.setEnabled(false);
        } else {
            tvCurrentOrder.setText("骑行中：订单号 " + ridingOrderNo
                    + "\n请选择还车站点后结束骑行");
            btnStart.setEnabled(false);
            btnEnd.setEnabled(true);
        }
    }

    private void bindSpinner(Spinner spinner, List<Option> options) {
        ArrayAdapter<Option> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /** 下拉选项：显示名称 + 携带后端ID */
    static class Option {
        final long id;
        final String name;

        Option(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
