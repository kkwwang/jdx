<template>
  <div>
    <div style="margin-bottom: 2em;">
      <van-notice-bar
          v-if="notice"
          left-icon="volume-o"
          :text="notice"
          mode="closeable"
      />

      <!-- title -->
      <div>
        <div
            v-if="title"
            style="text-align: center; margin: 40px 0 20px 0; font-size: 32px"
        >
          {{ title }}
        </div>
      </div>

      <JD/>

      <div style="text-align: center">
        <van-tag size="medium" type="primary">剩余车位：{{ remain }}</van-tag>
      </div>

      <div style="padding: 16px 8px " v-html="bottomNotice"></div>
    </div>


    <div
        style="position: fixed; bottom: 10px; right: 10px;"
    >

      <van-tag mark type="primary">页面版本号：{{ npm_package_version }}</van-tag>
      <van-tag mark type="primary">服务版本号：{{ version }}</van-tag>
    </div>
  </div>
</template>

<script>
import JD from "./JD";
import {baseInfo} from "@/api";
import {Dialog} from "vant";

export default {
  name: "Index",
  components: {JD},
  data() {
    return {
      title: "",
      notice: "",
      bottomNotice: "",
      version: "",
      npm_package_version: process.env.npm_package_version,
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
    renderBase: function () {
      baseInfo()
          .then(resp => {
            this.title = resp.data.title;
            this.notice = resp.data.notice;
            this.remain = resp.data.remain;
            this.bottomNotice = resp.data.bottomNotice;
            this.version = resp.data.version;

            debugger;
            if (
                this.version.split("-")[0] !==
                this.npm_package_version.split("-")[0]
            ) {
              Dialog({
                title: "提示",
                message: "版本不一致，请点击右上角...刷新后再试"
              });
            }
          })
          .catch(err => {
            console.log(err);
          });
    }
  }
};
</script>

<style scoped>
.van-tag--mark {
  border-radius: 0 0 0 0;
  padding: 4px 10px;

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
