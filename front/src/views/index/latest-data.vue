<template>
    <van-cell-group inset v-if="envDatas.length">
        <van-tabs swipeable>
            <van-tab
                v-for="envData in envDatas"
                :title="envData.mobile"
                :name="envData.mobile">
                <van-cell-group>
                    <van-cell
                        title="统计时间"
                    > {{ envData.时间 || '-' }}
                    </van-cell>
                    <van-cell
                        :key="index"
                        v-for="(item, index) in legend"
                        :title="item.title"
                        :label="getLatestTip(item, envData[item.title])"
                    >{{ envData[item.title] || '-' }}
                    </van-cell>
                </van-cell-group>
            </van-tab>
        </van-tabs>
    </van-cell-group>
</template>
<script setup>
import legend from "../legend.json"

import { onMounted, ref, watch } from "vue";


const _props = defineProps({
    data: {
        type: Array
    }
})

const envDatas = ref([])

const getLatestTip = (item, value) => {
    if (value - item.difference > 0) {
        return item.tip;
    }
}

const getLatestData = () => {
    const temp = {}
    _props.data.forEach(item => {

        if (!temp[item.mobile]) {
            temp[item.mobile] = []
        }
        temp[item.mobile].push(item)
    })

    for (let tempKey in temp) {
        envDatas.value.push(temp[tempKey][temp[tempKey].length - 1])
    }
}

onMounted(() => {
    watch(() => _props.data, () => {
        getLatestData()
    }, {
        deep: true,
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