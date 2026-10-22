import axiosInstance from "../api/axiosConfig";

export const createTask = async (taskData) => {
  const response = await axiosInstance.post("/api/tasks", taskData);
  return response.data;
};
export const getTasks = async ({
  page = 0,
  size = 10,
  sortBy = "createdAt",
  direction = "desc",
  status,
  keyword,
} = {}) => {
  const params = {
    page,
    size,
    sortBy,
    direction,
  };
  if (status) {
    params.status = status;
  }
  if (keyword) {
    params.keyword = keyword;
  }
  const response = await axiosInstance.get("/api/tasks", {
    params,
  });
  return response.data;
};
export const getTaskById = async (taskId) => {
  const response = await axiosInstance.get(`/api/tasks/${taskId}`);
  return response.data;
};
export const updateTask = async (taskId, taskData) => {
  const response = await axiosInstance.put(`/api/tasks/${taskId}`, taskData);
  return response.data;
};
export const deleteTask = async (taskId) => {
  const response = await axiosInstance.delete(`/api/tasks/${taskId}`);
  return response.data;
};
export const updateTaskStatus = async (taskId, status) => {
    const response = await axiosInstance.patch(
        `/api/tasks/${taskId}/status`,
        { status }
    );

    return response.data;
};
export const assignTask = async (taskId, userId) => {
  const response = await axiosInstance.post(`/api/tasks/${taskId}/assign`, {
    userId,
  });
  return response.data;
};
