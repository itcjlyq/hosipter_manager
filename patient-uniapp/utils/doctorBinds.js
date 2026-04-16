import { requestJson } from './request.js'

export async function listDoctorBinds(status) {
  const q = status ? `?status=${encodeURIComponent(status)}` : ''
  const body = await requestJson({ url: `/api/doctor/binds${q}`, method: 'GET' })
  return body.data || []
}

export async function countPendingDoctorBinds() {
  const list = await listDoctorBinds('pending')
  return list.length
}

export function approveDoctorBind(bindId) {
  return requestJson({
    url: `/api/doctor/binds/${bindId}/approve`,
    method: 'POST',
    data: {}
  })
}

export function rejectDoctorBind(bindId) {
  return requestJson({
    url: `/api/doctor/binds/${bindId}/reject`,
    method: 'POST',
    data: {}
  })
}
