<template>
    <van-divider
        :style="{
        color: '#1989fa',
        borderColor: '#1989fa',
        padding: '0 16px',
        marginTop: '12px'
      }"
    >账号关联
    </van-divider>
    <van-field
        v-model="mainAcount"
        is-link
        readonly
        label="主账户"
        placeholder="选择主账户"
        @click="showMainPicker = true"
    />

    <van-popup v-model:show="showMainPicker" destroy-on-close round position="bottom">
        <van-picker
            :columns="columns"
            @cancel="showMainPicker = false"
            @confirm="onConfirm"

        />
    </van-popup>

    <van-field
        is-link
        readonly
        label="关联账户"
        placeholder="选择关联账户"
        @click="showOtherPicker = true"
    />
    <van-popup v-model:show="showOtherPicker" destroy-on-close round position="bottom">
        <van-picker
            :columns="columns"
            @cancel="showOtherPicker = false"
            @confirm="onOtherConfirm"
        />
    </van-popup>
    <van-cell
        is-link
        center
        v-for="item in envs.filter(env => otherAcount.includes(env.mobile))"
        :title="item.wechat"
        :label="item.mobile"
        @click="del(item)"
        value="删除" />
    <van-cell title="操作">
        <div style="text-align: right">
            <van-button size="small" plain hairline style="margin-left: 5px;" type="info" @click="save()">保存</van-button>
        </div>
    </van-cell>
    <!-- 系统控制 -->
    <sys-controller />
</template>
<script setup>
import { getAllEnv } from "@/api/admin/bean";

import SysController from "@/views/admin/tab/sys-controller.vue";
import { onMounted, ref } from "vue";
import { showConfirmDialog, showToast } from "vant";
import { acountLink } from "@/api/admin/ql";

const mainAcount = ref("");
const otherAcount = ref([]);
const showMainPicker = ref(false);
const showOtherPicker = ref(false);
const envs = ref([]);
const columns = ref([]);

const getAllEnvFn = () => {
    getAllEnv().then(res => {
        envs.value = res.data;
        columns.value = res.data.map(item => {
            return {
                text: `${item.wechat}（${item.mobile}）`,
                value: item.mobile
            }
        });
    });
}

const del = (item) => {
    showConfirmDialog({
        title: '确认',
        message: '确认删除该条记录？',
    }).then(() => {
        otherAcount.value = otherAcount.value.filter(one => one !== item.mobile)
    })
}
const onConfirm = ({ selectedValues, selectedOptions }) => {
    showMainPicker.value = false;
    mainAcount.value = selectedOptions[0].value;

    otherAcount.value = envs.value.filter(env => env.mobile === selectedOptions[0].value)[0].notifyMobile;
};

const onOtherConfirm = ({ selectedValues, selectedOptions }) => {
    showOtherPicker.value = false;
    otherAcount.value.push(selectedOptions[0].value);
};

const save = () => {
    showConfirmDialog({
        title: '确认',
        message: '确认保存该条记录？',
    }).then(() => {
        const data = {
            mainAcount: mainAcount.value,
            otherAcount: otherAcount.value
        }
        acountLink(data).then(() => {
            showToast({
                message: '保存成功',
                duration: 500
            });
            getAllEnvFn();

        })
    })
}

onMounted(() => {
    getAllEnvFn()
})
</script>

<style scoped>

</style>