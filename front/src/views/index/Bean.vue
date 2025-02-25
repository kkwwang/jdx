<template>
    <div style="height: 100%;">
        <van-nav-bar title="收益统计">
            <template #left>
                <van-icon
                    color="#ee0a24"
                    name="arrow-left"
                    size="18"
                    @click="$router.push({ path: '/' + (searchMobile || '')})"
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
                v-model="searchMobile"
                left-icon="phone-o"
                name="mobile"
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
        <van-cell-group inset>
            <van-tabs v-model:active="activeTab">
                <van-tab title="最新" name="最新">
                    <latest-data :data="data" v-if="activeTab === '最新'" />
                </van-tab>
                <van-tab title="趋势图" name="趋势图">
                    <bean-chart :data="data" v-if="activeTab === '趋势图'" />
                </van-tab>
                <van-tab title="在线日历" name="online">
                    <online-tab :data="data" v-if="activeTab === 'online'" />
                </van-tab>
            </van-tabs>
        </van-cell-group>
    </div>
</template>
<script setup name="Bean">
import { onMounted, ref, watch } from "vue";
import { jdBean as jdBeanApi } from "@/api";
import BeanChart from "@/views/index/bean-chart.vue";
import LatestData from "@/views/index/latest-data.vue";
import OnlineTab from "@/views/index/online-tab.vue";

const _props = defineProps({
    mobile: String,
    type: String
})

const beanActiveTabName = 'beanActiveTabName';
const searchMobile = ref("");
const data = ref([])
const activeTab = ref(window.localStorage.getItem(beanActiveTabName) || '最新')


const getBean = () => {
    if (!searchMobile.value) {
        return;
    }

    jdBeanApi(searchMobile.value).then(res => {
        data.value = res.data
    });
}


onMounted(() => {


    if (_props.mobile && !searchMobile.value) {
        searchMobile.value = _props.mobile;
    }

    if (searchMobile.value) {
        getBean();
    }

    if (_props.type) {
        activeTab.value = _props.type
    }
})

watch(() => activeTab.value, () => {
    window.localStorage.setItem(beanActiveTabName, activeTab.value)
})

</script>


<style scoped>
.latest-cell,
.echarts-main {
    overflow-y: auto;
}

.latest-cell {
    height: calc(100vh - 190px - 16px);
}

.echarts-main {
    height: calc(100vh - 190px - 44px - 16px);
}

.van-cell.van-field {
    align-items: center;
}

.echarts-main .van-cell {
    height: calc(100% - 44px);
}

.van-cell-group .van-cell:nth-child(even) {
    background-color: #f5f5f5; /* 奇数行的背景色 */
}

:deep(.van-cell__label) {

    color: #ee0a24;
    float: left;
    width: 180%;
}
</style>
