<template>
    <div>
        <van-nav-bar title="后台管理">
            <template #left>
                <van-icon
                    color="#ee0a24"
                    name="arrow-left"
                    size="18"
                    @click="$router.push('/')"
                />
            </template>
        </van-nav-bar>
        <div style="height: calc(100% - 46px)">
            <van-cell-group>
                <van-field
                    v-model="username"
                    label="用户名"
                    placeholder="请输入用户名"
                />
                <van-field
                    v-model="password"
                    label="密码"
                    placeholder="请输入密码"
                    type="password"
                    @keypress.enter="login()"
                />
            </van-cell-group>
            <div style="padding: 20px">
                <van-button type="primary" block round @click="login()"
                >登录
                </van-button
                >
            </div>
        </div>
    </div>
</template>

<script setup name="login">
import { login as loginApi } from "@/api/admin/login";
import CryptoJS from "crypto-js";
import { getCurrentInstance, onBeforeMount, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter()

const username = ref(localStorage.getItem("username"));
const password = ref(localStorage.getItem("password"));

const login = () => {

    let form = {
        username: username.value,
        password: CryptoJS.MD5(password.value).toString()
    };
    loginApi(form).then(resp => {
        localStorage.setItem("token", resp.data);
        router.push("/admin");
        localStorage.setItem("username", username.value);
        localStorage.setItem("password", password.value);
    });
}


onBeforeMount(() => {
    getCurrentInstance().proxy.$setTitle ("管理端登录");
    if (localStorage.getItem("token")) {
        router.push("/admin");
    }
})

</script>

<style scoped></style>
