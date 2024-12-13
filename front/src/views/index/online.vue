<template>
    <van-calendar
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
import { computed } from "vue";

const defaultDate = computed(() => {
    return _props.dates.map(item => new Date(item)).sort((a, b) => new Date(b) - new Date(a))
})

const _props = defineProps({
    dates: {
        type: Array
    }
})
const formatter = (day) => {
    if (_props.dates.includes(dayjs(day.date).format("YYYY-MM-DD"))) {
        day.bottomInfo = "在线";
        day.className = "online";
    } else if (day.date < new Date()) {
        day.bottomInfo = "离线";
        day.className = "offline";
    }
    return day;
}

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