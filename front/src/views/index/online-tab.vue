<template>
    <van-cell-group inset v-if="envDatas.length">
        <van-tabs swipeable>
            <van-tab
                v-for="envData in envDatas"
                :title="envData.mobile"
                :name="envData.mobile">
                <van-cell-group inset>
                    <online :dates="envData.dates"></online>
                </van-cell-group>
            </van-tab>
        </van-tabs>
    </van-cell-group>

</template>
<script setup>
import { onMounted, ref, watch } from "vue";
import Online from "@/views/index/online.vue";


const envDatas = ref([])

const _props = defineProps({
    data: {
        type: Array
    }
})


const getOnlineDates = () => {
    const temp = {}
    _props.data.forEach(item => {

        if (!temp[item.mobile]) {
            temp[item.mobile] = []
        }
        temp[item.mobile].push(item)
    })

    for (let tempKey in temp) {
        envDatas.value.push({
            mobile: tempKey,
            dates: temp[tempKey].map(item => item.时间)
        })
    }
}

onMounted(() => {
    watch(() => _props.data, () => {
        getOnlineDates()
    }, {
        deep: true,
        immediate: true
    })
})
</script>


<style scoped>

</style>