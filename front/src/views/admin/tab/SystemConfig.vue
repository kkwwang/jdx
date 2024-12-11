<template>
    <div>


        <!-- 网站设置 -->
        <website-config />

        <!-- qywx -->
        <qywx-config />

        <!-- 账号配置 -->
        <account-config />
    </div>
</template>
<script setup name="SystemConfig">

import WebsiteConfig from "@/views/admin/tab/website-config.vue";
import QywxConfig from "@/views/admin/qywx-config.vue";
import AccountConfig from "@/views/admin/tab/account-config.vue";
</script>