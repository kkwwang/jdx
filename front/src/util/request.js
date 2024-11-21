import axios from "axios";
import router from "../router";
import { closeToast, showDialog, showLoadingToast } from 'vant';

const service = axios.create({
    baseURL: import.meta.env.VITE_BASE_API,
    timeout: 600000
});

service.interceptors.request.use(
    config => {
        showLoadingToast({
            duration: 0,
            message: "加载中...",
            forbidClick: true
        });
        // token
        config.headers["token"] = localStorage.getItem("token");
        return config;
    },
    error => {
        closeToast()
        return Promise.reject(error);
    }
);

service.interceptors.response.use(
    response => {
        closeToast()
        const resp = response.data;
        let msg = resp.msg;
        if (resp.code !== 0) {
            showDialog({
                title: "提示",
                message: msg
            });
            return Promise.reject(new Error(resp.msg || "Error"));
        } else {
            if (msg && msg.indexOf("登录成功") !== -1) {
                showDialog({
                    title: "提示",
                    message: msg
                });
            }
        }
        return resp;
    },
    error => {
        closeToast()
        const resp = error.response.data;
        let msg = resp.msg;
        if (error.response.status === 401) {
            setTimeout(() => {
                localStorage.removeItem("token");
                localStorage.removeItem("thirdToken");
                router.push("/login");
            }, 500);
        } else {
            showDialog({
                title: "提示",
                message: msg || "Error"
            });
        }
        return Promise.reject(error);
    }
);

export default service;
