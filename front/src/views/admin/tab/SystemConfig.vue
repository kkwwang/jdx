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

    <!-- wxPusher -->
    <van-divider
        :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '32px'
      }"
    >wxPusher设置
    </van-divider>
    <van-swipe-cell>
      <van-cell-group inset>
        <van-cell title="appToken" :value="wxPusher.appToken"/>
        <van-cell title="管理员UID" :value="wxPusher.adminUid"/>
      </van-cell-group>

      <template #right>
        <van-button
            square
            type="info"
            class="slide-button"
            text="编辑"
            @click="wxPusher.actionSheet.show = true"
        />
      </template>
    </van-swipe-cell>

    <van-action-sheet v-model="wxPusher.actionSheet.show" title="编辑WxPusher">
      <van-form>
        <van-field
            v-model="wxPusher.appToken"
            label="appToken"
            placeholder="appToken"
        />
        <van-field
            v-model="wxPusher.adminUid"
            label="管理员UID"
            autosize
            type="textarea"
            placeholder="管理员UID"
        />
        <div style="margin: 16px;">
          <van-button round block type="info" @click="updateWxPusher()"
          >提交
          </van-button>
        </div>
      </van-form>
    </van-action-sheet>

    <!-- wxPusher end -->

    <!-- qywxKey -->
    <van-divider
        :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '32px'
      }"
    >企业微信机器人配置
    </van-divider>
    <van-swipe-cell>
      <van-cell-group inset>
        <van-cell title="key" :value="qywx.qywxKey"/>
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
        <van-field
            v-model="qywx.qywxKey"
            label="key"
            placeholder="key"
        />
        <div style="margin: 16px;">
          <van-button round block type="info" @click="updateQywx()"
          >提交
          </van-button>
        </div>
      </van-form>
    </van-action-sheet>

    <!-- qywxKey end -->

    <!-- 定时任务配置 -->
    <van-divider
        :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '32px'
      }"
    >定时任务配置
    </van-divider>
    <!-- cookie -->
    <van-swipe-cell>
      <van-cell-group inset>
        <van-cell title="检查Cookie Cron" :value="checkCookie.cron" clickable/>
      </van-cell-group>
      <template #right>
        <van-button
            square
            type="info"
            class="slide-button"
            text="编辑"
            @click="checkCookie.picker.show = true"
        />
        <van-button
            square
            type="primary"
            class="slide-button"
            text="执行"
            @click="doCheckCookie"
        />
      </template>
    </van-swipe-cell>
    <van-action-sheet
        v-model="checkCookie.picker.show"
        title="编辑定时检查Cookie Cron"
    >
      <van-form>
        <van-field v-model="checkCookie.cron" label="Cron" placeholder="cron"/>
        <div style="margin: 16px;">
          <van-button round block type="info" @click="updateCheckCookieCron()"
          >提交
          </van-button>
        </div>
      </van-form>
    </van-action-sheet>

    <van-popup
        v-if="checkCookie.resultPop.show && checkCookie.resultPop.data.length > 0"
        v-model="checkCookie.resultPop.show"
        title="以下Cookie已经过期"
        :style="{ width: '85%', height: '80%' }"
        closeable
    >
      <div style="font-weight: bold; text-align: center; margin: 16px 0">
        以下Cookie已过期，已自动禁用
      </div>
      <van-list>
        <van-cell v-for="d in checkCookie.resultPop.data" :key="d.displayName">
          <template slot="default">
            <div>
              <div style="font-weight: bold">节点：{{ d.displayName }}</div>
              <div
                  v-for="ck in d.expiredPtPins"
                  :key="ck"
                  style="padding: 4px 0 0 16px "
              >
                <span>{{ ck }}</span>
              </div>
            </div>
          </template>
        </van-cell>
      </van-list>
    </van-popup>
    <!-- cookie end -->
    <!-- 定时任务配置 end -->

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
        <van-cell title="用户名" :value="accountConfig.username" clickable/>
        <van-cell title="密码" :value="accountConfig.password" clickable/>
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
    <van-action-sheet v-model="accountConfig.actionSheet.show" title="编辑账号设置">
      <van-form>
        <van-field v-model="accountConfig.username" label="用户名" placeholder="用户名"/>
        <van-field v-model="accountConfig.password" label="密码" placeholder="密码"/>
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
      <van-button style="margin: 8px 0" round block plain type="info" to="/">前往前台</van-button>
      <van-button style="margin: 8px 0" round block type="warning" @click="logout()">注销登录</van-button>
    </div>
    <!-- 其他操作 end -->
  </div>
</template>

<script>
import {
  getSystemConfig,
  updateWebsiteConfig,
  checkCookie,
  updateCheckCookieCron,
  updateAccount, updateWxPusher, updateQywx
} from "@/api/admin";

export default {
  name: "SystemConfig",
  data() {
    return {
      title: "",
      notice: "",
      bottomNotice: "",

      websiteConfig: {
        show: false,
      },
      qywx: {
        actionSheet: {
          show: false
        },
        qywxKey: ''
      },
      wxPusher: {
        actionSheet: {
          show: false
        },
        appToken: '',
        adminUid: '',
      },
      checkCookie: {
        cron: "",
        picker: {
          show: false
        },
        resultPop: {
          show: false,
          data: []
        }
      },
      accountConfig: {
        username: '',
        password: '',
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
    getSystemConfig: function () {
      getSystemConfig().then(resp => {
        this.title = resp.data.title;
        this.notice = resp.data.notice;
        this.bottomNotice = resp.data.bottomNotice;
        this.checkCookie.cron = resp.data.checkCookieCron;
        this.accountConfig.username = resp.data.username
        this.accountConfig.password = resp.data.password
        this.qywx.qywxKey = resp.data.qywxKey
        this.wxPusher.appToken = resp.data.appToken
        this.wxPusher.adminUid = resp.data.adminUid
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
    doCheckCookie: function () {
      checkCookie().then(resp => {
        if (resp.data.length > 0) {
          this.checkCookie.resultPop.show = true;
          this.checkCookie.resultPop.data = resp.data;
        } else {
          this.$dialog.alert({
            title: "提示",
            message: "所有Cookie均正常!"
          });
        }
      });
    },
    updateCheckCookieCron: function () {
      let param = {cron: this.checkCookie.cron};
      updateCheckCookieCron(param).then(() => {
        this.checkCookie.picker.show = false;
      });
    },
    logout: function () {
      localStorage.removeItem("token");
      this.$router.push("/login")
    },
    updateAccount: function () {
      updateAccount({
        username: this.accountConfig.username,
        password: this.accountConfig.password
      }).then(() => {
      })
      this.accountConfig.actionSheet.show = false;
      setTimeout(() => {
        localStorage.removeItem("token")
        this.$router.push("/login")
      }, 800)
    },
    updateQywx: function () {
      let param = {
        qywxKey: this.qywx.qywxKey
      };
      updateQywx(param).then(() => {
        this.qywx.actionSheet.show = false;
      });
    },
    updateWxPusher: function () {
      let param = {
        appToken: this.wxPusher.appToken,
        adminUid: this.wxPusher.adminUid
      };
      updateWxPusher(param).then(() => {
        this.wxPusher.actionSheet.show = false;
      });
    }
  }
};
</script>

<style scoped></style>
