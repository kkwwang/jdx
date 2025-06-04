<template>
    <van-cell-group>
        <van-cell
            title="统计时间"
            :value="date"
            @click="openCanlendar"
        >
        </van-cell>
        <template
            v-for="(item, index) in legend"
            :key="index"
        >
            <template v-if="activeData[item.title]">
                <van-cell
                    :title="item.title"
                    :label="getLatestTip(item, activeData[item.title])"
                >{{ activeData[item.title] }}
                </van-cell>
            </template>
        </template>
    </van-cell-group>
    <van-calendar
        root-portal
        :default-date="new Date(date)"
        v-model:show="show"
        @confirm="getLatestBeanFn"
        :show-confirm="false"
        :max-date="new Date()"
        :formatter="formatter"
        :min-date="new Date(new Date().getTime() - 12 * 30 * 24 * 60 * 60 * 1000)"
        teleport="body"
    />
</template>

<script setup name="latestDataItem">
import legend from "../legend.json"
import { defineProps, onMounted, ref, watch } from "vue";
import dayjs from "dayjs";

const activeData = ref({})

const show = ref(false)
const date = ref(dayjs().format("YYYY-MM-DD"))
const enableDate = ref([])

const _props = defineProps({
    envData: {
        type: Array
    }
})

const openCanlendar = () => {
    show.value = true;
    enableDate.value = _props.envData.map(item => item.时间)
}

const getLatestBeanFn = (value) => {
    show.value = false;
    if (!value) {
        activeData.value = _props.envData[_props.envData.length - 1] || {}
        date.value = activeData.value.时间
    } else {
        date.value = dayjs(value).format("YYYY-MM-DD")
        activeData.value = _props.envData.filter(item => item.时间 === dayjs(value).format("YYYY-MM-DD"))?.[0] || {}
    }

}
const formatter = (day) => {
    if (!enableDate.value.includes(dayjs(day.date).format("YYYY-MM-DD"))) {
        day.type = "disabled";
    }
    return day;
}
const getLatestTip = (item, value) => {
    if (value - item.difference > 0) {
        return item.tip;
    }
}

onMounted(() => {
    if (_props.envData) {
        getLatestBeanFn()
    }
})

watch(() => _props.envData, () => {
    if (_props.envData) {
        getLatestBeanFn()
    }
})

</script>


<style scoped>

</style>