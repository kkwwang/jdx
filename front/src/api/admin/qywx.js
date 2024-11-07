import request from "@/util/request";


export function updateQywxApi(data) {
    return request({
        url: "admin/qywx",
        method: "put",
        data: data
    });
}
export function getQywxApi() {
    return request({
        url: "admin/qywx",
        method: "get"
    });
}