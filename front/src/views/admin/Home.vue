<template>
  <div style="background-color: #f6f6f6;  height: 100%;">
    <van-tabs v-model="active">
      <van-tab title="青龙管理">
        <QLManage />
      </van-tab>
      <van-tab title="系统设置">
        <SystemConfig />
      </van-tab>
      <van-tab title="资产统计">
        <BeanMange />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script>
import QLManage from "@/views/admin/tab/QLManage";
import SystemConfig from "@/views/admin/tab/SystemConfig";
import BeanMange from "@/views/admin/tab/BeanManage.vue";

export default {
  name: "Home",
  components: { BeanMange, QLManage, SystemConfig },
  data() {
    return {
      active: 0
    };
  },
  methods: {}
};
</script>

<style scoped>
>>>.van-tabs__content{
  padding: 10px 0;
}
</style>
