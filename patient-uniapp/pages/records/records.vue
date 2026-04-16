<template>
  <diagnosis-records-screen :records="list" party-prefix="患者" />
</template>

<script setup>
import { ref } from 'vue'
import DiagnosisRecordsScreen from '../../components/diagnosis-records/diagnosis-records-screen.vue'
import { getDemoDiagnosisRecords } from '../../utils/diagnosisRecordMock.js'

const list = ref(getDemoDiagnosisRecords())
</script>
