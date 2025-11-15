import { ref, computed } from "vue";
import { defineStore } from "pinia";
import { getToken, setToken, removeToken } from "@/utils/token";
import { getLogin } from "@/api";

export const useUserStore = defineStore(
  "user",
  {
    persist: true,
    state: () => ({
      token: getToken(),
      userAccount:"",
      id:"",
      phone:"",
      userName:"",
    }),
    actions: {
      //登录函数
      async login(loginFrom) {
        const ret = await getLogin(loginFrom);
        const newToken = ret.token;
        setToken(newToken);
        this.token = newToken;
        this.userAccount=ret.data.userAccount;
        this.phone=ret.data.phone;
        this.id=ret.data.id
        this.userName=ret.data.userName
      },
      async getLoginUser() {
        // const ret = await getLoginUser();
        // this.nickName = ret.nickName;
        // this.uid = ret.uid;
        // setToken(ret.token);
      },
      //退出函数
      async clearUserInfo() {
        removeToken();
        this.token = "";
        this.userAccount="";
        this.phone="";
        this.userName="";
        this.id=""
      },
    },
  }
);