import request from "@/util/request";

/**
 * 更新系统配置
 * @param data
 * @returns {*}
 */
export function updateWebsiteConfigApi(data) {
    return request({
        url: "admin/websiteConfig",
        method: "put",
        data: data
    });
}

export function getWebsiteConfigApi() {
    return request({
        url: "admin/websiteConfig",
        method: "get"
    });
}