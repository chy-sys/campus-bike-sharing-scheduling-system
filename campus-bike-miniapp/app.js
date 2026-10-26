/**
 * 运维小程序入口
 * 全局维护登录态（token / 用户信息），并封装统一请求
 */
App({
  globalData: {
    // 后端地址：真机调试填电脑局域网 IP，与手机在同一 WiFi 下
    baseUrl: 'http://172.27.104.169:8001',
    token: '',
    userInfo: null
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
    }
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.globalData.userInfo = userInfo;
    }
  },

  /**
   * 统一请求：自动附带 token，统一处理业务码与提示
   * @param {string} path 接口路径，如 /api/stations/all
   * @param {string} method GET / POST / PUT / DELETE
   * @param {object} data 请求参数
   * @param {boolean} silent 失败时不弹提示
   * @returns {Promise} 成功时 resolve 业务 data
   */
  request(path, method = 'GET', data = {}, silent = false) {
    const app = this;
    return new Promise((resolve, reject) => {
      wx.request({
        url: app.globalData.baseUrl + path,
        method: method,
        data: data,
        header: {
          'content-type': 'application/json',
          Authorization: app.globalData.token ? 'Bearer ' + app.globalData.token : ''
        },
        success(res) {
          const body = res.data;
          if (!body || typeof body.code === 'undefined') {
            if (!silent) wx.showToast({ title: '返回数据格式异常', icon: 'none' });
            reject(body);
            return;
          }
          if (body.code === 200) {
            resolve(body.data);
            return;
          }
          if (body.code === 401) {
            app.clearLogin();
            wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
            setTimeout(() => {
              wx.navigateTo({ url: '/pages/login/login' });
            }, 800);
            reject(body);
            return;
          }
          if (!silent) {
            wx.showToast({ title: body.msg || '请求失败', icon: 'none' });
          }
          reject(body);
        },
        fail(err) {
          if (!silent) {
            wx.showToast({ title: '网络连接失败，检查后端是否启动', icon: 'none' });
          }
          reject(err);
        }
      });
    });
  },

  /** 保存登录态 */
  saveLogin(token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);
  },

  /** 清除登录态 */
  clearLogin() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  },

  /** 是否已登录 */
  isLogin() {
    return !!this.globalData.token;
  }
});
