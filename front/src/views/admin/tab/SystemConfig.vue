<template>
  <div>
    <!-- 系统控制 -->
    <van-divider
        :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '12px'
      }"
    >系统控制
    </van-divider>
    <van-cell-group>
      <van-cell title="更新环境" is-link clickable @click="updateEnv"/>
    </van-cell-group>

    <!-- 网站设置 -->
    <van-divider
        :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '12px'
      }"
    >网站设置
    </van-divider>
    <van-swipe-cell>
      <van-cell-group inset>
        <van-cell title="标题" :value="title" clickable/>
        <van-cell title="公告" :value="notice" clickable/>
        <van-cell title="底部公告" :value="bottomNotice" clickable/>
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
        <van-field v-model="title" label="网站标题" placeholder="网站标题"/>
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
        <van-cell v-for="item in field.qywx" :value="qywx[item.key]" :title="item.label"/>
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
        <van-field v-for="item in field.qywx" v-model="qywx[item.key]" :label="item.label" :placeholder="item.label"/>
        <div style="margin: 16px;">
          <van-button round block type="info" @click="updateQywx()">提交</van-button>
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
        <van-cell title="用户名" :value="accountConfig.username"/>
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
        <van-field
            v-model="accountConfig.rePassword"
            type="password"
            label="确认密码"
            placeholder="确认密码"
        />
        <div style="margin: 16px;">
          <van-button round block type="info" @click="updateAccount()"
          >提交
          </van-button>
        </div>
      </van-form>
    </van-action-sheet>
    <!-- 账号配置 end -->
  </div>
</template>

<script>
import {getSystemConfig, updateAccount, updateEnv, updateQywx, updateWebsiteConfig} from "@/api/admin";
import CryptoJS from "crypto-js";
import {Dialog} from "vant";

export default {
  name: "SystemConfig",
  data() {
    return {
      field: {
        qywx: [
          {
            key: "corpid",
            label: "企业ID"
          },
          {
            key: "domain",
            label: "企业邮箱域名"
          },
          {
            key: "qywxKey",
            label: "群机器人"
          },
          {
            key: "agentid",
            label: "应用id"
          },
          {
            key: "corpsecret",
            label: "应用Secret"
          },
          {
            key: "contactsSecret",
            label: "通讯录Secret"
          },
          {
            key: "adminQywxId",
            label: "管理员id"
          }
        ]
      },

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
        contactsSecret: "",
        domain: "",
        adminQywxId: "",
        agentid: "",
        qywxKey: ""
      },
      accountConfig: {
        username: "",
        password: "",
        rePassword: "",
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
    updateEnv(){
      Dialog.confirm({
        title: '提示',
        message: '确认提交环境更新？',
      })
          .then(() => {
            // on confirm
            updateEnv().then(() => {
              this.$toast.success("更新成功");
            })
          })
          .catch(() => {
            // on cancel
          });

    },
    getSystemConfig: function () {
      getSystemConfig().then(resp => {
        this.title = resp.data.title;
        this.notice = resp.data.notice;
        this.bottomNotice = resp.data.bottomNotice;
        this.accountConfig.username = resp.data.username;
        this.qywx.qywxKey = resp.data.qywxKey;
        this.qywx.corpid = resp.data.corpid;
        this.qywx.corpsecret = resp.data.corpsecret;
        this.qywx.agentid = resp.data.agentid;
        this.qywx.contactsSecret = resp.data.contactsSecret;
        this.qywx.domain = resp.data.domain;
        this.qywx.adminQywxId = resp.data.adminQywxId;
      });
    },
    updateWebsiteConfig: function () {
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
    updateAccount: function () {
      if (this.accountConfig.password && this.accountConfig.password === this.accountConfig.rePassword) {
        updateAccount({
          username: this.accountConfig.username,
          password: CryptoJS.MD5(this.accountConfig.password).toString()
        })
        this.accountConfig.actionSheet.show = false;
        setTimeout(() => {
          localStorage.removeItem("token");
          this.$router.push("/login");
        }, 800);
      }

    },
    updateQywx: function () {
      let param = {
        qywxKey: this.qywx.qywxKey,
        corpid: this.qywx.corpid,
        corpsecret: this.qywx.corpsecret,
        domain: this.qywx.domain,
        adminQywxId: this.qywx.adminQywxId,
        contactsSecret: this.qywx.contactsSecret,
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
