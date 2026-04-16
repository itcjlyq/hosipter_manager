<template>
  <view class="drc">
    <view class="drc-head">
      <view class="drc-head-l">
        <text class="drc-party">{{ partyPrefix }}：{{ record.partyName }}</text>
        <text class="drc-id">ID: {{ record.id }}</text>
      </view>
      <view class="drc-badge" :class="statusCls">{{ statusText }}</view>
    </view>

    <view class="drc-grid">
      <view class="drc-cell">
        <text class="drc-k">诊断时间</text>
        <text class="drc-v">{{ record.diagnosisTime }}</text>
      </view>
      <view class="drc-cell">
        <text class="drc-k">项目类型</text>
        <text class="drc-v">{{ record.projectType }}</text>
      </view>
      <view class="drc-cell">
        <text class="drc-k">诊断项目</text>
        <text class="drc-v">{{ record.diagnosisProject }}</text>
      </view>
      <view class="drc-cell">
        <text class="drc-k">手术费用</text>
        <text class="drc-v cost">¥{{ costFmt }}</text>
      </view>
    </view>

    <view class="drc-concl-block">
      <text class="drc-concl-label">诊断结论：</text>
      <view class="drc-concl-box">
        <text class="drc-concl-t">{{ record.conclusion }}</text>
      </view>
    </view>

    <view class="drc-foot">
      <view class="drc-btn outline" hover-class="drc-btn-h" @click="emit('detail', record)">
        <text class="drc-btn-ic">👁</text>
        <text>查看详情</text>
      </view>
      <view class="drc-btn solid" hover-class="drc-btn-h" @click="emit('export', record)">
        <text class="drc-btn-ic">⬇</text>
        <text>导出记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  record: {
    type: Object,
    required: true
  },
  /** 患者端可传「主治医师」；医生端多为「患者」 */
  partyPrefix: { type: String, default: '患者' }
})

const emit = defineEmits(['detail', 'export'])

const costFmt = computed(() => {
  const n = Number(props.record.surgeryCost)
  if (Number.isNaN(n)) return props.record.surgeryCost
  return n.toLocaleString('zh-CN')
})

const statusMap = {
  completed: { text: '已完成', cls: 'st-ok' },
  pending: { text: '待处理', cls: 'st-wait' },
  processing: { text: '进行中', cls: 'st-doing' }
}

const statusText = computed(() => statusMap[props.record.status]?.text || '已完成')
const statusCls = computed(() => statusMap[props.record.status]?.cls || 'st-ok')
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.drc {
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: 28rpx 26rpx 24rpx;
  margin-bottom: 24rpx;
}

.drc-head {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 22rpx;
}

.drc-head-l {
  flex: 1;
  min-width: 0;
}

.drc-party {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: $color-diagnosis-teal;
}

.drc-id {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $text-secondary;
}

.drc-badge {
  flex-shrink: 0;
  margin-left: 16rpx;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 700;
}

.st-ok {
  background: $diagnosis-status-ok-bg;
  color: $diagnosis-status-ok-text;
}

.st-wait {
  background: $diagnosis-status-wait-bg;
  color: $diagnosis-status-wait-text;
}

.st-doing {
  background: #e0f2fe;
  color: #0369a1;
}

.drc-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx 20rpx;
  margin-bottom: 20rpx;
}

.drc-cell {
  min-width: 0;
}

.drc-k {
  display: block;
  font-size: 22rpx;
  color: $text-secondary;
  margin-bottom: 6rpx;
}

.drc-v {
  display: block;
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 600;
}

.drc-v.cost {
  color: $color-diagnosis-teal;
}

.drc-concl-block {
  margin-bottom: 24rpx;
}

.drc-concl-label {
  font-size: 26rpx;
  font-weight: 700;
  color: $text-primary;
}

.drc-concl-box {
  margin-top: 12rpx;
  padding: 20rpx 22rpx;
  background: $diagnosis-conclusion-bg;
  border-radius: 16rpx;
}

.drc-concl-t {
  font-size: 26rpx;
  color: $text-secondary;
  line-height: 1.55;
}

.drc-foot {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
}

.drc-btn {
  flex: 1;
  height: 76rpx;
  border-radius: 999rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: 26rpx;
  font-weight: 700;
}

.drc-btn.outline {
  border: 2rpx solid $color-diagnosis-teal;
  color: $color-diagnosis-teal;
  background: #ffffff;
}

.drc-btn.solid {
  background: $color-diagnosis-teal;
  color: #ffffff;
}

.drc-btn-h {
  opacity: 0.88;
}

.drc-btn-ic {
  font-size: 28rpx;
}
</style>
