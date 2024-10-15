<template>
    <div>
        <div v-if="!haveCookie">
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
<!--                <van-button-->
<!--                    style="margin-top: 8px"-->
<!--                    round-->
<!--                    block plain-->
<!--                    type="info"-->
<!--                    @click="haveCookie = true"-->
<!--                >-->
<!--                    已有Cookie？-->
<!--                </van-button>-->
            </div>
        </div>
        <div v-else>
            <van-field
                v-model="cookieForm.cookie"
                left-icon="user-o"
                name="cookie"
                label="Cookie"
                placeholder="pt_key=xxx;pt_pin=xxx;"
            ></van-field>
            <van-field
                ref="telRef"
                v-model="form.mobile"
                left-icon="phone-o"
                name="mobile"
                type="tel"
                label="手机号"
                placeholder="手机号"
            ></van-field>
            <div style="margin: 16px; ">
                <van-button
                    round
                    block
                    :disabled="!(cookieForm.cookie )"
                    type="primary"
                    @click="submitCk"
                >
                    提交
                </van-button>
                <van-button
                    style="margin-top: 8px"
                    round
                    block plain
                    type="info"
                    @click="haveCookie = false"
                >
                    返回获取验证码
                </van-button>
            </div>
        </div>

        <van-dialog v-model="wxPusher.show" title="扫码关注获得最新消息" show-cancel-button>
            <img :src="wxPusher.qr" width="100%"/>
            <div style="padding: 4px 32px;text-align: center">扫描完成后请在公众号关注是否已经完成绑定</div>
        </van-dialog>
    </div>
</template>
<script>
import { baseInfo, jdLogin, jdSmsCode, submitCk } from "@/api";

export default {
    data() {
        return {
            expireTime: 0,
            haveCookie: false,
            remain: 0,
            form: {
                mobile: "",
                code: ""
            },
            cookieForm: {
                mobile: "",
                cookie: ''
            },
            wxPusher: {
                qr: '',
                show: false
            }
        };
    },
    mounted() {
        this.renderBase();
        this.form.mobile = window.localStorage.getItem("mobile") || "";
    },
    methods: {
        renderBase: function () {
            baseInfo()
                .then(resp => {
                    this.title = resp.data.title;
                    this.notice = resp.data.notice;
                    this.remain = resp.data.remain;
                    setTimeout(this.$refs.telRef.focus, 500)
                })
                .catch(err => {
                    console.log(err);
                });
        },
        smsCode: function () {
            this.form.code = "";
            jdSmsCode(this.form.mobile).then(resp => {
                this.expireTime = resp.data.expireTime * 1000;
                this.$refs.codeRef.focus();
                window.localStorage.setItem("mobile", this.form.mobile);
            });
        },
        login: async function () {
            let _this = this;
            if (_this.model == "ck") {
                this.form.displayName = "";
                this.form.remark = "";
            }
            await jdLogin(this.form)
                .then(function (response) {
                    // 计时器清零
                    _this.expireTime = 0;
                    localStorage.setItem("ptPin", response.data.ptPin);
                    _this.form.code = ""
                    _this.$copyText(response.data.cookie)
                    _this.cookieForm.cookie = response.data.cookie
                    _this.ifPushToQL()

                    // 弹框
                    // _this.$dialog
                    //     .alert({
                    //         title: "提示",
                    //         message: response.data.cookie,
                    //         confirmButtonText: "点击复制并提交"
                    //     })
                    //     .then(() => {
                    //         _this
                    //             .$copyText(response.data.cookie)
                    //             .then(() => {
                    //                 _this.$toast.success("复制成功");
                    //                 if (_this.remain > 0) {
                    //                     _this.ifPushToQL()
                    //                 }
                    //             })
                    //             .catch((e) => {
                    //                 console.error(e)
                    //                 _this.$toast.fail("复制失败，请手动复制");
                    //             });
                    //     });
                })
                .catch(function (error) {
                    console.error(error);
                });

        },
        ifPushToQL: function () {
            this.doSubmitCk()
            // setTimeout(() => {
            //   this.$dialog
            //       .confirm({
            //         title: "提示",
            //         message: "是否提交青龙？",
            //         confirmButtonText: "提交",
            //         cancelButtonText: "不了"
            //       }).then(() => {
            //     this.doSubmitCk()
            //   }).catch(() => {
            //   })
            // }, 300)
        },
        ifShowBindWxPusher: function () {
            this.$dialog
                .confirm({
                    title: "提示",
                    message: "是否启用一对一推送？",
                    confirmButtonText: "启用",
                    cancelButtonText: "不了"
                }).then(() => {
                this.wxPusher.show = true
            }).catch(() => {
            })
        },
        submitCk: async function () {
            await this.doSubmitCk()
        },
        doSubmitCk: function () {

            this.cookieForm.mobile = this.form.mobile

            submitCk(this.cookieForm).then((resp) => {
                this.wxPusher.qr = resp.data.dynamicWxPusherQRCode
                // let _this = this;
                // setTimeout(() => {
                //   _this.ifShowBindWxPusher()
                // }, 300)
            })
        }
    }
};
</script>
