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

import androidx.appcompat.app.AlertDialog;
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
 * 调度任务页：查看调度建议、创建调度任务、推进任务状态（开始/完成/取消）
 * 对应后端 /api/dispatch
 */
public class DispatchTaskActivity extends AppCompatActivity {

    /** 任务状态：0待调度 1进行中 2已完成 3已取消 */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_RUNNING = 1;

    private Spinner spSuggestion;
    private Button btnUseSuggestion;
    private Spinner spFrom;
    private Spinner spTo;
    private EditText etCount;
    private Button btnCreate;
    private ListView lvTasks;

    private final List<RideActivity.Option> stationList = new ArrayList<>();
    private final List<Suggestion> suggestionList = new ArrayList<>();
    private final List<TaskItem> taskList = new ArrayList<>();
    private ArrayAdapter<TaskItem> taskAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpUtil.init(this);
        setContentView(R.layout.activity_dispatch_task);

        spSuggestion = findViewById(R.id.sp_suggestion);
        btnUseSuggestion = findViewById(R.id.btn_use_suggestion);
        spFrom = findViewById(R.id.sp_from);
        spTo = findViewById(R.id.sp_to);
        etCount = findViewById(R.id.et_count);
        btnCreate = findViewById(R.id.btn_create);
        lvTasks = findViewById(R.id.lv_tasks);

        taskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        lvTasks.setAdapter(taskAdapter);

        btnUseSuggestion.setOnClickListener(v -> useSuggestion());
        btnCreate.setOnClickListener(v -> createTask());
        lvTasks.setOnItemClickListener((parent, view, position, id) -> showActionDialog(taskList.get(position)));

