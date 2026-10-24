package com.example.bikeuserapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bikeuserapp.R;
import com.example.bikeuserapp.util.ApiClient;
import com.example.bikeuserapp.util.SpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 地图页：WebView 加载高德地图展示站点分布，并按距离列出附近站点
 * 对应后端 /api/stations/all
 */
public class MapActivity extends AppCompatActivity {

    private WebView webMap;
    private TextView tvMapTitle;
    private ListView lvNearby;

    private final List<StationItem> stations = new ArrayList<>();
    private final List<String> stationTexts = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    /** 站点数据的 JSON，供地图页面打点 */
    private String stationsJson = "[]";
    private double myLatitude = 0;
    private double myLongitude = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int REQUEST_LOCATION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpUtil.init(this);
        setContentView(R.layout.activity_map);

        webMap = findViewById(R.id.web_map);
        tvMapTitle = findViewById(R.id.tv_map_title);
        lvNearby = findViewById(R.id.lv_nearby);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stationTexts);
        lvNearby.setAdapter(adapter);

        setupWebView();
        requestLocationOrLoad();
        loadStations();
    }

    private void setupWebView() {
        WebSettings settings = webMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String key = getString(R.string.amap_js_key);
                view.evaluateJavascript("initMap('" + key + "',"
                        + myLongitude + "," + myLatitude + ")", null);
                view.evaluateJavascript("setStations(" + stationsJson + ")", null);
            }
        });
        webMap.loadUrl("file:///android_asset/map.html");
    }

    private void requestLocationOrLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        locate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locate();
            } else {
                toast("未授予定位权限，将按站点数据展示");
                refreshStations();
            }
        }
    }

    /** 获取最后已知位置，用于按距离排序 */
    private void locate() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = null;
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        if (location != null) {
            myLatitude = location.getLatitude();
            myLongitude = location.getLongitude();
        }
        refreshStations();
    }

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
                        stations.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject station = data.getJSONObject(i);
                            StationItem item = new StationItem();
                            item.id = station.getLong("id");
                            item.name = station.optString("name", "-");
                            item.lng = station.optDouble("longitude", 0);
                            item.lat = station.optDouble("latitude", 0);
                            item.current = station.optInt("currentCount", 0);
                            item.capacity = station.optInt("capacity", 0);
                            stations.add(item);
                        }
                        refreshStations();
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("站点数据解析错误");
                    }
                });
            }
        });
    }

    /** 按距离排序并刷新列表与地图打点 */
    private void refreshStations() {
        boolean hasLocation = myLatitude != 0 && myLongitude != 0;
        if (hasLocation) {
            for (StationItem item : stations) {
                item.distance = distance(myLatitude, myLongitude, item.lat, item.lng);
            }
            Collections.sort(stations, Comparator.comparingDouble(s -> s.distance));
        }

        stationTexts.clear();
        for (StationItem item : stations) {
            String rate = item.capacity > 0
                    ? Math.round(item.current * 100.0 / item.capacity) + "%" : "-";
            String distanceText = hasLocation
                    ? "  约 " + (int) item.distance + " m" : "";
            stationTexts.add(item.name + distanceText
                    + "\n在站 " + item.current + " / 容量 " + item.capacity + " · 负荷率 " + rate);
        }
        adapter.notifyDataSetChanged();
        tvMapTitle.setText(hasLocation ? "附近站点（按距离由近到远）" : "站点列表");

        // 构造给地图页面的数据
        try {
            JSONArray array = new JSONArray();
            for (StationItem item : stations) {
                JSONObject json = new JSONObject();
                json.put("name", item.name);
                json.put("lng", item.lng);
                json.put("lat", item.lat);
                json.put("currentCount", item.current);
                json.put("capacity", item.capacity);
                array.put(json);
            }
            stationsJson = array.toString();
            webMap.evaluateJavascript("setStations(" + stationsJson + ")", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 球面距离（米） */
    private double distance(double lat1, double lng1, double lat2, double lng2) {
        double radius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /** 站点数据项 */
    static class StationItem {
        long id;
        String name;
        double lng;
        double lat;
        int current;
        int capacity;
        double distance;
    }
}
