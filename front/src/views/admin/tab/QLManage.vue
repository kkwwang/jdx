<template>
    <div>
        <van-button style="margin-left: 20px" type="primary" size="small" plain hairline icon="apps-o" @click="addQLConfig()">新增配置</van-button>
        <van-empty v-if="qlConfigList.length === 0" description="暂无配置" />
        <template v-for="(qlConfig, index) in qlConfigList">
            <ql-item :ql-config="qlConfig" :index="index" @changed="qlItemChanged"></ql-item>
        </template>
        <ql-item v-if="newQlConfig" :ql-config="newQlConfig" add @changed="qlItemChanged"></ql-item>
        <other />
    </div>
</template>
<script setup name="QLManage">
import QlItem from "@/views/admin/tab/ql-item.vue";
import { onMounted, ref } from "vue";
import Other from "@/views/admin/other.vue";
import { getQLConfigList as getQLConfigListApi } from "@/api/admin/ql";

const newQlConfig = ref(null)
const qlConfigList = ref([])

const addQLConfig = () => {
    newQlConfig.value = {
        displayName: "",
        clientId: "",
        clientSecret: "",
        max: 30,
        disabled: 0,
        edit: true
    }
}

const getQLConfigList = () => {
    qlConfigList.value = []
    getQLConfigListApi().then(resp => {
        qlConfigList.value = resp.data;
    });
}

const qlItemChanged = (resData) => {
    if (resData) {

        qlConfigList.value = resData
    }
    newQlConfig.value = null

}

onMounted(() => {
    getQLConfigList()
})
</script>
