import request from "@/util/request";

// 青龙列表
export function getQLConfigList() {
    return request({
        url: "admin/ql",
        method: "get"
    });
}

// 删除青龙配置
export function delQLConfig(displayName) {
    return request({
        url: "admin/ql?displayName=" + displayName,
        method: "delete"
    });
}


// 编辑青龙配置
export function saveQLConfig(data) {
    return request({
        url: "admin/ql",
        method: "post",
        data: data
    });
}

export function updateEnv() {
    return request({
        url: "admin/updateEnv",
        method: "get"
    });
}
