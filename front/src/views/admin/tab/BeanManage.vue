<template>
  <div style="height: 100%">
    <van-cell-group inset>
      <van-cell>
        <van-tabs @change="legendTabChange" swipeable v-model="activeTab" class="legend-tab">
          <van-tab title="登录统计" name="登录统计"/>
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
          v-model="show"
          @confirm="getLatestBean"
          :show-confirm="false"
          :max-date="new Date()"
          :formatter="formatter"
          :min-date="
            new Date(new Date().getTime() - 12 * 30 * 24 * 60 * 60 * 1000)
          "
      />
    </van-cell-group>
    <van-cell-group inset>
      <van-cell>
        <div style="height: calc(100vh - 192px)" ref="main"></div>
      </van-cell>
    </van-cell-group>
  </div>
</template>
<script>
import {getAllDate, getAllEnv, getLatestBean} from "@/api/admin";
import legend from "../../legend.json";
import * as echarts from "echarts";
import dayjs from "dayjs";

let chart;
export default {
  name: "BeanMange",
  data() {
    return {
      date: dayjs().format("YYYY-MM-DD"),
      show: false,
      isBean: false,
      activeTab: "登录统计",
      data: [],
      enableDate: [],
      envs: [],

      legend: legend,
      yAxis: {
        type: "category",
        // inverse: true,
        axisLabel: {
          interval: 0
        },
        data: []
      },
      lastLoginTime: {
        name: "最后登录时间",
        type: "bar",
        markLine: {
          symbol: ["none", "none"],
          label: {
            position: "middle",
            formatter: "{b}"
          },
          data: [
            //{xAxis: item.difference, name: "提示线"},
            {type: "average", name: "平均值", lineStyle: {color: "#ee0a24"}}
          ]
        },
        data: []
      }
    };
  },
  computed: {
    dayjs() {
      return dayjs;
    },
    lasLoginOption() {
      return {
        tooltip: {
          trigger: "axis"
        },
        yAxis: this.yAxis,
        xAxis: {
          type: "time",
          min: function (value) {
            return Math.ceil(value.min - 6 * 60 * 60 * 1000);
          }
        },
        grid: {
          top: "0",
          left: "130",
          right: "5",
          bottom: "0"
        },
        series: [this.lastLoginTime]
      };
    },
    beanOption() {
      return {
        tooltip: {
          trigger: "axis"
        },
        yAxis: this.yAxis,
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
        grid: {
          top: "0",
          bottom: 0,
          left: "130",
          right: "5"
        },
        legend: {
          show: false,
          type: "scroll",
          data: this.legend.map(item => item.title),
          left: "center",
          right: 0,
          icon: "rect",
          selectedMode: "single"
        },
        series: this.legend.map(item => {
          return {
            name: item.title,
            type: "bar",
            data: []
          };
        })
      };
    }
  },
  beforeDestroy() {
    chart = null;
  },
  mounted() {
    this.getAllDate();
    this.getAllEnv();

    window.addEventListener("resize", () => {
      chart && chart.resize();
    });
  },
  methods: {
    reInit(option) {
      if (chart) {
        chart.dispose();
      }
      chart = echarts.init(this.$refs.main, null, {locale: "ZH"});
      if (option) {
        chart.setOption(option);
      }
    },
    formatter(day) {
      if (!this.enableDate.includes(dayjs(day.date).format("YYYY-MM-DD"))) {
        day.type = "disabled";
      }
      return day;
    },
    getAllDate() {
      getAllDate().then(res => {
        this.enableDate = res.data;
        this.date = this.enableDate[this.enableDate.length - 1];
        if (this.envs.length) {
          this.getLatestBean(this.date);
        }

      });
    },
    getAllEnv() {
      getAllEnv().then(res => {
        this.envs = res.data;
        this.yAxis.data = res.data.map(
            item => item.wechat + (item.status === 1 ? " ❌" : " ✅")
        );
        this.lastLoginTime.data = res.data.map(item => {
          return item.status === 1
              ? {
                value: item.loginTime,
                itemStyle: {
                  color: "#ee0a24"
                }
              }
              : item.loginTime;
        });
        this.reInit(this.lasLoginOption);
        if (this.enableDate.length) {
          if (this.envs.length) {
            this.getLatestBean(this.date);
          }
        }
      });
    },
    getLatestBean(value) {
      this.show = false;
      value && (this.date = dayjs(value).format("YYYY-MM-DD"));
      getLatestBean(this.date).then(res => {
        const temp = [];

        this.envs.forEach(env => {
          let dataTemp = {};
          res.data.forEach(item => {
            if (env.ptPin === item.账号) {
              dataTemp = item;
            }
          });
          temp.push({
            ...env,
            ...dataTemp
          });
        });

        console.log(temp)

        this.beanOption.series = this.legend.map(item => {
          return {
            name: item.title,
            type: "bar",
            markLine: {
              symbol: ["none", "none"],
              label: {
                position: "middle",
                formatter: "{b}:{c}"
              },
              data: [
                {xAxis: item.difference, name: "提示线"},
                {
                  type: "average",
                  name: "平均值",
                  lineStyle: {color: "#ff0000"}
                }
              ]
            },
            markPoint: {
              data: [
                {type: "max", name: "Max"},
                {type: "min", name: "Min"}
              ]
            },
            data: temp.map(env => env[item.title]).filter(item => item)
          };
        });

        console.log(this.beanOption.series)
        if (this.activeTab !== "登录统计") {
          this.legendTabChange(this.activeTab);
        }
        this.isBean = true;
      });
    },
    legendTabChange(name) {
      this.activeTab = name;
      this.$nextTick(() => {
        if (!chart) {
          return;
        }
        if (name === "登录统计") {
          this.reInit(this.lasLoginOption);
        } else {
          console.log(this.beanOption)
          this.reInit(this.beanOption);
          chart.dispatchAction({
            type: "legendToggleSelect",
            name: name
          });
        }
      });
    }
  }
};
</script>

<style scoped>
.legend-tab.van-tabs :deep(.van-tabs__content) {
  display: none;
}
</style>
