<template>
  <div style="height: 100%;">
    <van-nav-bar :title="title">
      <template #left>
        <van-icon
          color="#ee0a24"
          name="setting-o"
          size="18"
          @click="$router.push('/login')"
        />
      </template>
      <template #right>
        <van-icon
          color="#ee0a24"
          name="balance-pay"
          size="18"
          @click="$router.push('/bean')"
        />
      </template>
    </van-nav-bar>
    <div
      style="height: calc(100vh - 46px);  overflow-y: auto; overflow-x: hidden;"
    >
      <van-notice-bar
        v-if="notice"
        left-icon="volume-o"
        :text="notice"
        mode="closeable"
      />
      <JD />
      <div style="text-align: center">
        <van-tag size="medium" type="primary">剩余车位：{{ remain }}</van-tag>
      </div>
      <div style="padding: 16px 8px " v-html="bottomNotice"></div>
      <version></version>
    </div>
  </div>
</template>

<script>
import JD from "./JD";
import { baseInfo } from "@/api";
import Version from "@/views/version.vue";

export default {
  name: "Index",
  components: { Version, JD },
  data() {
    return {
      title: "",
      notice: "",
      bottomNotice: "",

      remain: 0
    };
  },
  created() {
    this.renderBase();
    let tab = this.$route.query.tab;
    if (tab) {
      this.active = tab;
    } else {
      this.active = "jd";
    }
  },
  watch: {},
  methods: {
    renderBase: function() {
      baseInfo()
        .then(resp => {
          this.title = resp.data.title;
          this.notice = resp.data.notice;
          this.remain = resp.data.remain;
          this.bottomNotice = resp.data.bottomNotice;
        })
        .catch(err => {
          console.log(err);
        });
    }
  }
};
</script>

<style scoped></style>