        loadStations();
        loadSuggestions();
        loadTasks();
    }

    /** 站点列表，用于创建任务时选择 */
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
                            return;
                        }
                        JSONArray data = res.getJSONArray("data");
                        stationList.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject station = data.getJSONObject(i);
                            stationList.add(new RideActivity.Option(
                                    station.getLong("id"), station.optString("name", "-")));
                        }
                        bindSpinner(spFrom, stationList);
                        bindSpinner(spTo, stationList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    /** 调度建议：GET /api/dispatch/suggest */
    private void loadSuggestions() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/dispatch/suggest"))
                .get()
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> toast("调度建议加载失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                handler.post(() -> {
                    try {
                        JSONObject res = new JSONObject(body);
                        if (res.getInt("code") != 200) {
                            return;
                        }
                        JSONArray data = res.getJSONArray("data");
                        suggestionList.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject s = data.getJSONObject(i);
                            String text = s.optString("fromStationName", "-")
                                    + " → " + s.optString("toStationName", "-")
                                    + "  调 " + s.optInt("bikeCount", 0) + " 辆";
                            suggestionList.add(new Suggestion(
                                    s.getLong("fromStationId"),
                                    s.getLong("toStationId"),
                                    s.optInt("bikeCount", 0),
                                    text));
                        }
                        if (suggestionList.isEmpty()) {
                            suggestionList.add(new Suggestion(0, 0, 0, "当前无需调度"));
                        }
                        ArrayAdapter<Suggestion> adapter = new ArrayAdapter<>(DispatchTaskActivity.this,
                                android.R.layout.simple_spinner_item, suggestionList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spSuggestion.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    /** 把选中的建议填入下方表单 */
    private void useSuggestion() {
        Suggestion suggestion = (Suggestion) spSuggestion.getSelectedItem();
        if (suggestion == null || suggestion.bikeCount <= 0) {
            toast("当前没有可用的调度建议");
            return;
        }
        selectOption(spFrom, suggestion.fromStationId);
        selectOption(spTo, suggestion.toStationId);
        etCount.setText(String.valueOf(suggestion.bikeCount));
        toast("已填充表单，确认无误后点击创建");
    }

    /** 创建调度任务：POST /api/dispatch/tasks */
    private void createTask() {
        RideActivity.Option from = (RideActivity.Option) spFrom.getSelectedItem();
        RideActivity.Option to = (RideActivity.Option) spTo.getSelectedItem();
        String countText = etCount.getText().toString().trim();

        if (from == null || to == null) {
            toast("请选择调出与调入站点");
            return;
        }
        if (countText.isEmpty()) {
            toast("请输入调度车辆数");
            return;
        }
        if (from.id == to.id) {
            toast("调出站点与调入站点不能相同");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("fromStationId", from.id);
            json.put("toStationId", to.id);
            json.put("bikeCount", Integer.parseInt(countText));
            json.put("remark", "App端创建");
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(ApiClient.url("/api/dispatch/tasks"))
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
                                etCount.setText("");
                                loadTasks();
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

    /** 任务列表：GET /api/dispatch/tasks */
    private void loadTasks() {
        Request request = new Request.Builder()
                .url(ApiClient.url("/api/dispatch/tasks?pageNum=1&pageSize=20"))
                .get()
                .build();

        ApiClient.client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> toast("任务列表加载失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                handler.post(() -> {
                    try {
                        JSONObject res = new JSONObject(body);
                        if (res.getInt("code") != 200) {
                            toast(res.optString("msg", "任务列表加载失败"));
                            return;
                        }
                        JSONArray records = res.getJSONObject("data").getJSONArray("records");
                        taskList.clear();
                        for (int i = 0; i < records.length(); i++) {
                            JSONObject task = records.getJSONObject(i);
                            long id = task.getLong("id");
                            int status = task.optInt("status", 0);
                            String text = "#" + id + "  "
                                    + stationName(task.optLong("fromStationId", 0L))
                                    + " → " + stationName(task.optLong("toStationId", 0L))
                                    + "\n" + task.optInt("bikeCount", 0) + " 辆 · "
                                    + statusName(status);
                            taskList.add(new TaskItem(id, status, text));
                        }
                        taskAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("任务数据解析错误");
                    }
                });
            }
        });
    }

    private void showActionDialog(TaskItem item) {
        String[] actions;
        final String[] paths;
        if (item.status == STATUS_PENDING) {
            actions = new String[]{"开始调度", "取消任务"};
            paths = new String[]{"/start", "/cancel"};
        } else if (item.status == STATUS_RUNNING) {
            actions = new String[]{"完成调度", "取消任务"};
            paths = new String[]{"/finish", "/cancel"};
        } else {
            toast("该任务已结束，无法继续操作");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("任务 #" + item.id)
                .setItems(actions, (dialog, which) ->
                        sendAction("/api/dispatch/tasks/" + item.id + paths[which]))
                .show();
    }

    private void sendAction(String path) {
        RequestBody emptyBody = RequestBody.create(new byte[0], (MediaType) null);
        Request request = new Request.Builder()
                .url(ApiClient.url(path))
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
                        loadTasks();
                        loadStations();
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("数据解析错误");
                    }
                });
            }
        });
    }

    private void selectOption(Spinner spinner, long id) {
        for (int i = 0; i < stationList.size(); i++) {
            if (stationList.get(i).id == id) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private String stationName(long id) {
        for (RideActivity.Option option : stationList) {
            if (option.id == id) {
                return option.name;
            }
        }
        return "站点" + id;
    }

    private String statusName(int status) {
        switch (status) {
            case STATUS_PENDING: return "待调度";
            case STATUS_RUNNING: return "进行中";
            case 2: return "已完成";
            default: return "已取消";
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

    /** 调度建议项 */
    static class Suggestion {
        final long fromStationId;
        final long toStationId;
        final int bikeCount;
        final String text;

        Suggestion(long fromStationId, long toStationId, int bikeCount, String text) {
            this.fromStationId = fromStationId;
            this.toStationId = toStationId;
            this.bikeCount = bikeCount;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    /** 调度任务列表项 */
    static class TaskItem {
        final long id;
        final int status;
        final String text;

        TaskItem(long id, int status, String text) {
            this.id = id;
            this.status = status;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
