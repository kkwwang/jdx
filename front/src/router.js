import Vue from "vue";
import Router from "vue-router";
import Index from "./views/index/Index";
import AdminLogin from "./views/admin/Login";
import AdminHome from "./views/admin/Home";
import Bean from "./views/index/Bean";

Vue.use(Router);

export default new Router({
  mode: "history",
  // mode: "hash",
  base: process.env.BASE_URL,
  routes: [
    {
      path: "/",
      name: "Index",
      component: Index,
      meta: {
        title: "JDX"
      }
    },
    {
      path: "/bean",
      name: "Bean",
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
