<template>
  <div>
    <!-- 网站设置 -->
    <van-divider
      :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '32px'
      }"
      >网站设置
    </van-divider>
    <van-swipe-cell>
      <van-cell-group inset>
        <van-cell title="标题" :value="title" clickable />
        <van-cell title="公告" :value="notice" clickable />
        <van-cell title="底部公告" :value="bottomNotice" clickable />
      </van-cell-group>

      <template #right>
        <van-button
          square
          type="info"
          class="slide-button"
          text="编辑"
          @click="websiteConfig.show = true"
        />
      </template>
    </van-swipe-cell>
    <van-action-sheet v-model="websiteConfig.show" title="编辑网站设置">
      <van-form>
        <van-field v-model="title" label="网站标题" placeholder="网站标题" />
        <van-field
          v-model="notice"
          label="公告"
          autosize
          type="textarea"
          placeholder="公告"
        />
        <van-field
          v-model="bottomNotice"
          label="底部公告"
          autosize
          type="textarea"
          placeholder="底部公告"
        />
        <div style="margin: 16px;">
          <van-button round block type="info" @click="updateWebsiteConfig()"
            >提交
          </van-button>
        </div>
      </van-form>
    </van-action-sheet>
    <!-- 网站设置 end -->

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
    <van-swipe-cell>
      <van-cell-group inset>
        <van-cell title="webhookKey" :value="qywx.qywxKey" />
        <van-cell title="corpid" :value="qywx.corpid" />
        <van-cell title="corpsecret" :value="qywx.corpsecret" />
        <van-cell title="agentid" :value="qywx.agentid" />
      </van-cell-group>

      <template #right>
        <van-button
          square
          type="info"
          class="slide-button"
          text="编辑"
          @click="qywx.actionSheet.show = true"
        />
      </template>
    </van-swipe-cell>

    <van-action-sheet v-model="qywx.actionSheet.show" title="编辑企业微信配置">
      <van-form>
        <van-field v-model="qywx.corpid" label="corpid" placeholder="corpid" />
        <van-field
          v-model="qywx.corpsecret"
          label="corpsecret"
          placeholder="corpsecret"
        />
        <van-field
          v-model="qywx.agentid"
          label="agentid"
          placeholder="agentid"
        />
        <van-field v-model="qywx.qywxKey" label="key" placeholder="key" />
        <div style="margin: 16px;">
          <van-button round block type="info" @click="updateQywx()"
            >提交
          </van-button>
        </div>
      </van-form>
    </van-action-sheet>

    <!-- qywx end -->

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
    <van-swipe-cell>
      <van-cell-group inset>
        <van-cell title="用户名" :value="accountConfig.username" />
      </van-cell-group>

      <template #right>
        <van-button
          square
          type="info"
          class="slide-button"
          text="编辑"
          @click="accountConfig.actionSheet.show = true"
        />
      </template>
    </van-swipe-cell>
    <van-action-sheet
      v-model="accountConfig.actionSheet.show"
      title="编辑账号设置"
    >
      <van-form>
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
        <div style="margin: 16px;">
          <van-button round block type="info" @click="updateAccount()"
            >提交
          </van-button>
        </div>
      </van-form>
    </van-action-sheet>
    <!-- 账号配置 end -->

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
        >前往前台</van-button
      >
      <van-button
        style="margin: 8px 0"
        round
        block
        type="warning"
        @click="logout()"
        >注销登录</van-button
      >
    </div>
    <!-- 其他操作 end -->
  </div>
</template>

<script>
import {
  getSystemConfig,
  updateWebsiteConfig,
  updateAccount,
  updateQywx
} from "@/api/admin";

export default {
  name: "SystemConfig",
  data() {
    return {
      title: "",
      notice: "",
      bottomNotice: "",

      websiteConfig: {
        show: false
      },
      qywx: {
        actionSheet: {
          show: false
        },
        corpid: "",
        corpsecret: "",
        agentid: "",
        qywxKey: ""
      },
      accountConfig: {
        username: "",
        password: "",
        actionSheet: {
          show: false
        }
      }
    };
  },
  mounted() {
    this.getSystemConfig();
  },
  methods: {
    getSystemConfig: function() {
      getSystemConfig().then(resp => {
        this.title = resp.data.title;
        this.notice = resp.data.notice;
        this.bottomNotice = resp.data.bottomNotice;
        this.accountConfig.username = resp.data.username;
        this.accountConfig.password = resp.data.password;
        this.qywx.qywxKey = resp.data.qywxKey;
        this.qywx.corpid = resp.data.corpid;
        this.qywx.corpsecret = resp.data.corpsecret;
        this.qywx.agentid = resp.data.agentid;
      });
    },
    updateWebsiteConfig: function() {
      let param = {};
      param.title = this.title;
      param.notice = this.notice;
      param.bottomNotice = this.bottomNotice;
      updateWebsiteConfig(param).then(resp => {
        this.title = resp.data.title;
        this.notice = resp.data.notice;
        this.websiteConfig.show = false;
      });
    },
    logout: function() {
      localStorage.removeItem("token");
      this.$router.push("/login");
    },
    updateAccount: function() {
      updateAccount({
        username: this.accountConfig.username,
        password: this.accountConfig.password
      }).then(() => {});
      this.accountConfig.actionSheet.show = false;
      setTimeout(() => {
        localStorage.removeItem("token");
        this.$router.push("/login");
      }, 800);
    },
    updateQywx: function() {
      let param = {
        qywxKey: this.qywx.qywxKey,
        corpid: this.qywx.corpid,
        corpsecret: this.qywx.corpsecret,
        agentid: this.qywx.agentid
      };
      updateQywx(param).then(() => {
        this.qywx.actionSheet.show = false;
      });
    }
  }
};
</script>

<style scoped></style>
