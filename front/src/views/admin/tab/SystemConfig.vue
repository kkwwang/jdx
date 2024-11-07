<template>
    <div>
        <!-- 系统控制 -->
        <sys-controller />

        <!-- 网站设置 -->
        <website-config />

        <!-- qywx -->
        <qywx-config />

        <!-- 账号配置 -->
        <account-config />
    </div>
</template>
<script setup name="SystemConfig">

import SysController from "@/views/admin/tab/sys-controller.vue";
import WebsiteConfig from "@/views/admin/tab/website-config.vue";
import QywxConfig from "@/views/admin/qywx-config.vue";
import AccountConfig from "@/views/admin/tab/account-config.vue";
</script>