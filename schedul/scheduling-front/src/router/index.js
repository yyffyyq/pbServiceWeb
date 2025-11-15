import Test from "@/views/test.vue";
import { getToken } from "@/utils/token";

import { createRouter, createWebHistory } from "vue-router";
import UserRegisterPage from "@/views/user/UserRegisterPage.vue";
import UserLoginPage from "@/views/user/UserLoginPage.vue";
import HomePage from "@/views/HomePage.vue";
import UserManagePage from "@/views/admin/UserManagePage.vue";



const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: "/home",
    },
    {
      name: "home",
      path: "/home",
      component: HomePage,
    },
    {
      name: "test",
      path: "/test",
      component: Test,
    },
    {
      name: "register",
      path: "/user/register",
      component: UserRegisterPage,
    },
    {
      name: "login",
      path: "/user/login",
      component: UserLoginPage,
    },
    {
      name: "userManage",
      path: "/admin/userManage",
      component: UserManagePage,
    },

    // my_information
    
  ],
});

// 需要登录才能访问的路由
const authRequiredRoutes = ['/home', '/', '/admin/userManage']

// 全局前置路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken();
  const userInfo = localStorage.getItem('userInfo');
  const isLoggedIn = token || userInfo;

  // 判断目标路由是否需要登录
  const requiresAuth = authRequiredRoutes.includes(to.path);

  if (isLoggedIn) {
    // 已登录
    if (to.path === '/user/login' || to.path === '/user/register') {
      // 已登录用户访问登录/注册页，重定向到首页
      next({ path: '/home' });
    } else {
      next();
    }
  } else {
    // 未登录
    if (requiresAuth) {
      // 需要登录但未登录，重定向到登录页
      next({ path: '/user/login', query: { redirect: to.fullPath } });
    } else {
      // 不需要登录的页面，直接访问
      next();
    }
  }
});

export default router;