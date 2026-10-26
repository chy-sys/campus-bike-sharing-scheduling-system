const app = getApp();

const STATUS_TEXT = { 0: '故障', 1: '可用', 2: '使用中', 3: '维修中' };
const STATUS_CLASS = { 0: 'tag-gray', 1: 'tag-ok', 2: 'tag', 3: 'tag-warn' };

Page({
  data: {
    bikes: []
  },

  onShow() {
    if (!app.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.load();
  },

  load() {
    app.request('/api/bikes?pageNum=1&pageSize=50')
      .then((data) => {
        const records = (data && data.records) || [];
        this.setData({
          bikes: records.map((item) => ({
            ...item,
            statusText: STATUS_TEXT[item.status] || '-',
            statusClass: STATUS_CLASS[item.status] || 'tag-gray'
          }))
        });
      })
      .catch(() => { });
  },

  /** 点击车辆弹出操作菜单，变更状态 */
  changeStatus(e) {
    const { id, number } = e.currentTarget.dataset;
    wx.showActionSheet({
      itemList: ['设为可用', '标记故障', '送修（维修中）'],
      success: (res) => {
        const statusMap = [1, 0, 3];
        const status = statusMap[res.tapIndex];
        app.request('/api/bikes/' + id + '/status?status=' + status, 'PUT')
          .then(() => {
            wx.showToast({ title: number + ' 状态已更新' });
            this.load();
          })
          .catch(() => { });
      }
    });
  },

  onPullDownRefresh() {
    this.load();
    wx.stopPullDownRefresh();
  }
});
