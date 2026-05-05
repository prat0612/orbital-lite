import axiosClient from './axiosClient.js';

export const getUsers = async () => {
  const response = await axiosClient.get('/users');
  return response.data;
};

export const createUser = async (payload) => {
  const response = await axiosClient.post('/users', payload);
  return response.data;
};

export const updateUser = async (id, payload) => {
  const response = await axiosClient.put(`/users/${id}`, payload);
  return response.data;
};

export const deleteUser = async (id) => {
  await axiosClient.delete(`/users/${id}`);
};
