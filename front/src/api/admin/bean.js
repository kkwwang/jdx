import request from "@/util/request";

export function getLatestBean(time) {
    return request({
        url: "admin/getLatestBean",
        method: "get",
        params: { time }
    });
}
export function getAllEnv() {
    return request({
        url: "admin/getAllEnv",
        method: "get"
    });
}
export function getAllDate() {
    return request({
        url: "admin/getAllDate",
        method: "get"
    });
}