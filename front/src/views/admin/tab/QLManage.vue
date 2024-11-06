<template>
  <div>
    <div style="margin-left: 20px">
      <van-popover
          v-model="qlOptPopover"
          placement="right-start"
          trigger="click">
        <div style="width: 96px;text-align: center">
          <van-button style="border: none; margin: 4px 0" type="default" @click="showSaveQLConfig()">新增配置
          </van-button>
        </div>
        <template #reference>
          <van-button type="primary" icon="apps-o" size="small">操作</van-button>
        </template>
      </van-popover>
    </div>

    <van-empty v-if="qlConfigList.length == 0" description="暂无配置"/>

    <van-swipe-cell
        v-for="qlConfig in qlConfigList"
        :key="qlConfig.displayName"
        :title="qlConfig.displayName"
        style="margin-bottom: 20px"
    >
      <van-cell-group :title="qlConfig.displayName" inset>
        <van-cell title="地址">
          <template #default>
            {{ qlConfig.url }}
          </template>
        </van-cell>
        <van-cell title="客户端ID">
          <template #default>
            {{ qlConfig.clientId }}
          </template>
        </van-cell>
        <van-cell title="客户端密钥">
          <template #default>
            {{ qlConfig.clientSecret }}
          </template>
        </van-cell>
        <van-cell title="最大数量">
          <template #default>
            {{ qlConfig.max }}
          </template>
        </van-cell>
        <van-cell title="是否禁用">
          <template #default>
            {{ computedDisableText(qlConfig.disabled) }}
          </template>
        </van-cell>
      </van-cell-group>
      <template #right>
        <van-button
            square
            text="编辑"
            type="info"
            class="slide-button"
            @click="showSaveQLConfig(qlConfig)"
        />
        <van-button
            square
            text="删除"
            type="danger"
            class="slide-button"
            @click="delQLConfig(qlConfig.displayName)"
        />
      </template>
    </van-swipe-cell>

    <van-action-sheet v-model="saveField.show" :title="saveField.title">
      <van-form>
        <van-field
            v-model="saveField.form.displayName"
            :disabled="saveField.func=='update'"
            name="displayName"
            label="显示名称"
            placeholder="显示名称"
        />
        <van-field
            v-model="saveField.form.url"
            name="url"
            label="地址"
            placeholder="http://127.0.0.1:5701/"
        />
        <van-field
            v-model="saveField.form.clientId"
            name="clientId"
            label="客户端ID"
            placeholder="客户端ID"
        />
        <van-field
            v-model="saveField.form.clientSecret"
            name="clientSecret"
            label="客户端密钥"
            placeholder="客户端密钥"
        />
        <van-field
            v-model="saveField.form.max"
            name="max"
            type="number"
            label="最大数量"
            placeholder="最大数量"
        />
        <van-field
            readonly
            clickable
            label="是否禁用"
            :value="saveField.disabledPicker.text"
            placeholder="是否禁用"
            @click="saveField.disabledPicker.show = true"
        />
        <van-popup v-model="saveField.disabledPicker.show" round position="bottom">
          <van-picker
              show-toolbar
              value-key="text"
              :columns="saveField.disabledPicker.columns"
              @cancel="saveField.disabledPicker.show = false"
              @confirm="onConfirmDisabled"
          />
        </van-popup>
        <div style="margin: 16px;">
          <van-button v-if="saveField.func == 'add'" round block type="primary" @click="addQLConfig()">添加</van-button>
          <van-button v-if="saveField.func == 'update'" round block type="info" @click="updateQLConfig()">保存
          </van-button>
        </div>
      </van-form>
    </van-action-sheet>

    <other/>

  </div>
</template>

<script>
import {addQLConfig, delQLConfig, getQLConfigList, updateQLConfig} from "@/api/admin";
import Other from "@/views/admin/other.vue";
import Version from "@/views/version.vue";

export default {
  name: "QLManage",
  components: {Version, Other},
  data() {
    return {
      qlConfigList: [{displayName: "", clientId: "", clientSecret: "", url: "", max: 0, used: 0, disabled: 0}],
      qlOptPopover: false,
      saveField: {
        show: false,
        title: "",
        form: {
          displayName: "",
          clientId: "",
          clientSecret: "",
          max: 30,
          disabled: 0,
        },
        disabledPicker: {
          show: false,
          text: '启用',
          columns: [{text: '启用', value: 0,}, {text: '禁用', value: 1}]
        },
        func: 'add'
      }
    };
  },
  computed: {
    computedDisableText() {
      return function (value) {
        console.log(value)
        let options = {0: "启用", 1: "禁用"}
        console.log(options[value])
        return options[value]
      }
    }
  },
  mounted() {
    this.getQLConfigList();
  }
  ,
  methods: {
    getQLConfigList: function () {
      getQLConfigList().then(resp => {
        this.qlConfigList = resp.data;
      });
    }
    ,
    delQLConfig: function (displayName) {
      this.$dialog
          .confirm({
            title: "提示",
            message: "确定删除么?"
          })
          .then(() => {
            delQLConfig(displayName).then(resp => {
              this.qlConfigList = resp.data;
            });
          })
          .catch(() => {
            this.$dialog.close();
          });
    }
    ,
    showSaveQLConfig: function (params) {
      if (params) {
        this.saveField.title = `编辑  ${params.displayName}`
        this.saveField.form = params
        this.saveField.func = "update"
      } else {
        this.saveField.title = "新增QL配置"
        this.saveField.form = {
          displayName: "",
          clientId: "",
          clientSecret: "",
          max: 30,
          disabled: 0,
        }
        this.saveField.func = "add"
      }
      this.saveField.disabledPicker.text = this.computedDisableText(this.saveField.form.disabled)
      this.saveField.show = true;
      this.qlOptPopover = false;
    }
    ,
    addQLConfig: function () {
      addQLConfig(this.saveField.form).then(resp => {
        this.qlConfigList = resp.data;
        this.saveField.show = false;
      });
    }
    ,
    updateQLConfig: function () {
      updateQLConfig(this.saveField.form).then(resp => {
        this.qlConfigList = resp.data;
        this.saveField.show = false;
      });
    },
    onConfirmDisabled: function (value) {
      this.saveField.form.disabled = value.value
      this.saveField.disabledPicker.text = this.computedDisableText(this.saveField.form.disabled)
      this.saveField.disabledPicker.show = false
    }
  }
}
;
</script>

<style>
.slide-button {
  height: 100%;
}
</style>
