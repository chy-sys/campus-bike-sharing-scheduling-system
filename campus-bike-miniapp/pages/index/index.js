const app = getApp();

Page({
  data: {
    userInfo: null,
    roleName: '',
    stats: null,
    faultRateText: '-',
    repairFinishRateText: '-'
  },

  onShow() {
    if (!app.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const userInfo = app.globalData.userInfo || {};
    this.setData({
      userInfo: userInfo,
      roleName: this.roleName(userInfo.role)
    });
    this.loadStats();
  },

  loadStats() {
    app.request('/api/stats/overview')
      .then((data) => {
        this.setData({
          stats: data,
          faultRateText: ((data.faultRate || 0) * 100).toFixed(1) + '%',
          repairFinishRateText: ((data.repairFinishRate || 0) * 100).toFixed(1) + '%'
        });
      })
      .catch(() => { });
  },

  onPullDownRefresh() {
    this.loadStats();
    wx.stopPullDownRefresh();
  },

  logout() {
    app.clearLogin();
    wx.reLaunch({ url: '/pages/login/login' });
  },

  roleName(role) {
    if (role === 2) return '管理员';
    if (role === 1) return '运维人员';
    return '普通用户';
  }
});
