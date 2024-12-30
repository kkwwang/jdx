<template>
    <div style="height: 100%">
        <van-cell-group inset>
            <van-cell>
                <van-tabs @change="legendTabChange" swipeable v-model="activeTab" class="legend-tab">
                    <van-tab title="登录统计" name="登录统计" />
                    <van-tab
                        :disabled="!isBean && !item.data.length"
                        :key="item.name"
                        v-for="item in beanOption.series"
                        :title="item.name"
                        :name="item.name"
                    />
                </van-tabs>
            </van-cell>
        </van-cell-group>
        <van-cell-group inset>
            <van-cell
                title="日期"
                :value="date"
                @click="show = true"
                v-if="activeTab !== '登录统计'"
            />
            <van-cell v-else title="在线情况">
                {{ envs.length }}/{{ envs.filter(item => item.status === 0).length }}/{{
                    envs.filter(item => item.status !== 0).length
                }}
            </van-cell>
            <van-calendar
                :default-date="new Date(date)"
                v-model:show="show"
                @confirm="getLatestBeanFn"
                :show-confirm="false"
                :max-date="new Date()"
                :formatter="formatter"
                :min-date="
            new Date(new Date().getTime() - 12 * 30 * 24 * 60 * 60 * 1000)
          "
            />
        </van-cell-group>
        <van-cell-group inset>
            <van-cell style="height: calc(100vh - 192px)" ref="mainRef">
            </van-cell>
        </van-cell-group>
    </div>
</template>

<script setup name="BeanManage">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import dayjs from "dayjs";
import * as echarts from "echarts";
import { getAllDate, getAllEnv, getLatestBean } from "@/api/admin/bean";
import legend from "../../legend.json"
import { useRouter } from "vue-router";

const router = useRouter()
let chart;

const date = ref(dayjs().format("YYYY-MM-DD"))
const show = ref(false)
const isBean = ref(false)
const activeTab = ref('登录统计')
const enableDate = ref([])
const envs = ref([])
const lastLoginData = ref([])
const beanData = ref([])

const mainRef = ref()

const chartClick = ref({})

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
        left: "130",
        right: "5",
        bottom: "20"
    },
    yAxis: {
        type: "category",
        axisLabel: {
            interval: 0
        },
        data: []
    },
})


const lastLoginOption = computed(() => {
    return {
        ...commonOptions.value,
        xAxis: {
            type: "value",
            min: -1,
        },
        series: [
            {
                label: {
                    show: true,
                    position: 'inside',
                    fontSize: 10,
                },
                name: "在线时长（天）",
                type: "bar",
                markLine: {
                    symbol: ["none", "none"],
                    label: {
                        position: "middle",
                        formatter: "{b}"
                    },
                    data: [
                        { xAxis: 48, name: "离线警示线", lineStyle: { color: "#ee0a24" } },
                        { xAxis: 72, name: "提示线", lineStyle: { color: "#ff976a" } },
                    ]
                },
                data: lastLoginData.value
            }
        ]
    }
})
const beanOption = computed(() => {
    return {
        ...commonOptions.value,
        xAxis: {
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
        legend: {
            show: false,
            type: "scroll",
            data: legend.map(item => item.title),
            left: "center",
            right: 0,
            icon: "rect",
            selectedMode: "single"
        },
        series: beanData.value
    }
})

const reInit = (option) => {
    if (chart) {
        chart.dispose();
    }
    chart = echarts.init(mainRef.value?.$el, null, { locale: "ZH" });
    if (option) {
        chart.setOption(option);
    }

    chart.on('click', (params) => {
        if (chartClick.value[params.dataIndex + params.seriesName]) {
            router.push('/bean/' + envs.value[params.dataIndex].mobile);
        }

        chartClick.value[params.dataIndex + params.seriesName] = true
        setTimeout(() => {
            chartClick.value[params.dataIndex + params.seriesName] = false
        }, 1000)
    });
}

const formatter = (day) => {
    if (!enableDate.value.includes(dayjs(day.date).format("YYYY-MM-DD"))) {
        day.type = "disabled";
    }
    return day;
}

const getAllDateFn = () => {
    getAllDate().then(res => {
        enableDate.value = res.data;
        date.value = enableDate.value[enableDate.value.length - 1];
        if (envs.value.length) {
            getLatestBeanFn(date.value);
        }
    });
}

const getAllEnvFn = () => {
    getAllEnv().then(res => {
        envs.value = res.data;
        commonOptions.value.yAxis.data = res.data.map(
            (item, index) => item.wechat + (item.status === 1 ? " ❌" : " ✅") + ((index + 1).toString().padStart(2, "0"))
        );
        lastLoginData.value = res.data.map(item => {

            const temp = parseFloat(((new Date() - new Date(item.loginTime)) / 1000 / 60 / 60 / 24).toFixed(2))

            return item.status === 1
                ? {
                    value: temp * -1,
                    itemStyle: {
                        color: "#ee0a24"
                    }
                }
                : temp;
        });
        reInit(lastLoginOption.value);
        if (enableDate.value.length) {
            if (envs.value.length) {
                getLatestBeanFn(date.value);
            }
        }
    });
}

const getLatestBeanFn = (value) => {
    show.value = false;
    value && (date.value = dayjs(value).format("YYYY-MM-DD"));
    getLatestBean(date.value).then(res => {
        const temp = [];

        envs.value.forEach(env => {
            let dataTemp = {};
            res.data.forEach(item => {
                if (env.mobile === item.mobile) {
                    dataTemp = item;
                }
            });
            temp.push({
                ...env,
                ...dataTemp
            });
        });

        beanData.value = legend.map(item => {
            return {
                name: item.title,
                type: "bar",
                // label: {
                //     show: true,
                //     position: 'inside',
                //     fontSize: 10,
                // },
                markLine: {
                    symbol: ["none", "none"],
                    label: {
                        position: "middle",
                        formatter: "{b}:{c}"
                    },
                    data: [
                        { xAxis: item.difference, name: "提示线" },
                        {
                            type: "average",
                            name: "平均值",
                            lineStyle: { color: "#ee0a24" }
                        }
                    ]
                },
                markPoint: {
                    data: [
                        { type: "max", name: "Max" },
                        { type: "min", name: "Min" }
                    ]
                },
                data: temp.map(env => env[item.title])
            };
        });

        if (activeTab.value !== "登录统计") {
            legendTabChange(activeTab.value);
        }
        isBean.value = true;
    });
}

const legendTabChange = (name) => {
    activeTab.value = name;
    nextTick(() => {
        if (!chart) {
            return;
        }
        if (name === "登录统计") {
            reInit(lastLoginOption.value);
        } else {
            reInit(beanOption.value);
            chart.dispatchAction({
                type: "legendToggleSelect",
                name: name
            });
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
    getAllDateFn();
    getAllEnvFn();
    window.addEventListener("resize", () => {
        chart && chart.resize();
    });
})
</script>


<style scoped>
.legend-tab.van-tabs :deep(.van-tabs__content) {
    display: none;
}
</style>
