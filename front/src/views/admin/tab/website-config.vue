<template>
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

    <van-cell-group inset>
        <van-field v-model="websiteConfig.title" label="网站标题" placeholder="网站标题" />

        <van-field
            v-model="websiteConfig.notice"
            label="公告"
            :rows="2"
            type="textarea"
            placeholder="公告"
        />
        <van-field
            v-model="websiteConfig.indexBottomNotice"
            label="底部公告"
            :rows="5"
            type="textarea"
            placeholder="底部公告"
        />

        <van-cell title="操作">
            <div style="text-align: right">
                <van-button size="small" plain hairline style="margin-left: 5px;" type="info" @click="save()">保存</van-button>
            </div>
        </van-cell>
    </van-cell-group>


</template>

<script setup name="website-config">

import { getWebsiteConfigApi, updateWebsiteConfigApi } from "@/api/admin/website";
import { onMounted, ref } from "vue";
import { showToast } from "vant";

const websiteConfig = ref({
    title: "",
    notice: "",
    indexBottomNotice: ""
});

const get = () => {
    getWebsiteConfigApi().then(res => {
        websiteConfig.value = res.data
    })
};

const save = () => {
    updateWebsiteConfigApi(websiteConfig.value).then(() => {
        showToast({
            message: '保存成功',
            duration: 500
        })
    })
}

onMounted(() => {
    get()
})

</script>

<style scoped>

</style>