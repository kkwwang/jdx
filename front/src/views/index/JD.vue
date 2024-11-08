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

<script setup name="JD">
import { onMounted, ref } from "vue";
import { jdLogin, jdSmsCode } from "@/api";
import { useRoute } from "vue-router";

const expireTime = ref(0)
const form = ref({
    mobile: "",
    code: ""
})

const codeRef = ref()
let route = useRoute()

const smsCode = () => {
    form.value.code = "";
    jdSmsCode(form.value.mobile).then(resp => {
        expireTime.value = resp.data.expireTime * 1000;
        codeRef.value.focus();
        window.localStorage.setItem("mobile", form.value.mobile);
    });
}

const login = () => {
    jdLogin(form.value).then(() => {
        // 计时器清零
        expireTime.value = 0;
        form.value.code = "";
    })
}

onMounted(() => {
    const queryMobile = route.query.mobile
    if (queryMobile) {
        form.value.mobile = queryMobile.split(",")[0];
    } else {
        form.value.mobile = window.localStorage.getItem("mobile") || "";
    }
})

</script>

