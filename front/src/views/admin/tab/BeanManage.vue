<template>
  <div>
    <van-tabs @change="legendTabChange" swipeable>
      <van-tab title="登录统计" name="登录统计" />
      <van-tab
        :disabled="!isBean"
        :key="item.title"
        v-for="item in legend"
        :title="item.title"
        :name="item.title"
      />
    </van-tabs>
    <van-cell-group inset>
      <van-cell>
        <div style="height: calc(100vh - 100px)" ref="main"></div>
      </van-cell>
    </van-cell-group>
  </div>
</template>
<script>
import { getAllEnv, getLatestBean } from "@/api/admin";
import legend from "../../legend.json";
import * as echarts from "echarts";

let chart;
export default {
  name: "BeanMange",
  data() {
    return {
      isBean: false,
      activeTab: "登录统计",
      data: [],
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
            { type: "average", name: "平均值", lineStyle: { color: "#ee0a24" } }
          ]
        },
        data: []
      }
    };
  },
  computed: {
    lasLoginOption() {
      return {
        tooltip: {
          trigger: "axis"
        },
        yAxis: this.yAxis,
        xAxis: {
          type: "time",
          min: function(value) {
            return Math.ceil(value.min - 6 * 60 * 60 * 1000);
          }
        },
        grid: {
          top: "0",
          left: "130",
          right: "5"
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
          max: function(value) {
            return Math.ceil(value.max * 1.1);
          },
          minInterval: 1,

          type: "value"
        },
        grid: {
          top: "0",
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
      chart = echarts.init(this.$refs.main, null, { locale: "ZH" });
      if (option) {
        chart.setOption(option);
      }
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
        console.log(this.lastLoginTime.data);
        this.reInit(this.lasLoginOption);

        this.getLatestBean();
      });
    },
    getLatestBean() {
      getLatestBean().then(res => {
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
                { xAxis: item.difference, name: "提示线" },
                {
                  type: "average",
                  name: "平均值",
                  lineStyle: { color: "#ff0000" }
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

<style scoped></style>
