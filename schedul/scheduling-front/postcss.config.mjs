export default {
  plugins: {
    'postcss-px-to-viewport': {
      viewportWidth: 1366,      // 按设计稿宽度（Vant 默认基于 375px 设计） pc 1366 1920
      unitPrecision: 6,        // 转换后保留的小数位数
      minPixelValue: 1,        // 小于 1px 不转换
      propList: ['*'],         // 对所有 CSS 属性生效
      exclude: /node_modules\/vant/, // 排除 Vant 组件库（已适配无需重复转换）
    }
  }
}