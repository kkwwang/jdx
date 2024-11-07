import request from "@/util/request";

// 登录
export function login(data) {
    return request({
        url: "auth/login",
        method: "post",
        data: data
    });
}