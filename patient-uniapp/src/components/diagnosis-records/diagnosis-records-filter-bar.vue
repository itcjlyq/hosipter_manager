<template>
  <view class="drf">
    <view class="drf-search">
      <text class="drf-search-ic">🔍</text>
      <input
        class="drf-input"
        type="text"
        :value="searchKeyword"
        :placeholder="searchPlaceholder"
        placeholder-class="drf-ph"
        confirm-type="search"
        @input="onInput"
        @confirm="emit('filter')"
      />
    </view>
    <view class="drf-row">
      <picker class="drf-pick" mode="selector" :range="timeOptions" :value="timeIndex" @change="onTime">
        <view class="drf-pick-face">
          <text class="drf-pick-t">{{ timeOptions[timeIndex] }}</text>
          <text class="drf-arrow">▼</text>
        </view>
      </picker>
      <picker class="drf-pick" mode="selector" :range="projectOptions" :value="projectIndex" @change="onProject">
        <view class="drf-pick-face">
          <text class="drf-pick-t">{{ projectOptions[projectIndex] }}</text>
          <text class="drf-arrow">▼</text>
        </view>
      </picker>
    </view>
    <view class="drf-filter-btn" hover-class="drf-filter-h" @click="emit('filter')">
      <text class="drf-f-ic">▽</text>
      <text>筛选</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  searchKeyword: { type: String, default: '' },
  searchPlaceholder: { type: String, default: '搜索患者姓名或诊断类型' },
  timeOptions: { type: Array, default: () => ['全部时间'] },
  projectOptions: { type: Array, default: () => ['全部项目'] },
  timeIndex: { type: Number, default: 0 },
  projectIndex: { type: Number, default: 0 }
})

const emit = defineEmits(['update:searchKeyword', 'update:timeIndex', 'update:projectIndex', 'filter'])

function onInput(e) {
  emit('update:searchKeyword', e.detail.value || '')
}

function onTime(e) {
  const i = Number(e.detail.value)
  emit('update:timeIndex', Number.isNaN(i) ? 0 : i)
}

function onProject(e) {
  const i = Number(e.detail.value)
  emit('update:projectIndex', Number.isNaN(i) ? 0 : i)
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.drf {
  background: $card-white;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: 24rpx 22rpx 22rpx;
  margin-bottom: 24rpx;
}

.drf-search {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 0 20rpx;
  height: 80rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  border: 2rpx solid #e2e8f0;
  margin-bottom: 20rpx;
}

.drf-search-ic {
  font-size: 28rpx;
  margin-right: 12rpx;
  opacity: 0.65;
}

.drf-input {
  flex: 1;
  height: 80rpx;
  font-size: 28rpx;
  color: $text-primary;
}

.drf-ph {
  color: #94a3b8;
}

.drf-row {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.drf-pick {
  flex: 1;
  min-width: 0;
}

.drf-pick-face {
  height: 72rpx;
  padding: 0 20rpx;
  background: #f8fafc;
  border-radius: 14rpx;
  border: 2rpx solid #e2e8f0;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.drf-pick-t {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.drf-arrow {
  font-size: 18rpx;
  color: #94a3b8;
  margin-left: 8rpx;
}

.drf-filter-btn {
  height: 80rpx;
  border-radius: 16rpx;
  background: $color-diagnosis-teal;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 30rpx;
  font-weight: 800;
  color: #ffffff;
}

.drf-filter-h {
  opacity: 0.9;
}

.drf-f-ic {
  font-size: 28rpx;
  opacity: 0.95;
}
</style>
