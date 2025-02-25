<template>
    <div style="height: 100%;">
        <van-nav-bar :title="info.title">
            <template #left>
                <van-icon
                    color="#ee0a24"
                    name="setting-o"
                    size="18"
                    @click="$router.push('/login')"
                />
            </template>
            <template #right>
                <van-icon
                    color="#ee0a24"
                    name="balance-pay"
                    size="18"
                    @click="$router.push('/bean/' + (mobile || ''))"
                />
            </template>
        </van-nav-bar>
        <div
            style="height: calc(100vh - 46px);  overflow-y: auto; overflow-x: hidden;"
        >
            <van-notice-bar
                v-if="info.notice"
                left-icon="volume-o"
                :text="info.notice"
                mode="closeable"
            />
            <JD :mobile="mobile" />
            <div style="text-align: center">
                <van-tag size="medium" type="primary">剩余车位：{{ info.remain }}</van-tag>
            </div>
            <div style="padding: 16px 8px " v-html="info.bottomNotice"></div>
            <version :server-version="info.version"></version>
        </div>
    </div>
</template>

<script setup name="Index">
import JD from "./JD.vue";
import { baseInfo } from "@/api";
import Version from "@/views/version.vue";
import { defineProps, getCurrentInstance, onMounted, ref } from "vue";

const info = ref({})
const _props = defineProps({
    mobile: String
})
onMounted(() => {
    getCurrentInstance().proxy.$setTitle ("登录");

    baseInfo().then(resp => {
        info.value = resp.data;
    })
})

</script>

<style scoped></style>
