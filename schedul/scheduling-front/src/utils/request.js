import axios from "axios";
import { showToast, showLoadingToast, closeToast } from "vant";

// 创建axios实例（baseURL 保持你之前配置的 /api/，配合代理）
const request = axios.create({
  baseURL: "/api/",
  timeout: 20000,
  withCredentials: true, // 关键：允许跨域请求携带 Cookie（Session 依赖 Cookie 传递）
});

// 请求拦截器（无 Token 逻辑）
request.interceptors.request.use(
  (config) => {
    // 显示加载提示（排除上传请求）
    if (!config.hideLoading && config.method !== "get") {
      showLoadingToast({
        message: "请求中...",
        forbidClick: true,
        duration: 0,
      });
    }

    return config;
  },
  (error) => {
    closeToast();
    showToast("请求配置错误");
    return Promise.reject(error);
  }
);

// 响应拦截器（无 Token 逻辑）
request.interceptors.response.use(
  (response) => {
    closeToast();

    const { data } = response;

    if (data.code !== undefined) {
      if (data.code === 200 || data.code === 0) {
        return data;
      } else if (data.code === 401) {
        showToast("登录已过期，请重新登录");
        // 可添加跳转登录页面逻辑，例：window.location.href = "/login"
        return Promise.reject(new Error(data.message || "未授权"));
      } else {
        return data;
      }
    }

    return data;
  },
  (error) => {
    closeToast();

    let message = "网络错误";

    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 400:
          message = data.message || "请求参数错误";
          break;
        case 401:
          message = "未授权，请登录";
          break;
        case 403:
          message = "权限不足";
          break;
        case 404:
          message = "请求的资源不存在";
          break;
        case 413:
          message = "上传文件过大";
          break;
        case 422:
          message = data.message || "数据验证失败";
          break;
        case 500:
          message = "服务器内部错误";
          break;
        case 502:
          message = "网关错误";
          break;
        case 503:
          message = "服务不可用";
          break;
        default:
          message = data.message || `请求失败 (${status})`;
      }
    } else if (error.request) {
      if (error.code === "ECONNABORTED") {
        message = "请求超时，请重试";
      } else {
        message = "网络连接失败";
      }
    }

    showToast(message);
    return Promise.reject(error);
  }
);

// 文件上传专用请求方法（保留不变）
export function uploadRequest(url, formData, onProgress) {
  return request({
    url,
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
    timeout: 60000,
    hideLoading: true,
    onUploadProgress: (progressEvent) => {
      if (onProgress) {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        );
        onProgress(percentCompleted);
      }
    },
  });
}

// 下载文件（保留不变）
export function downloadFile(url, filename) {
  return request({
    url,
    method: "get",
    responseType: "blob",
    hideLoading: true,
  }).then((response) => {
    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  });
}

export default request;