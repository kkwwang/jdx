<template>
  <div>
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
  </div>

  <!-- 其他操作 end -->
</template>
<script>
export default {
  name: "other",
  methods: {
    logout: function() {
      localStorage.removeItem("token");
      this.$router.push("/login");
    }
  }
};
</script>
<style scoped></style>
