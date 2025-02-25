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
import { showConfirmDialog, showToast } from "vant";
import { updateEnv } from "@/api/admin/ql";
import { getCurrentInstance, onMounted } from "vue";

const updateEnvFn = () => {

    showConfirmDialog({
        title: '提示',
        message: '确认提交环境更新？',
    }).then(() => {
        updateEnv().then(() => {
            showToast({
                message: '提交成功',
                duration: 500
            });
        })
    })
}

onMounted(()=>{
    getCurrentInstance().proxy.$setTitle ("系统设置");

})
</script>