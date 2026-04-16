import request from './request'

export function listBinds(status) {
  const params = {}
  if (status != null && status !== '') {
    params.status = status
  }
  return request.get('/api/admin/binds', { params })
}
