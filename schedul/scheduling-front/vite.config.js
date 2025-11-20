import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import { VantResolver } from '@vant/auto-import-resolver';
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";


export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    AutoImport({
      resolvers: [ElementPlusResolver(),VantResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver(),VantResolver()],
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },

  server: {
    plugins: [vue()],
    proxy: {
      "/api": {
        // target: "http://223.4.249.106:8101/api/",
        target: "http://223.4.249.106:8101/api/",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
    },
  },
});
