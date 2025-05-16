<template>
    <van-calendar
        v-if="loginLog != null"
        :title="'累计在线天数：' + dates.length"
        type="single"
        :default-date="defaultDate"
        switch-mode="year-month"
        color="#07c160"
        :formatter="formatter"
        :max-date="new Date()"
        :min-date="defaultDate[defaultDate.length - 1]"
        :poppable="false"
        :show-confirm="false"
        @select="calendarSelect"
    />

        <van-list
            finished-text="没有更多了"
        >
            <van-cell v-for="item in allDatas.filter(a => a.login_day === selectDate)" :key="item" :title="item.type === '1' ? '在线' : '离线'" :class="item.type === '1' ? 'online' : 'offline'">
                {{ item.login_day }} {{ item.login_time }}
            </van-cell>
        </van-list>
</template>
<script setup>
import dayjs from "dayjs";
import { computed, getCurrentInstance, onMounted, ref } from "vue";
import { getLoginLog } from "@/api";

const loginLog = ref(null)
const logoutLog = ref(null)

const allDatas = ref([])
const selectDate = ref(null)

const defaultDate = computed(() => {
    return _props.dates.map(item => new Date(item)).sort((a, b) => new Date(b) - new Date(a))
})

const _props = defineProps({
    dates: {
        type: Array
    },
    mobile: {
        type: String
    }
})
const formatter = (day) => {
    const dayStr = dayjs(day.date).format("YYYY-MM-DD")
    if (_props.dates.includes(dayStr)) {
        day.className = "online";
    } else if (day.date < new Date()) {
        day.className = "offline";
    }

    if (loginLog.value[dayStr]) {
        day.className = "online";
        day.bottomInfo = loginLog.value[dayStr][0] + "✅";
    }

    if (logoutLog.value[dayStr]) {
        day.className = "online";
        day.topInfo = logoutLog.value[dayStr][0] + "❌";
    }


    return day;
}

const calendarSelect = (data) => {
    const dayStr = dayjs(data).format("YYYY-MM-DD")
    selectDate.value = dayStr
    console.log(logoutLog.value[dayStr])
    console.log(loginLog.value[dayStr])
}

onMounted(() => {
    getCurrentInstance().proxy.$setTitle("在线日历 " + _props.mobile)

    getLoginLog(_props.mobile).then(res => {
        const loginResult = {}
        const logoutResult = {}
        allDatas.value = res.data
        res.data.forEach(item => {
            if (item.type === "1") {
                if (!loginResult[item.login_day]) {
                    loginResult[item.login_day] = []
                }
                loginResult[item.login_day].push(item.login_time)
            }

            if (item.type === "0") {
                if (!logoutResult[item.login_day]) {
                    logoutResult[item.login_day] = []
                }
                logoutResult[item.login_day].push(item.login_time)
            }

        })
        loginLog.value = loginResult
        logoutLog.value = logoutResult
    })
})

</script>
<style scoped>
::v-deep(.van-calendar__day.online) {
    color: var(--van-success-color) !important;
}

::v-deep(.van-calendar__day.offline) {
    color: var(--van-field-error-message-color) !important;
}

::v-deep(.online .van-cell__title){
    color: var(--van-success-color) !important;
}

::v-deep(.offline .van-cell__title){
    color: var(--van-field-error-message-color) !important;
}
</style>