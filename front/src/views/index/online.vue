<template>
    <van-calendar
        v-if="loginLog != null"
        :title="'累计在线天数：' + dates.length"
        type="multiple"
        :default-date="defaultDate"
        switch-mode="year-month"
        color="#07c160"
        :formatter="formatter"
        :max-date="new Date()"
        :min-date="defaultDate[defaultDate.length - 1]"
        :poppable="false"
        readonly
        :show-confirm="false"
        :style="{ height: '500px' }"
    />
</template>
<script setup>
import dayjs from "dayjs";
import { computed, onMounted, ref } from "vue";
import { getLoginLog } from "@/api";

const loginLog = ref(null)
const logoutLog = ref(null)

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

onMounted(() => {
    getLoginLog(_props.mobile).then(res => {
        const loginResult = {}
        const logoutResult = {}
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
    background: none !important;
    color: var(--van-success-color) !important;
}

::v-deep(.van-calendar__day.offline) {
    background: none !important;
    color: var(--van-field-error-message-color) !important;
}
</style>