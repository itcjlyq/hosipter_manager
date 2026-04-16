import { requestJson } from './request.js'

/** 当前患者资料 */
export async function fetchPatientMe() {
  const body = await requestJson({ url: '/api/patient/me', method: 'GET' })
  return body.data
}

/** 可绑定医生目录 */
export async function listPatientDoctors() {
  const body = await requestJson({ url: '/api/patient/doctors', method: 'GET' })
  return body.data || []
}

/** 我的医患绑定记录 */
export async function listPatientBinds() {
  const body = await requestJson({ url: '/api/patient/binds', method: 'GET' })
  return body.data || []
}

/** 向指定医生发起绑定申请 */
export async function applyPatientBind(doctorUserId) {
  const body = await requestJson({
    url: '/api/patient/binds',
    method: 'POST',
    data: { doctorUserId }
  })
  return body.data
}

/** 取消待审核申请 */
export function cancelPatientBind(bindId) {
  return requestJson({ url: `/api/patient/binds/${bindId}`, method: 'DELETE' })
}
