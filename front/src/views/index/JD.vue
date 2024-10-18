<template>
  <div>
    <div>
      <van-field
        ref="telRef"
        maxlength="11"
        v-model="form.mobile"
        left-icon="phone-o"
        name="mobile"
        type="tel"
        label="手机号"
        placeholder="手机号"
      ></van-field>
      <van-field
        maxlength="6"
        ref="codeRef"
        v-model="form.code"
        left-icon="shield-o"
        name="code"
        type="number"
        label="验证码"
        placeholder="验证码"
        @change="codeChange"
      >
        <template #button>
          <van-count-down
            v-if="Number(expireTime) > 0"
            ref="countDown"
            :time="expireTime"
            format="ss"
          />
          <van-button v-else size="small" plain type="info" @click="smsCode"
            >发送验证码
          </van-button>
        </template>
      </van-field>

      <div style="margin: 16px; ">
        <van-button
          round
          block
          :disabled="!form.code"
          type="primary"
          @click="login"
        >
          登录
        </van-button>
      </div>
    </div>
  </div>
</template>
<script>
import { jdLogin, jdSmsCode } from "@/api";

export default {
  data() {
    return {
      expireTime: 0,
      form: {
        mobile: "",
        code: ""
      }
    };
  },
  mounted() {
    this.form.mobile = window.localStorage.getItem("mobile") || "";
  },
  methods: {
    codeChange: function() {
      if(this.form.code.length === 6){
        this.login();
      }
    },
    smsCode: function() {
      this.form.code = "";
      jdSmsCode(this.form.mobile).then(resp => {
        this.expireTime = resp.data.expireTime * 1000;
        this.$refs.codeRef.focus();
        window.localStorage.setItem("mobile", this.form.mobile);
      });
    },
    login: async function() {
      let _this = this;
      await jdLogin(this.form)
        .then(function(response) {
          // 计时器清零
          _this.expireTime = 0;
          _this.form.code = "";
          console.log(response);
        })
        .catch(function(error) {
          console.error(error);
        });
    }
  }
};
</script>
