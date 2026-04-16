import request from './request'

export function listPatients() {
  return request.get('/api/admin/patients')
}

export function getPatient(userId) {
  return request.get(`/api/admin/patients/${userId}`)
}

export function createPatient(data) {
  return request.post('/api/admin/patients', data)
}

export function updatePatient(userId, data) {
  return request.put(`/api/admin/patients/${userId}`, data)
}

export function deletePatient(userId) {
  return request.delete(`/api/admin/patients/${userId}`)
}

export function resetPatientPassword(userId, newPassword) {
  return request.post(`/api/admin/patients/${userId}/reset-password`, { newPassword })
}
