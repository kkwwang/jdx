<template>
    <!-- 账号配置 -->
    <van-divider
        :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '32px'
      }"
    >账号设置
    </van-divider>
    <van-cell-group inset>

        <van-field
            v-model="accountConfig.username"
            label="用户名"
            placeholder="用户名"
        />
        <van-field
            v-model="accountConfig.password"
            type="password"
            label="密码"
            placeholder="密码"
        />
        <van-field
            v-model="accountConfig.rePassword"
            type="password"
            label="确认密码"
            placeholder="确认密码"
        />

        <van-cell title="操作">
            <div style="text-align: right">
                <van-button size="small" plain hairline style="margin-left: 5px;" type="info" @click="save()">保存</van-button>
            </div>
        </van-cell>
    </van-cell-group>

</template>

<script setup name="account-config">
import { onMounted, ref } from "vue";
import { getAccount, updateAccount } from "@/api/admin/account";
import { showDialog } from "vant";

const accountConfig = ref({
    username: "",
    password: "",
    rePassword: ""
})

const get = () => {
    getAccount().then(resp => {
        accountConfig.value.username = resp.data;
    });
};

const save = () => {
    updateAccount(accountConfig.value).then(() => {
        showDialog({
            message: "修改成功",
            duration: 500
        }).then(() => {
            localStorage.removeItem("token");
            window.location.href = "/login";
        })
    });
};

onMounted(() => {
    get()
})
</script>

<style scoped>

</style>