import axiosClient from './axiosClient.js';

export const login = async (credentials) => {
  const response = await axiosClient.post('/auth/login', credentials);
  return response.data;
};

export const register = async (payload) => {
  const response = await axiosClient.post('/auth/register', payload);
  return response.data;
};
