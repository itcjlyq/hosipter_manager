import request from './request'

export function listDoctors() {
  return request.get('/api/admin/doctors')
}

export function getDoctor(userId) {
  return request.get(`/api/admin/doctors/${userId}`)
}

export function createDoctor(data) {
  return request.post('/api/admin/doctors', data)
}

export function updateDoctor(userId, data) {
  return request.put(`/api/admin/doctors/${userId}`, data)
}

export function deleteDoctor(userId) {
  return request.delete(`/api/admin/doctors/${userId}`)
}

export function resetDoctorPassword(userId, newPassword) {
  return request.post(`/api/admin/doctors/${userId}/reset-password`, { newPassword })
}
