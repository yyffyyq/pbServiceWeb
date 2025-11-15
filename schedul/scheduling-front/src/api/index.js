import request from "@/utils/request";

//测试后端服务器是否正常
export const getTest = () => {
  return request({
    method: "get",
    url: "health",
  });
};


//登录
export const getLogin = (data) => {
  //return request.post("user/login", info)
  return request({
    method: "post",
    url: "user/login",
    data: data,
  });
};

// 获取用户信息
export const getLoginUserUsingGet = () => {
  return request({
    method: "get",
    url: "user/get/login",
  });
}

// 注册接口
export const register = (data) => {
  return request({
    method: "post",
    url: "user/register",
    data: data,
  });
};

//重置密码
export const resetAccount = (data) => {
  return request({
    method: "put",
    url: "user/reset",
    data: data
  });
};

//退出登录
export const userLoginOutUsingPost = () => {
  return request({
    method: "post",
    url: "user/logout",
  });
};

//上传图片
export const uploadImageApi = (formData) => {
  return request({
    method: "post",
    url: "file/image",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
    timeout: 60000,
  });
};





