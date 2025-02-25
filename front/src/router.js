import { createRouter, createWebHistory } from "vue-router";
import Index from "@/views/index/Index.vue";
import AdminLogin from "@/views/admin/Login.vue";
import AdminHome from "@/views/admin/Home.vue";
import Bean from "@/views/index/Bean.vue";


const router = createRouter({
    history: createWebHistory(),
    base: import.meta.env.VITE_BASE_URL,
    routes: [
        {
            path: "/:mobile",
            alias: "/",
            name: "Index",
            props: true,
            component: Index,
            meta: {
                title: "JDX"
            }
        },
        {
            path: "/bean/:mobile/:type",
            alias: ["/bean", "/bean/:mobile"],
            name: "Bean",
            props: true,
            component: Bean,
            meta: {
                title: "收益"
            }
        },
        {
            path: "/login",
            name: "adminLogin",
            component: AdminLogin,
            meta: {
                title: "登录"
            }
        },
        {
            path: "/admin",
            name: "adminIndex",
            component: AdminHome,
            meta: {
                title: "Dashboard"
            }
        }
    ]
});



export default router
