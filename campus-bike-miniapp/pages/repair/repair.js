const app = getApp();

const STATUS_TEXT = { 0: '待处理', 1: '处理中', 2: '已修复', 3: '无法修复' };
const STATUS_CLASS = { 0: 'tag-gray', 1: 'tag-warn', 2: 'tag-ok', 3: 'tag-gray' };
const FAULT_NAME = { 1: '刹车失灵', 2: '轮胎损坏', 3: '链条故障', 4: '电量不足', 5: '其他' };

Page({
  data: {
    repairs: []
  },

  onShow() {
    if (!app.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.load();
  },

  load() {
    app.request('/api/repairs?pageNum=1&pageSize=50')
      .then((data) => {
        const records = (data && data.records) || [];
        this.setData({
          repairs: records.map((item) => ({
            ...item,
            statusText: STATUS_TEXT[item.status] || '-',
            statusClass: STATUS_CLASS[item.status] || 'tag-gray',
            faultName: FAULT_NAME[item.faultType] || '其他'
          }))
        });
      })
      .catch(() => { });
  },

  /** 受理工单 */
  assign(e) {
    const id = e.currentTarget.dataset.id;
    app.request('/api/repairs/' + id + '/assign', 'PUT')
      .then(() => {
        wx.showToast({ title: '已受理' });
        this.load();
      })
      .catch(() => { });
  },

  /** 处理完成：选择修复结果 */
  finish(e) {
    const id = e.currentTarget.dataset.id;
    wx.showActionSheet({
      itemList: ['已修复，车辆恢复可用', '无法修复'],
      success: (res) => {
        const fixed = res.tapIndex === 0;
        app.request('/api/repairs/' + id + '/finish', 'PUT', {
          fixed: fixed,
          handleResult: fixed ? '现场检修完成，车辆恢复可用' : '车辆损坏严重，无法修复'
        })
          .then(() => {
            wx.showToast({ title: '工单已办结' });
            this.load();
          })
          .catch(() => { });
      }
    });
  },

  /** 查看报修照片 */
  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    if (!url) {
      return;
    }
    // 后端返回的是 /uploads/xxx.png，拼接成完整地址
    wx.previewImage({
      urls: url.split(',').map((item) => app.globalData.baseUrl + item)
    });
  },

  onPullDownRefresh() {
    this.load();
    wx.stopPullDownRefresh();
  }
});
