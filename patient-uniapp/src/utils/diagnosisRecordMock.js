/** 演示用诊断记录；后续可替换为接口数据 */
export const TIME_FILTER_OPTIONS = ['全部时间', '近 7 天', '近 30 天', '2023 年', '2024 年']

export const PROJECT_FILTER_OPTIONS = ['全部项目', '面部整形', '眼部整形', '皮肤管理']

export function getDemoDiagnosisRecords() {
  return [
    {
      id: 'P20230512001',
      partyName: '张三',
      diagnosisTime: '2023-05-12',
      projectType: '面部整形',
      diagnosisProject: '隆鼻手术',
      surgeryCost: 12800,
      conclusion:
        '患者术后恢复良好，鼻部形态稳定，未见明显并发症。建议继续按医嘱护理，定期复诊观察。',
      status: 'completed'
    },
    {
      id: 'P20230820002',
      partyName: '李四',
      diagnosisTime: '2023-08-20',
      projectType: '眼部整形',
      diagnosisProject: '双眼皮修复',
      surgeryCost: 9800,
      conclusion: '切口愈合正常，肿胀消退中。注意休息、避免揉眼，两周后复查。',
      status: 'pending'
    },
    {
      id: 'P20240410003',
      partyName: '王五',
      diagnosisTime: '2024-04-10',
      projectType: '皮肤管理',
      diagnosisProject: '激光嫩肤',
      surgeryCost: 3600,
      conclusion: '治疗区域轻微泛红属正常反应，注意防晒与保湿。',
      status: 'processing'
    }
  ]
}
