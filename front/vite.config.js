import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import {resolve} from "path"
import VueSetupExtend from "vite-plugin-vue-setup-extend";
import { createHtmlPlugin } from 'vite-plugin-html'

export default async ({mode}) => {

    const devConfig = loadEnv(mode, "./");

    return defineConfig({
        base: "/",
        resolve: {
            alias: {
                "@": resolve(__dirname, "./src"),
            }
        },
        define: {
            'npm_package_version': JSON.stringify(process.env.npm_package_version),
            '_TRACKING_ID': JSON.stringify(devConfig.VITE_TRACKING_ID),
        },
        plugins: [
            vue(),
            VueSetupExtend(),
            createHtmlPlugin({
                inject:{
                    data:{
                        _TRACKING_ID: devConfig.VITE_TRACKING_ID,
                    }
                }
            })
        ],
        build: {
            rollupOptions: {
                output: {
                    ...process.argv.includes("--host") ? {} : {
                        chunkFileNames: "assets/js/chunk-[hash].js",
                        entryFileNames: "assets/js/chunk-[hash].js",
                        assetFileNames: "assets/[ext]/chunk-[hash].[ext]"
                    },
                }
            },
            terserOptions: {
                compress: {
                    //生产环境时移除console.log()
                    drop_console: !process.argv.includes("--debug"),
                    drop_debugger: !process.argv.includes("--debug"),
                },
            },
        },

        server: {
            port: 43000,
            allowedHosts: true,
            proxy: {
                '^/api/*': {
                    target: `http://127.0.0.1:8090`,
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, ''),
                },
            }
        },
    })
}
