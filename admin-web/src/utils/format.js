export function formatDateTime(v) {
  if (v == null) return '—'
  if (typeof v === 'string') return v.replace('T', ' ')
  return String(v)
}
