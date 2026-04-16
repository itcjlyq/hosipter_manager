<script>
export default {
  onLaunch() {
    // 应用启动；后续可在此做全局初始化
  },
  onShow() {},
  onHide() {}
}
</script>

<style lang="scss">
@import './uni.scss';

/* H5：根节点无高度时会出现整页空白（用 vh 避免父级未设 height 时 100% 失效） */
/* #ifdef H5 */
html,
body {
  margin: 0;
  min-height: 100%;
  height: auto;
}
#app {
  min-height: 100vh;
  height: auto;
}
uni-app {
  display: block;
  min-height: 100vh;
  box-sizing: border-box;
}
/* #endif */

page {
  background-color: $bg-page;
  color: $text-primary;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 自定义导航页顶部占位：与内联 height 取较大值，避免部分机型仅 statusBarHeight 偏小 */
.status-bar {
  flex-shrink: 0;
  width: 100%;
  min-height: constant(safe-area-inset-top);
  min-height: env(safe-area-inset-top);
}
</style>
