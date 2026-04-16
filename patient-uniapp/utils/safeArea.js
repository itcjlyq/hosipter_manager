/**
 * 自定义导航页顶部占位高度（px）：状态栏 + 刘海安全区取较大值。
 * 优先 uni.getWindowInfo，其次 getSystemInfoSync。
 */
export function getStatusBarSafeTopPx() {
  let info = null
  try {
    if (typeof uni.getWindowInfo === 'function') {
      info = uni.getWindowInfo()
    }
  } catch (_) {}
  if (!info) {
    try {
      info = uni.getSystemInfoSync()
    } catch (_) {
      return 20
    }
  }
  return pickSafeTopPx(info)
}

function pickSafeTopPx(info) {
  const sb = toNum(info.statusBarHeight)
  const insetTop = info.safeAreaInsets ? toNum(info.safeAreaInsets.top) : 0
  const safeTop = info.safeArea ? toNum(info.safeArea.top) : 0
  const list = [sb, insetTop, safeTop].filter((n) => n > 0)
  if (!list.length) return 20
  return Math.max(...list)
}

function toNum(v) {
  const n = Number(v)
  return Number.isFinite(n) && n >= 0 ? n : 0
}
