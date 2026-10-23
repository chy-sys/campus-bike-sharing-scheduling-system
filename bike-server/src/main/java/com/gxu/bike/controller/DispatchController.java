package com.gxu.bike.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gxu.bike.common.R;
import com.gxu.bike.dto.DispatchSuggestion;
import com.gxu.bike.entity.DispatchTask;
import com.gxu.bike.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    @Autowired
    private DispatchService dispatchService;

    /**
     * 调度建议：根据站点负荷率自动生成调出/调入方案
     */
    @GetMapping("/suggest")
    public R<List<DispatchSuggestion>> suggest() {
        return R.ok(dispatchService.suggest());
    }

    @GetMapping("/tasks")
    public R<IPage<DispatchTask>> page(@RequestParam(defaultValue = "1") long pageNum,
                                       @RequestParam(defaultValue = "10") long pageSize,
                                       @RequestParam(required = false) Integer status) {
        return R.ok(dispatchService.page(pageNum, pageSize, status));
    }

    @GetMapping("/tasks/{id}")
    public R<DispatchTask> detail(@PathVariable Long id) {
        return R.ok(dispatchService.getById(id));
    }

    @PostMapping("/tasks")
    public R<Long> create(@RequestBody DispatchTask task, HttpServletRequest request) {
        Object operatorId = request.getAttribute("userId");
        if (task.getOperatorId() == null && operatorId != null) {
            task.setOperatorId(Long.valueOf(String.valueOf(operatorId)));
        }
        return R.ok("调度任务创建成功", dispatchService.createTask(task));
    }

    @PutMapping("/tasks/{id}/start")
    public R<Void> start(@PathVariable Long id) {
        dispatchService.start(id);
        return R.ok("已开始调度", null);
    }

    @PutMapping("/tasks/{id}/finish")
    public R<Void> finish(@PathVariable Long id, HttpServletRequest request) {
        Object operatorId = request.getAttribute("userId");
        Long uid = operatorId == null ? null : Long.valueOf(String.valueOf(operatorId));
        dispatchService.finish(id, uid);
        return R.ok("调度完成", null);
    }

    @PutMapping("/tasks/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        dispatchService.cancel(id);
        return R.ok("调度已取消", null);
    }
}
