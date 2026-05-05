import axiosClient from './axiosClient.js';

export const getEmployees = async ({ page, size }) => {
  const response = await axiosClient.get('/employees', {
    params: { page, size },
  });
  return response.data;
};

export const searchEmployees = async (query) => {
  const response = await axiosClient.get('/employees/search', {
    params: { query },
  });
  return response.data;
};

export const createEmployee = async (employee) => {
  const response = await axiosClient.post('/employees', employee);
  return response.data;
};

export const updateEmployee = async (id, employee) => {
  const response = await axiosClient.put(`/employees/${id}`, employee);
  return response.data;
};

export const deleteEmployee = async (id) => {
  await axiosClient.delete(`/employees/${id}`);
};
