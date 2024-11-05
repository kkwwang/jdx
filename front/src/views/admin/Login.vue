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
    <div>
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
          >登录</van-button
        >
      </div>
    </div>
  </div>
</template>

<script>
import { login } from "@/api/admin";
import CryptoJS from "crypto-js";

export default {
  name: "Login",
  data() {
    return {
      username: localStorage.getItem("username"),
      password: localStorage.getItem("password")
    };
  },
  mounted() {
    if (localStorage.getItem("token")) {
      this.$router.push("/admin");
    }
  },
  methods: {
    login: function() {
      let form = {
        username: this.username,
        password: CryptoJS.MD5(this.password).toString()
      };
      login(form).then(resp => {
        localStorage.setItem("token", resp.data.token);
        this.$router.push("/admin");
        localStorage.setItem("username", this.username);
        localStorage.setItem("password", this.password);

      });
    }
  }
};
</script>

<style scoped></style>
