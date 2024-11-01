<template>
  <div>
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
    <!--    <div style="text-align: center; margin: 40px 0 20px 0; font-size: 32px">-->
    <!--      收益统计-->
    <!--    </div>-->
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
      >{{ this.account.join() }}
    </van-divider>
    <van-cell-group inset>
      <van-tabs v-model="activeTab" @change="tabChange">
        <van-tab title="最新数据" name="最新数据" />
        <van-tab title="趋势图" name="趋势图" />
      </van-tabs>
      <template v-if="activeTab === '趋势图'">
        <van-cell-group class="echarts-main">
          <van-tabs @change="legendTabChange" swipeable>
            <van-tab
              v-for="item in legend"
              :title="item.title"
              :name="item.title"
            />
          </van-tabs>
          <van-cell ref="main"></van-cell>
        </van-cell-group>
      </template>
      <template v-if="activeTab === '最新数据'">
        <van-cell-group class="latest-cell">
          <van-cell
            v-for="item in legend"
            :title="item.title"
            :label="getLatestTip(item)"
            >{{ latest[item.title] || "-" }}
          </van-cell>
        </van-cell-group>
      </template>
    </van-cell-group>
  </div>
</template>

<script>
import { jdBean } from "@/api";
import * as echarts from "echarts";
import "echarts/i18n/langZH";
import dayjs from "dayjs";
import legend from "./legend.json";

let chart = null;
export default {
  name: "Bean",
  data() {
    return {
      latest: {},
      activeTab: "最新数据",
      mobile: "",
      accountName: "账号",
      account: [],
      dataTime: "时间",
      activeLegend: "",
      legend: legend
    };
  },
  computed: {
    option() {
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
          min: function(value) {
            return Math.floor(value.min * 0.9);
          },
          max: function(value) {
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
          data: this.legend.map(item => item.title),
          left: "center",
          right: 0,
          icon: "rect",
          selectedMode: "single"
        },
        series: this.legend.map(item => {
          return {
            name: item.title,
            type: "line",
            data: []
          };
        })
      };
    }
  },
  mounted() {
    dayjs().format("YYYY-MM-DD 00:00:00 A");
    // this.init();
    this.activeLegend = this.legend[0].title;
    this.mobile = window.localStorage.getItem("mobile") || "";
    if (this.mobile) {
      this.getBean();
    }
    window.addEventListener("resize", () => {
      chart && chart.resize();
    });
  },
  methods: {
    init() {
      const chartDom = this.$refs.main;
      chart = echarts.init(chartDom, null, { locale: "ZH" });
      this.option && chart.setOption(this.option);
      chart.on("legendselectchanged", params => {
        this.activeLegend = params.name;
      });
    },
    tabChange() {
      if (chart != null && chart.dispose) {
        chart.dispose();
      }
      if (this.activeTab === "趋势图") {
        this.$nextTick(() => {
          this.init();

          if (this.mobile) {
            this.getBean();
          }
        });
      }
    },
    getLatestTip(item) {
      if (this.latest[item.title] - item.difference > 0) {
        return item.tip;
      }
    },

    legendTabChange(name) {
      chart.dispatchAction({
        type: "legendToggleSelect",
        name: name
      });
    },
    getBean() {
      if (!this.mobile) {
        return;
      }
      window.localStorage.setItem("mobile", this.mobile);

      jdBean(this.mobile).then(res => {
        let account = new Set();
        const seriesObj = {};
        res.data.forEach(item => {
          account.add(item[this.accountName]);
          for (let key of Object.keys(item)) {
            if (this.legend.map(item => item.title).includes(key)) {
              if (!seriesObj[key]) {
                seriesObj[key] = {
                  name: key,
                  type: "line",
                  data: []
                };
              }
              seriesObj[key].data.push([
                new Date(item[this.dataTime]),
                item[key]
              ]);
            }
          }
          this.latest = item;
        });

        this.account = Array.from(account);

        chart &&
          chart.setOption({
            series: Object.values(seriesObj)
          });
      });
    }
  }
};
</script>

<style scoped>
.latest-cell,
.echarts-main {
  height: calc(100vh - 225px - 44px - 16px);
  overflow-y: auto;
}
.van-cell{
  align-items: center;
}

.echarts-main .van-cell {
  height: calc(100% - 44px);
  //padding: 0!important;
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
