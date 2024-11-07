import request from "@/util/request";

export function updateAccount(data) {
    return request({
        url: "admin/updateAccount",
        method: "put",
        data: data
    });
}
export function getAccount() {
    return request({
        url: "admin/account",
        method: "get",
    });
}