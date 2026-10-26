const app = getApp();

Page({
  data: {
    stations: []
  },

  onShow() {
    if (!app.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.load();
  },

  load() {
    app.request('/api/stations/all')
      .then((data) => {
        const list = (data || []).map((item) => {
          const rate = item.capacity > 0 ? Math.round((item.currentCount / item.capacity) * 100) : 0;
          let rateClass = 'tag-ok';
          if (rate >= 80) {
            rateClass = 'tag-warn';
          } else if (rate <= 20) {
            rateClass = 'tag-gray';
          }
          return { ...item, rateText: rate + '%', rateClass };
        });
        this.setData({ stations: list });
      })
      .catch(() => { });
  },

  onPullDownRefresh() {
    this.load();
    wx.stopPullDownRefresh();
  }
});
