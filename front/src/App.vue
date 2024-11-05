<template>
  <div id="app">
    <div style="height: calc(100vh - 44px)">
      <router-view />
    </div>

    <div style="position: absolute; right: 10px; bottom: 10px;">
      <van-tag mark type="primary">页面：v{{ npm_package_version }}</van-tag>
      <van-tag mark type="primary">服务：v{{ version }}</van-tag>
    </div>
  </div>
</template>
<script>
import { baseInfo } from "@/api";
import { Dialog } from "vant";

export default {
  data() {
    return {
      version: "",
      npm_package_version: process.env.npm_package_version
    };
  },
  created() {
    baseInfo().then(resp => {
      this.version = resp.data.version;
      if (
        this.version.split("-")[0] !== this.npm_package_version.split("-")[0]
      ) {
        Dialog({
          title: "提示",
          message: "版本不一致，请点击右上角...刷新后再试"
        });
      }
    });
  }
};
</script>
<style>
#app {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: rgba(0, 0, 0, 0.02);
  height: 100vh;
}

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
</style>
