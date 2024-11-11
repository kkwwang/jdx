<template>
    <div style="background-color: #f6f6f6;  height: 100%;">
        <van-tabs>
            <van-tab title="资产统计">
                <BeanMange />
            </van-tab>
            <van-tab title="青龙管理">
                <QLManage />
            </van-tab>
            <van-tab title="系统设置">
                <SystemConfig />
            </van-tab>
        </van-tabs>
    </div>
</template>

<script setup name="Home">
import QLManage from "@/views/admin/tab/QLManage.vue";
import SystemConfig from "@/views/admin/tab/SystemConfig.vue";
import BeanMange from "@/views/admin/tab/BeanManage.vue";
</script>

<style scoped>
:deep(.van-tabs__content) {
    padding: 10px 0;
    height: calc(100vh - 64px);
    overflow-y: auto;
}
</style>
