<template>
    <!-- qywx -->
    <van-divider
        :style="{
            color: '#1989fa',
            borderColor: '#1989fa',
            padding: '0 16px',
            marginTop: '32px'
          }"
    >企业微信配置
    </van-divider>
    <van-cell-group inset>
        <van-field v-for="item in qywxField" v-model="qywxConfig[item.key]" :label="item.label" :placeholder="item.label" />

        <van-cell title="操作">
            <div style="text-align: right">
                <van-button size="small" plain hairline style="margin-left: 5px;" type="info" @click="save()">保存</van-button>
            </div>
        </van-cell>
    </van-cell-group>
</template>

<script setup name="qywx-config">
import { onMounted, ref } from "vue";
import { getQywxApi } from "@/api/admin/qywx";

const qywxField = [
    {
        key: "corpid",
        label: "企业ID"
    },
    {
        key: "domain",
        label: "企业邮箱域名"
    },
    {
        key: "qywxKey",
        label: "群机器人"
    },
    {
        key: "agentid",
        label: "应用id"
    },
    {
        key: "corpsecret",
        label: "应用Secret"
    },
    {
        key: "contactsSecret",
        label: "通讯录Secret"
    },
    {
        key: "adminQywxId",
        label: "管理员id"
    }
]
const qywxConfig = ref({})

const get = () => {
    getQywxApi().then(res => {
        qywxConfig.value = res.data
    })
}

const save = () => {

}

onMounted(() => {
    get()
})
</script>
<style scoped>

</style>