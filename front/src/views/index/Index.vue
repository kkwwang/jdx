<template>
  <div>
    <div style="margin-bottom: 2em;">
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
      <van-notice-bar
        v-if="notice"
        left-icon="volume-o"
        :text="notice"
        mode="closeable"
      />

      <!-- title -->
      <!--      <div>-->
      <!--        <div-->
      <!--            v-if="title"-->
      <!--            style="text-align: center; margin: 40px 0 20px 0; font-size: 32px"-->
      <!--        >-->
      <!--          {{ title }}-->
      <!--        </div>-->
      <!--      </div>-->

      <JD />

      <div style="text-align: center">
        <van-tag size="medium" type="primary">剩余车位：{{ remain }}</van-tag>
      </div>

      <div style="padding: 16px 8px " v-html="bottomNotice"></div>
    </div>
  </div>
</template>

<script>
import JD from "./JD";
import { baseInfo } from "@/api";

export default {
  name: "Index",
  components: { JD },
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
