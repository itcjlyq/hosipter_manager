import { API_BASE } from './config.js'
import { clearAuth, getToken } from './auth.js'
import { requestJson } from './request.js'

function joinUrl(path) {
  const p = path.startsWith('/') ? path : `/${path}`
  return `${API_BASE}${p}`
}

/** @returns {Promise<Array>} FileAssetVO[] */
export async function listPatientFiles() {
  const body = await requestJson({ url: '/api/patient/files', method: 'GET' })
  return Array.isArray(body.data) ? body.data : []
}

export function deletePatientFile(id) {
  return requestJson({ url: `/api/patient/files/${id}`, method: 'DELETE' })
}

/**
 * 下载患者档案到本地临时路径（需配置合法域名与 Authorization）
 * @param {number|string} id
 * @returns {Promise<string>} tempFilePath
 */
export function downloadPatientFile(id) {
  const token = getToken()
  const url = joinUrl(`/api/patient/files/${id}/download`)
  return new Promise((resolve, reject) => {
    let mpNeedAbsolute = false
    // #ifdef MP-WEIXIN
    mpNeedAbsolute = true
    // #endif
    if (mpNeedAbsolute && url && !url.startsWith('http') && !url.startsWith('//')) {
      reject(new Error('请配置 utils/config.js：微信小程序需填写本机局域网 IP'))
      return
    }
    uni.downloadFile({
      url,
      header: token ? { Authorization: `Bearer ${token}` } : {},
      timeout: 120000,
      success: (res) => {
        const sc = res.statusCode
        if (sc === 401 || sc === 403) {
          clearAuth()
          reject(new Error('登录已失效，请重新登录'))
          return
        }
        if (sc !== 200 || !res.tempFilePath) {
          reject(new Error(`下载失败（HTTP ${sc || '—'}）`))
          return
        }
        resolve(res.tempFilePath)
      },
      fail: (err) => {
        const msg = err?.errMsg || err?.message || '下载失败'
        reject(new Error(msg.includes('timeout') ? '下载超时' : msg))
      }
    })
  })
}

function parseUploadBody(raw) {
  if (raw == null) return null
  if (typeof raw === 'object') return raw
  if (typeof raw === 'string') {
    try {
      return JSON.parse(raw)
    } catch {
      return null
    }
  }
  return null
}

/**
 * 患者上传文件：POST /api/patient/files  multipart field name: file
 * @param {string} filePath 本地临时路径
 * @param {{ name?: string, bizType?: string }} options
 */
export function uploadPatientFile(filePath, options = {}) {
  const token = getToken()
  const bizType = (options.bizType || 'attachment').trim().toLowerCase()
  const url = joinUrl('/api/patient/files')

  return new Promise((resolve, reject) => {
    let mpNeedAbsolute = false
    // #ifdef MP-WEIXIN
    mpNeedAbsolute = true
    // #endif
    if (mpNeedAbsolute && url && !url.startsWith('http') && !url.startsWith('//')) {
      reject(new Error('请配置 utils/config.js：微信小程序需填写本机局域网 IP（不能用 localhost）'))
      return
    }
    uni.uploadFile({
      url,
      filePath,
      name: 'file',
      formData: { bizType },
      header: token ? { Authorization: `Bearer ${token}` } : {},
      timeout: 120000,
      success: (res) => {
        const status = res.statusCode
        const body = parseUploadBody(res.data)
        if (status === 401 || status === 403) {
          clearAuth()
          reject(new Error(body?.message || '无权限'))
          return
        }
        if (status < 200 || status >= 300) {
          reject(new Error(body?.message || `上传失败（HTTP ${status}）`))
          return
        }
        if (body && typeof body.code === 'number' && body.code !== 0) {
          reject(new Error(body.message || '上传失败'))
          return
        }
        resolve(body?.data ?? body)
      },
      fail: (err) => {
        const msg = err?.errMsg || err?.message || '网络错误'
        reject(new Error(msg.includes('timeout') ? '上传超时，请检查网络与后端' : msg))
      }
    })
  })
}

/** 业务类型展示名 */
export function bizTypeLabel(bizType) {
  const m = {
    diagnosis_image: '图片',
    diagnosis_doc: '文档',
    diagnosis_note: '说明',
    attachment: '附件'
  }
  return m[bizType] || bizType || '附件'
}

export function formatFileSize(bytes) {
  const n = Number(bytes)
  if (!Number.isFinite(n) || n < 0) return '—'
  if (n < 1024) return `${n} B`
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
  return `${(n / (1024 * 1024)).toFixed(1)} MB`
}
