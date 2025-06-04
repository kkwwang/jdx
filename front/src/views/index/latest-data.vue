<template>
    <van-cell-group inset v-if="Object.keys(envDatas).length">
        <van-tabs v-model:active="activeTab" swipeable>
            <van-tab
                v-for="(envData, mobile) in envDatas"
                :title="mobile"
                :name="mobile">
            </van-tab>
        </van-tabs>
        <latest-data-item v-if="activeTab" :envData="envDatas[activeTab]" />
    </van-cell-group>
</template>
<script setup>

import { getCurrentInstance, onMounted, ref, watch } from "vue";
import LatestDataItem from "@/views/index/latest-data-item.vue";


const latestDataActiveTabName = 'latestDataActiveTabName'
const { proxy } = getCurrentInstance()

const activeTab = ref(window.localStorage.getItem(latestDataActiveTabName))
const _props = defineProps({
    data: {
        type: Array
    }
})

const envDatas = ref([])


const getLatestData = () => {
    const temp = {}
    envDatas.value = []
    _props.data?.forEach(item => {

        if (!temp[item.mobile]) {
            temp[item.mobile] = []
        }
        temp[item.mobile].push(item)
    })
    envDatas.value = temp
}

onMounted(() => {

    watch(() => _props.data, () => {
        getLatestData()
    }, {
        deep: true,
        immediate: true
    })

    watch(() => activeTab.value, () => {
        proxy.$setTitle("最近收益 " + activeTab.value);

        window.localStorage.setItem(latestDataActiveTabName, activeTab.value)
    }, {
        immediate: true
    })
})

</script>
<style scoped>
:deep(.van-tabs) {
    height: 100%;
}

:deep(.van-tabs__content) {
    height: calc(100% - 44px);
    overflow-y: auto;
}
</style>