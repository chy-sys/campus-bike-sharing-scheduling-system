const app = getApp();

Page({
  data: {
    phone: '',
    password: '',
    // 微信首次登录时为 true，需先绑定手机号
    needBind: false,
    openid: ''
  },

  onLoad() {
    // 已登录直接进工作台
    if (app.isLogin()) {
      wx.switchTab({ url: '/pages/index/index' });
    }
  },

  onPhone(e) {
    this.setData({ phone: e.detail.value });
  },

  onPassword(e) {
    this.setData({ password: e.detail.value });
  },

  /** 微信一键登录：wx.login 取 code 换 openid */
  wechatLogin() {
    wx.login({
      success: (res) => {
        if (!res.code) {
          wx.showToast({ title: '获取微信凭证失败', icon: 'none' });
          return;
        }
        app.request('/auth/wechat/login', 'POST', { code: res.code }, true)
          .then((data) => {
            if (data && data.needBind) {
              this.setData({ needBind: true, openid: data.openid });
              wx.showToast({ title: '请先绑定手机号', icon: 'none' });
              return;
            }
            this.afterLogin(data);
          })
          .catch((err) => {
            wx.showToast({ title: (err && err.msg) || '微信登录失败', icon: 'none' });
          });
      },
      fail: () => {
        wx.showToast({ title: 'wx.login 调用失败', icon: 'none' });
      }
    });
  },

  /** 绑定微信与账号 */
  bindWechat() {
    const { openid, phone, password } = this.data;
    if (!phone || !password) {
      wx.showToast({ title: '请输入手机号与密码', icon: 'none' });
      return;
    }
    app.request('/auth/wechat/bind', 'POST', { openid, phone, password }, true)
      .then((data) => {
        this.afterLogin(data);
      })
      .catch((err) => {
        wx.showToast({ title: (err && err.msg) || '绑定失败', icon: 'none' });
      });
  },

  /** 账号密码登录 */
  passwordLogin() {
    const { phone, password } = this.data;
    if (!phone || !password) {
      wx.showToast({ title: '请输入手机号与密码', icon: 'none' });
      return;
    }
    app.request('/auth/login', 'POST', { phone, password }, true)
      .then((data) => {
        this.afterLogin(data);
      })
      .catch((err) => {
        wx.showToast({ title: (err && err.msg) || '登录失败', icon: 'none' });
      });
  },

  /** 登录成功后的统一处理 */
  afterLogin(data) {
    app.saveLogin(data.token, {
      userId: data.userId,
      userName: data.userName,
      phone: data.phone,
      role: data.role
    });
    wx.showToast({ title: '登录成功' });
    wx.switchTab({ url: '/pages/index/index' });
  },

  cancelBind() {
    this.setData({ needBind: false, openid: '', password: '' });
  }
});
