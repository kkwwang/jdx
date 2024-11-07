<template>
    <van-cell-group :title="form.displayName || '-'" inset v-if="form">
        <template #title>
            <van-cell center :title="form.displayName || '-'">
                <div style="text-align: right">
                    <van-button
                        v-if="qlConfig.displayName"
                        size="small"
                        type="primary"
                        plain
                        hairline
                        @click="form.edit = !form.edit"
                    >{{ form.edit ? '取消' : '编辑' }}
                    </van-button>
                    <van-button
                        size="small"
                        type="danger"
                        plain
                        hairline
                        @click="delQLConfig(form.displayName)"
                        style="margin-left: 5px;"
                    >删除
                    </van-button>
                </div>
            </van-cell>
        </template>
        <van-field
            v-if="add"
            label="名称"
            v-model="form.displayName"
            :disabled="!form.edit"
            name="displayName"
            placeholder="名称"
        />
        <van-field
            label="地址"
            v-model="form.url"
            type="textarea"
            autosize
            rows="1"
            :disabled="!form.edit"

            name="url"
            placeholder="http://127.0.0.1:5701/"
        />
        <van-field
            label="客户端ID"
            v-model="form.clientId"
            type="textarea"
            autosize
            rows="1"
            :disabled="!form.edit"
            name="clientId"
            placeholder="客户端ID"
        />
        <van-field
            label="客户端密钥"
            v-model="form.clientSecret"
            type="textarea"
            autosize
            rows="1"
            :disabled="!form.edit"
            name="clientSecret"
            placeholder="客户端密钥"
        />
        <van-field
            label="最大数量"
            v-model="form.max"
            :disabled="!form.edit"
            name="max"
            type="number"
            placeholder="最大数量"
        />
        <van-field name="disabled" label="是否禁用" :disabled="!form.edit">
            <template #input>
                <van-switch
                    :active-value="1"
                    :inactive-value="0"
                    :disabled="!form.edit"
                    v-model="form.disabled" />
            </template>
        </van-field>

        <van-cell title="操作" v-if="form.edit">
            <div style="text-align: right">
                <van-button size="small" plain hairline style="margin-left: 5px;" type="info" @click="save()">保存</van-button>
            </div>
        </van-cell>
    </van-cell-group>
    <van-divider />
</template>
<script setup name="ql-item">
import { showConfirmDialog, showToast } from "vant";
import { delQLConfig as delQLConfigApi, saveQLConfig as saveQLConfigApi } from "@/api/admin/ql";
import { ref, watch } from "vue";

const _props = defineProps({
    qlConfig: {
        type: Object,
        required: true
    },
    add: {
        type: Boolean,
        default: false
    },
})

const form = ref(null)

watch(() => _props.qlConfig, () => {
    form.value = {
        ..._props.qlConfig
    }
}, {
    deep: true,
    immediate: true
})

const _emit = defineEmits(['changed'])

const delQLConfig = (displayName) => {
    showConfirmDialog({
        title: '提示',
        message: '确定删除么?'
    }).then(() => {
        if (!_props.add) {
            delQLConfigApi(displayName).then(res => {
                showToast({
                    message: '删除成功',
                    duration: 500
                })
                _emit('changed', res.data)
            })
        } else {
            showToast({
                message: '删除成功',
                duration: 500
            })
            _emit('changed')
        }

    })
}

const save = () => {
    saveQLConfigApi(form.value).then(res => {
        showToast({
            message: '保存成功',
            duration: 500
        })
        _emit('changed', res.data)
    })
}
</script>