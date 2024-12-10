<template>
    <van-cell-group inset>
        <van-tabs
            @change="reInit"
            swipeable
            v-model:active="activeTab"
            class="legend-tab">
            <van-tab
                :key="item.title"
                v-for="item in legend"
                :title="item.title"
                :name="item.title"
            />
        </van-tabs>
    </van-cell-group>
    <van-cell-group inset>
        <van-cell style="height: calc(100vh - 280px)" ref="mainRef" />
    </van-cell-group>
</template>

<script setup name="BeanManage">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts";
import legend from "../legend.json"

let chart;

const show = ref(false)
const activeTab = ref(legend[0].title)
const dataTime = "时间"

const seriesObj = ref({})

const mainRef = ref()

const _props = defineProps({
    data: {
        type: Array
    }
})

const commonOptions = ref({
    tooltip: {
        trigger: "axis",
        axisPointer: {
            type: "cross",
            snap: true
        },
    },
    grid: {
        top: "0",
        left: "55",
        right: "5"
    },
    xAxis: {
        type: "time",
        min(value) {
            return value.min - 24 * 60 * 60 * 1000;
        },
        max(value) {
            return value.max + 24 * 60 * 60 * 1000;
        },
    },
})


const beanOption = computed(() => {
    return {
        ...commonOptions.value,
        dataZoom: [
            {
                type: "inside",
                start: 0,
                end: 100
            },
            {
                start: 0,
                end: 100
            }
        ],
        yAxis: {
            axisLabel: {
                inside: false
            },
            boundaryGap: ["20%", "20%"],
            min: 0,
            max: function (value) {
                return Math.ceil(value.max * 1.1);
            },
            minInterval: 1,

            type: "value"
        },
    }
})

const reInit = () => {
    if (chart) {
        chart.dispose();
    }

    nextTick(() => {
        chart = echarts.init(mainRef.value?.$el, null, { locale: "ZH" });
        const option = ({
            ...beanOption.value,
            series: Object.values(seriesObj.value[activeTab.value])
        })
        chart.setOption(option);
    })

}


const getBeanFn = () => {
    show.value = false;
    seriesObj.value = {}
    legend.forEach(item => {
        if (!seriesObj.value[item.title]) {
            seriesObj.value[item.title] = {}
        }
        _props.data.forEach(itemData => {
            if (!seriesObj.value[item.title][itemData.mobile]) {
                seriesObj.value[item.title][itemData.mobile] = {
                    name: itemData.mobile,
                    type: "line",
                    markLine: {
                        symbol: ["none", "none"],
                        label: {
                            position: "middle",
                            formatter: "{b}:{c}"
                        },
                        data: [
                            { xAxis: item.difference, name: "提示线" },
                        ]
                    },
                    markPoint: {
                        data: [
                            { type: "max", name: "Max" },
                            { type: "min", name: "Min" }
                        ]
                    },
                    data: []
                }
            }

            seriesObj.value[item.title][itemData.mobile].data.push([
                itemData[dataTime],
                parseFloat(itemData[item.title])
            ])
        });
    });
}


onBeforeUnmount(() => {
    if (chart) {
        chart.dispose();
        chart = null;
    }
})

onMounted(() => {
    window.addEventListener("resize", () => {
        chart && chart.resize();
    });

    watch(() => _props.data, () => {
        getBeanFn();
        reInit();
    }, {
        deep: true,
        immediate: true
    })
})


</script>
