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

// ========== 用户管理相关接口 ==========

/**
 * 添加用户（管理员）
 */
export const addUser = (data) => {
  return request({
    method: "post",
    url: "user/add",
    data: data,
  });
};

/**
 * 根据 id 获取用户（管理员）
 */
export const getUserById = (id) => {
  return request({
    method: "get",
    url: "user/get",
    params: { id },
  });
};

/**
 * 根据 id 获取用户 VO
 */
export const getUserVOById = (id) => {
  return request({
    method: "get",
    url: "user/get/vo",
    params: { id },
  });
};

/**
 * 删除用户（管理员）
 */
export const deleteUser = (data) => {
  return request({
    method: "post",
    url: "user/delete",
    data: data,
  });
};

/**
 * 更新用户（管理员）
 */
export const updateUser = (data) => {
  return request({
    method: "post",
    url: "user/update",
    data: data,
  });
};

/**
 * 获取用户列表分页（管理员）
 */
export const listUserVOByPage = (data) => {
  return request({
    method: "post",
    url: "user/list/page/vo",
    data: data,
  });
};

// ========== 值班管理相关接口 ==========

/**
 * 获取值班配置（包括基准日期和所有值班人员列表）
 */
export const getDutyConfig = () => {
  return request({
    method: "get",
    url: "duty/config",
  });
};

/**
 * 保存/更新基准日期（仅管理员）
 */
export const saveBaseDate = (data) => {
  return request({
    method: "post",
    url: "duty/config/base-date",
    data: data,
  });
};

/**
 * 添加值班人员（仅管理员）
 */
export const addDutyPerson = (data) => {
  return request({
    method: "post",
    url: "duty/person/add",
    data: data,
  });
};

/**
 * 删除值班人员（仅管理员）
 */
export const deleteDutyPerson = (data) => {
  return request({
    method: "post",
    url: "duty/person/delete",
    data: data,
  });
};

/**
 * 根据值班类型查询值班人员列表（仅管理员）
 */
export const getDutyPersonListByType = (data) => {
  return request({
    method: "post",
    url: "duty/person/list/type",
    data: data,
  });
};

/**
 * 根据日期查询当天的值班人员列表（用于日历显示）
 */
export const getDutyPersonListByDate = (data) => {
  return request({
    method: "post",
    url: "duty/person/list/date",
    data: data,
  });
};

/**
 * 统计用户的值班天数（用于用户管理页面显示）
 */
export const getDutyCount = (data) => {
  return request({
    method: "post",
    url: "duty/count",
    data: data,
  });
};





