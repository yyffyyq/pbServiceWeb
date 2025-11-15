import Test from "@/views/test.vue";
import { getToken } from "@/utils/token";

import { createRouter, createWebHistory } from "vue-router";
import UserRegisterPage from "@/views/user/UserRegisterPage.vue";
import UserLoginPage from "@/views/user/UserLoginPage.vue";



const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: "/test",
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

    // my_information
    
  ],
});

//全局前置路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken();
  if (token) {
    if (to.path == "/user/login" || to.path == "/user/register") {
      // next({ path: "/" });
      next();
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;