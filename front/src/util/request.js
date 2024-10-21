import axios from "axios";
import router from "../router";
import { Toast, Dialog } from "vant";

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 50000
});

service.interceptors.request.use(
  config => {
    Toast.loading({
      duration: 0,
      message: "加载中...",
      forbidClick: true
    });
    // token
    config.headers["token"] = localStorage.getItem("token");
    return config;
  },
  error => {
    Toast.clear();
    console.error("interceptors request error: " + error);
    return Promise.reject(error);
  }
);

service.interceptors.response.use(
  response => {
    Toast.clear();
    const resp = response.data;
    let msg = resp.msg;
    if (resp.code !== 0) {
      Dialog({
        title: "提示",
        message: msg
      });
      return Promise.reject(new Error(resp.msg || "Error"));
    } else {
      if (msg && msg.indexOf("登录成功") !== -1) {
        Dialog({
          title: "提示",
          message: msg
        });
      }
    }
    return resp;
  },
  error => {
    Toast.clear();
    const resp = error.response.data;
    let msg = resp.msg;
    if (msg) {
      Dialog({
        title: "提示",
        message: msg
      });
    }
    if (error.response.status === 401) {
      console.log("aa");
      setTimeout(() => {
        localStorage.removeItem("token");
        localStorage.removeItem("thirdToken");
        router.push("/login");
      }, 500);
    }
    return Promise.reject(error);
  }
);

export default service;
