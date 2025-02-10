<template>
    <van-cell-group inset>
        <van-tabs
            v-if="legendData.length"
            @change="reInit"
            swipeable
            v-model:active="activeTab"
            class="legend-tab">
            <van-tab
                :key="item.title"
                v-for="(item, index) in legendData"
                :title="item.title"
                :name="index"
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
const beanChartActiveTabName = 'beanChartActiveTabName';

const show = ref(false)
const activeTab = ref(parseInt(window.localStorage.getItem(beanChartActiveTabName)) || 0)
const dataTime = "时间"
const legendData = ref([])


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

const reInit = (index) => {
    if (chart) {
        chart.dispose();
    }

    const series = legendData.value[index]?.data
    if (!series) {
        return
    }

    nextTick(() => {
        chart = echarts.init(mainRef.value?.$el, null, { locale: "ZH" });
        const option = ({
            ...beanOption.value,
            series: Object.values(legendData.value[index]?.data)
        })
        chart.setOption(option);
    })
}


const getBeanFn = () => {
    show.value = false;
    legend.forEach(item => {
        const temp = {}

        let allNan = true
        _props.data.forEach(itemData => {
            if (!temp[itemData.mobile]) {
                temp[itemData.mobile] = {
                    name: itemData.mobile,
                    type: "line",
                    smooth: true,
                    symbol: 'none',
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
                            { type: "min", name: "Min" },
                            {
                                coord: null,
                                value: 0,
                            }
                        ]
                    },
                    data: []
                }
            }

            const itemValue = [
                itemData[dataTime],
                parseFloat(itemData[item.title])
            ]
            if (!isNaN(parseFloat(itemData[item.title]))) {
                allNan = false
            }
            if (itemData[item.title]) {
                temp[itemData.mobile].markPoint.data[2].value = itemValue[1]
                temp[itemData.mobile].markPoint.data[2].coord = itemValue
            }

            temp[itemData.mobile].data.push(itemValue)
        });

        if (!allNan) {
            legendData.value.push({
                ...item,
                data: temp
            })
        }

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
        reInit(0);
    }, {
        deep: true,
        immediate: true
    })
})


watch(() => activeTab.value, () => {
    window.localStorage.setItem(beanChartActiveTabName, activeTab.value)
})
</script>
