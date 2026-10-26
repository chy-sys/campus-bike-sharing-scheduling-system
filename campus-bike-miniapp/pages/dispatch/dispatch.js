const app = getApp();

const STATUS_TEXT = { 0: '待调度', 1: '进行中', 2: '已完成', 3: '已取消' };

Page({
  data: {
    suggest: [],
    tasks: [],
    fromId: '',
    toId: '',
    count: ''
  },

  onShow() {
    if (!app.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.load();
  },

  load() {
    app.request('/api/dispatch/suggest')
      .then((data) => {
        const list = (data || []).map((item) => ({
          ...item,
          fromRateText: Math.round((item.fromRate || 0) * 100) + '%',
          toRateText: Math.round((item.toRate || 0) * 100) + '%'
        }));
        this.setData({ suggest: list });
      })
      .catch(() => { });

    app.request('/api/dispatch/tasks?pageNum=1&pageSize=20')
      .then((data) => {
        const records = (data && data.records) || [];
        this.setData({
          tasks: records.map((item) => ({ ...item, statusText: STATUS_TEXT[item.status] || '-' }))
        });
      })
      .catch(() => { });
  },

  onFromId(e) {
    this.setData({ fromId: e.detail.value });
  },

  onToId(e) {
    this.setData({ toId: e.detail.value });
  },

  onCount(e) {
    this.setData({ count: e.detail.value });
  },

  /** 把建议填入表单 */
  useSuggest(e) {
    const item = this.data.suggest[e.currentTarget.dataset.index];
    this.setData({
      fromId: String(item.fromStationId),
      toId: String(item.toStationId),
      count: String(item.bikeCount)
    });
    wx.showToast({ title: '已填入表单', icon: 'none' });
  },

  createTask() {
    const fromStationId = Number(this.data.fromId);
    const toStationId = Number(this.data.toId);
    const bikeCount = Number(this.data.count);
    if (!fromStationId || !toStationId || !bikeCount) {
      wx.showToast({ title: '请填写完整的站点与数量', icon: 'none' });
      return;
    }
    if (fromStationId === toStationId) {
      wx.showToast({ title: '调出与调入站点不能相同', icon: 'none' });
      return;
    }
    app.request('/api/dispatch/tasks', 'POST', {
      fromStationId,
      toStationId,
      bikeCount,
      remark: '小程序运维创建'
    }, true)
      .then(() => {
        wx.showToast({ title: '任务已创建' });
        this.setData({ fromId: '', toId: '', count: '' });
        this.load();
      })
      .catch((err) => {
        wx.showToast({ title: (err && err.msg) || '创建失败', icon: 'none' });
      });
  },

  startTask(e) {
    this.doAction(e.currentTarget.dataset.id, 'start');
  },

  finishTask(e) {
    this.doAction(e.currentTarget.dataset.id, 'finish');
  },

  cancelTask(e) {
    this.doAction(e.currentTarget.dataset.id, 'cancel');
  },

  doAction(id, action) {
    app.request('/api/dispatch/tasks/' + id + '/' + action, 'PUT')
      .then(() => {
        wx.showToast({ title: '操作成功' });
        this.load();
      })
      .catch(() => { });
  }
});
