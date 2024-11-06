<template>
    <div style="height: 100%;">
        <van-nav-bar title="收益统计">
            <template #left>
                <van-icon
                    color="#ee0a24"
                    name="arrow-left"
                    size="18"
                    @click="$router.push('/')"
                />
            </template>
        </van-nav-bar>
        <van-notice-bar
            left-icon="volume-o"
            text="统计信息仅供参考，以实际到账为准"
            mode="closeable"
        />
        <van-cell-group inset>
            <van-field
                ref="telRef"
                maxlength="11"
                v-model="mobile"
                left-icon="phone-o"
                name="mobile"
                type="tel"
                label="手机号"
                placeholder="手机号"
            >
                <template #button>
                    <van-button size="small" plain type="info" @click="getBean"
                    >查 询
                    </van-button>
                </template>
            </van-field>
        </van-cell-group>
        <van-divider contentPosition="center"
        >{{ account.join() }}
        </van-divider>
        <van-cell-group inset>
            <van-tabs v-model="activeTab" @change="tabChange">
                <van-tab title="最新" name="最新" />
                <van-tab title="趋势图" name="趋势图" />
            </van-tabs>
            <van-empty v-if="!latest.mobile" description="暂无数据" />
            <template v-else>
                <template v-if="activeTab === '趋势图'">
                    <van-cell-group class="echarts-main">
                        <van-tabs
                            @change="legendTabChange"
                            swipeable
                            v-model="activeLegend"
                        >
                            <van-tab
                                :key="item.title"
                                v-for="item in legend"
                                :title="item.title"
                                :name="item.title"
                            />
                        </van-tabs>
                        <van-cell ref="mainRef"></van-cell>
                    </van-cell-group>
                </template>
                <template v-if="activeTab === '最新'">
                    <van-cell-group class="latest-cell">
                        <van-cell
                            title="统计时间"
                            :label="
                new Date() - new Date(latest.时间) > 12 * 60 * 60 * 1000
                  ? '若展示为非最新数据，请过十分钟后再试'
                  : ''
              "
                        >
                            {{
                                latest.时间
                                    ? dayjs(latest.时间)
                                        .format("MM-DD A")
                                        .replace("AM", "早上")
                                        .replace("PM", "晚上")
                                    : "-"
                            }}
                        </van-cell>
                        <van-cell
                            :key="item.title"
                            v-for="item in legend"
                            :title="item.title"
                            :label="getLatestTip(item)"
                        >{{ latest[item.title] || "-" }}
                        </van-cell>
                    </van-cell-group>
                </template>
            </template>
        </van-cell-group>
    </div>
</template>
<script setup name="Bean">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import legend from "../legend.json";
import { useRoute } from "vue-router";
import { jdBean as jdBeanApi } from "@/api";
import * as echarts from "echarts";
import dayjs from "dayjs";

let route = useRoute()

const latest = ref({});
const activeTab = ref("最新");
const mobile = ref("");
const accountName = "账号"
const dataTime = "时间"
const account = ref([])
const activeLegend = ref("")
let chart = null;
const mainRef = ref()

const option = computed(() => {
    return {
        tooltip: {
            trigger: "axis"
        },
        xAxis: {
            type: "time"
        },
        yAxis: {
            axisLabel: {
                showMaxLabel: false,
                showMinLabel: false,
                inside: true
            },
            boundaryGap: ["20%", "20%"],
            min: 0,
            max(value) {
                return Math.ceil(value.max * 1.1);
            },
            minInterval: 1,
            type: "value"
        },
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
        grid: {
            top: "0",
            left: "5",
            right: "5"
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
        series: legend.map(item => {
            return {
                name: item.title,
                type: "line",
                data: []
            };
        })
    }
})

const getBean = () => {

    if (!mobile.value) {
        return;
    }

    jdBeanApi(mobile.value).then(res => {
        let account = new Set();
        const seriesObj = {};
        res.data.forEach(item => {
            account.add(item[accountName]);
            legend.forEach(legendItem => {
                for (let key of Object.keys(item)) {
                    if (legendItem.title === key) {
                        if (!seriesObj[key]) {
                            seriesObj[key] = {
                                name: key,
                                type: "line",
                                markLine: {
                                    data: [{ yAxis: legendItem.difference, name: "提示线" }]
                                },
                                data: []
                            };
                        }
                        seriesObj[key].data.push([
                            new Date(item[dataTime]),
                            item[key]
                        ]);
                    }
                }
            });

            latest.value = item;
        });

        nextTick(() => {
            account.value = Array.from(account);
            init({
                ...option.value,
                series: Object.values(seriesObj)
            });
            legendTabChange(activeLegend.value);
        });
    });
}

const init = optionProp => {
    if (chart != null && chart.dispose) {
        chart.dispose();
    }

    const chartDom = mainRef.value?.$el;
    if (chartDom == null) {
        return;
    }
    chart = echarts.init(chartDom, null, { locale: "ZH" })
    optionProp && chart.setOption(optionProp);
}


onMounted(() => {
    const queryMobile = route.query.mobile
    if (queryMobile) {
        mobile.value = queryMobile.split(",")[0];
    } else {
        mobile.value = window.localStorage.getItem("mobile") || "";
    }

    activeLegend.value = legend[0].title;
    if (mobile.value) {
        getBean();
    }
    window.addEventListener("resize", () => {
        chart && chart.resize();
    });
})

const legendTabChange = (name) => {
    chart &&
    chart.dispatchAction({
        type: "legendToggleSelect",
        name: name
    });
}

const getLatestTip = item => {
    if (latest.value[item.title] - item.difference > 0) {
        return item.tip;
    }
}

const tabChange = (name) => {
    activeTab.value = name;
    if (chart != null && chart.dispose) {
        chart.dispose();
    }
    if (activeTab.value === "趋势图") {
        nextTick(() => {
            if (mobile.value) {
                getBean();
            }
        });
    }
}

onBeforeUnmount(() => {
    if (chart != null && chart.dispose) {
        chart.dispose();
    }
})
</script>


<style scoped>
.latest-cell,
.echarts-main {
    height: calc(100vh - 190px - 44px - 16px);
    overflow-y: auto;
}

.van-cell {
    align-items: center;
}

.echarts-main .van-cell {
    height: calc(100% - 44px);
}

.van-cell-group .van-cell:nth-child(even) {
    background-color: #f5f5f5; /* 奇数行的背景色 */
}

.van-cell__label {
    color: #ee0a24;
    float: left;
    width: 180%;
}
</style>
