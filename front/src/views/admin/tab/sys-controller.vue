<template>
    <van-divider
        :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '12px'
      }"
    >系统控制
    </van-divider>
    <van-cell-group inset>
        <van-cell title="更新环境" is-link clickable @click="updateEnvFn" />
    </van-cell-group>
</template>
<script setup name="sys-controller">
import { showConfirmDialog } from "vant";
import { updateEnv } from "@/api/admin/ql";

const updateEnvFn = () => {

    showConfirmDialog({
        title: '提示',
        message: '确认提交环境更新？',
    }).then(() => {
        updateEnv().then(() => {
            this.$toast.success("更新成功");
        })
    })
}
</script>