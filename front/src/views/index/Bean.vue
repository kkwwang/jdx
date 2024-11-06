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
      >{{ this.account.join() }}
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
            <van-cell ref="main"></van-cell>
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

<script>
import { jdBean } from "@/api";
import * as echarts from "echarts";
import "echarts/i18n/langZH";
import legend from "../legend.json";
import dayjs from "dayjs";

let chart = null;
export default {
  name: "Bean",
  data() {
    return {
      latest: {},
      activeTab: "最新",
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
          min: 0,
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
    if (this.$route.query.mobile) {
      this.mobile = this.$route.query.mobile.split(",")[0];
    } else {
      this.mobile = window.localStorage.getItem("mobile") || "";
    }

    this.activeLegend = this.legend[0].title;
    if (this.mobile) {
      this.getBean();
    }
    window.addEventListener("resize", () => {
      chart && chart.resize();
    });
  },
  beforeDestroy() {
    if (chart != null && chart.dispose) {
      chart.dispose();
    }
  },
  methods: {
    dayjs,
    init() {
      if (chart != null && chart.dispose) {
        chart.dispose();
      }
      const chartDom = this.$refs.main;
      if (chartDom == null) {
        return;
      }
      chart = echarts.init(chartDom, null, { locale: "ZH" });
      this.option && chart.setOption(this.option);
    },
    tabChange() {
      if (chart != null && chart.dispose) {
        chart.dispose();
      }
      if (this.activeTab === "趋势图") {
        this.$nextTick(() => {
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
      chart &&
        chart.dispatchAction({
          type: "legendToggleSelect",
          name: name
        });
    },
    getBean() {
      if (!this.mobile) {
        return;
      }

      jdBean(this.mobile).then(res => {
        let account = new Set();
        const seriesObj = {};
        res.data.forEach(item => {
          account.add(item[this.accountName]);
          this.legend.forEach(legendItem => {
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
                  new Date(item[this.dataTime]),
                  item[key]
                ]);
              }
            }
          });

          this.latest = item;
        });

        this.$nextTick(() => {
          this.account = Array.from(account);
          this.option.series = Object.values(seriesObj);
          this.init();
          this.legendTabChange(this.activeLegend);
        });
      });
    }
  }
};
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
