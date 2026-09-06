/**
 * 金额工具 - 前端"元" <-> 后端"分"
 */

/** 分转元 */
export function fenToYuan(fen) {
  if (fen == null) return '0.00'
  return (fen / 100).toFixed(2)
}

/** 元转分 */
export function yuanToFen(yuan) {
  if (yuan == null) return 0
  return Math.round(parseFloat(yuan) * 100)
}

/** 格式化金额显示(带¥符号) */
export function formatMoney(fen) {
  return '¥' + fenToYuan(fen)
}