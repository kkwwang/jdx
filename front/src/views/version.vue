<template>
    <div class="version-box">
        <van-tag mark type="primary">页面：v{{ npmPackageVersion }}</van-tag>
        <van-tag mark type="primary">服务：v{{ serverVersion }}</van-tag>
    </div>
</template>

<script setup name="version">
import { ref, watch } from "vue";
import { showDialog } from 'vant';

const npmPackageVersion = ref(npm_package_version)
let _props = defineProps({
    serverVersion: {
        type: String,
        default: ""
    }
});

watch(_props.serverVersion, () => {
    if (_props.serverVersion) {
        if (_props.serverVersion.split("-")[0] !== npmPackageVersion.value.split("-")[0]) {
            showDialog({
                title: "提示",
                message: "版本不一致，请点击右上角...刷新后再试"
            });
        }
    }
})
</script>

<style scoped lang="scss">
.version-box {
    padding: 10px;
    text-align: right;

    .van-tag--mark {
        padding: 4px 10px;
        border-radius: 999px !important;
    }

    .van-tag--mark:first-child {
        border-radius: 999px 0 0 999px !important;
    }

    .van-tag--mark:last-child {
        border-radius: 0 999px 999px 0 !important;
    }

    .van-tag--mark:not(:last-child) {
        margin-right: 1px;
    }
}
</style>
