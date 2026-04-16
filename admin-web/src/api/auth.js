import request from './request'

export function loginPassword(data) {
  return request.post('/api/auth/login/password', data)
}
