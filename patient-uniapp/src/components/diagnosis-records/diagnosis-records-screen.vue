<template>
  <view class="drs">
    <view class="drs-header">
      <text class="drs-title">{{ title }}</text>
      <text class="drs-sub">{{ subtitle }}</text>
    </view>

    <diagnosis-records-filter-bar
      v-model:searchKeyword="searchKeyword"
      v-model:timeIndex="timeIndex"
      v-model:projectIndex="projectIndex"
      :search-placeholder="searchPlaceholder"
      :time-options="timeOptions"
      :project-options="projectOptions"
      @filter="onFilterTap"
    />

    <scroll-view scroll-y class="drs-scroll" :show-scrollbar="false">
      <diagnosis-record-card
        v-for="item in filteredList"
        :key="item.id"
        :record="item"
        :party-prefix="partyPrefix"
        @detail="onDetail"
        @export="onExport"
      />
      <view v-if="!filteredList.length" class="drs-empty">
        <text class="drs-empty-t">暂无符合条件的记录</text>
      </view>
      <view class="drs-pad-bottom" />
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import DiagnosisRecordCard from './diagnosis-record-card.vue'
import DiagnosisRecordsFilterBar from './diagnosis-records-filter-bar.vue'
import { PROJECT_FILTER_OPTIONS, TIME_FILTER_OPTIONS } from '../../utils/diagnosisRecordMock.js'

const props = defineProps({
  records: { type: Array, default: () => [] },
  title: { type: String, default: '历史诊断记录' },
  subtitle: { type: String, default: '查看和管理所有历史诊断记录' },
  /** 卡片主标题前缀：患者端可看「主治医师」；医生端用「患者」 */
  partyPrefix: { type: String, default: '患者' },
  searchPlaceholder: { type: String, default: '搜索患者姓名或诊断类型' },
  timeOptions: { type: Array, default: () => [...TIME_FILTER_OPTIONS] },
  projectOptions: { type: Array, default: () => [...PROJECT_FILTER_OPTIONS] }
})

const searchKeyword = ref('')
const timeIndex = ref(0)
const projectIndex = ref(0)

function norm(s) {
  return String(s || '')
    .trim()
    .toLowerCase()
}

function matchTime(row, idx) {
  if (idx <= 0) return true
  const t = row.diagnosisTime
  if (!t) return true
  const d = new Date(t.replace(/-/g, '/'))
  if (Number.isNaN(d.getTime())) return true
  const now = new Date()
  if (idx === 1) {
    const ago = new Date(now)
    ago.setDate(ago.getDate() - 7)
    return d >= ago
  }
  if (idx === 2) {
    const ago = new Date(now)
    ago.setDate(ago.getDate() - 30)
    return d >= ago
  }
  const label = props.timeOptions[idx] || ''
  const m = label.match(/(\d{4})\s*年/)
  if (m) {
    return String(d.getFullYear()) === m[1]
  }
  return true
}

function matchProject(row, idx) {
  if (idx <= 0) return true
  const want = props.projectOptions[idx]
  return row.projectType === want
}

const filteredList = computed(() => {
  const kw = norm(searchKeyword.value)
  return (props.records || []).filter((row) => {
    if (kw) {
      const hay = norm(
        `${row.partyName} ${row.projectType} ${row.diagnosisProject} ${row.conclusion} ${row.id}`
      )
      if (!hay.includes(kw)) return false
    }
    if (!matchTime(row, timeIndex.value)) return false
    if (!matchProject(row, projectIndex.value)) return false
    return true
  })
})

function onFilterTap() {
  uni.showToast({ title: `共 ${filteredList.value.length} 条`, icon: 'none' })
}

function onDetail(row) {
  uni.showModal({
    title: '诊断详情',
    content: `${row.diagnosisProject} · ${row.diagnosisTime}\n${row.conclusion || ''}`.slice(0, 500),
    showCancel: false
  })
}

function onExport(row) {
  uni.showToast({ title: `导出「${row.id}」开发中`, icon: 'none' })
}
</script>

<style lang="scss" scoped>
@import '../../uni.scss';

.drs {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: $bg-page;
  padding: 24rpx 24rpx 0;
  box-sizing: border-box;
}

.drs-header {
  margin-bottom: 20rpx;
  padding: 8rpx 4rpx 4rpx;
}

.drs-title {
  display: block;
  font-size: 40rpx;
  font-weight: 900;
  color: $color-diagnosis-teal;
  letter-spacing: 1rpx;
}

.drs-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: $text-secondary;
  line-height: 1.45;
}

.drs-scroll {
  flex: 1;
  height: 0;
  padding-bottom: 24rpx;
}

.drs-empty {
  padding: 80rpx 24rpx;
  text-align: center;
}

.drs-empty-t {
  font-size: 28rpx;
  color: $text-secondary;
}

.drs-pad-bottom {
  height: 48rpx;
}
</style>
