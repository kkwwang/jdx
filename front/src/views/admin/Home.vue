<template>
  <div>
    <div style="background-color: #f6f6f6; padding-bottom: 20px;">
      <van-tabs v-model="active">
        <van-tab title="青龙管理">
          <QLManage />
        </van-tab>
        <van-tab title="系统设置">
          <SystemConfig />
        </van-tab>
        <van-tab title="资产统计">
          <BeanMange/>
        </van-tab>
      </van-tabs>

      <!-- 其他操作 -->
      <van-divider
        :style="{
          color: '#1989fa',
          borderColor: '#1989fa',
          padding: '0 16px',
          marginTop: '32px'
        }"
        >其他操作
      </van-divider>
      <div style="margin: 16px 16px">
        <van-button style="margin: 8px 0" round block plain type="info" to="/"
          >前往前台
        </van-button>
        <van-button
          style="margin: 8px 0"
          round
          block
          type="warning"
          @click="logout()"
          >注销登录
        </van-button>
      </div>
      <!-- 其他操作 end -->
    </div>
  </div>
</template>

<script>
import QLManage from "@/views/admin/tab/QLManage";
import SystemConfig from "@/views/admin/tab/SystemConfig";
import BeanMange from "@/views/admin/tab/BeanManage.vue";

export default {
  name: "Home",
  components: {BeanMange, QLManage, SystemConfig },
  data() {
    return {
      active: 2
    };
  },
  methods: {
    logout: function() {
      localStorage.removeItem("token");
      this.$router.push("/login");
    }
  }
};
</script>

<style scoped></style>
