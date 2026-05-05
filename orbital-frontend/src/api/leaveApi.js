import axiosClient from './axiosClient.js';

export const applyLeave = async (payload) => {
  const response = await axiosClient.post('/leaves', payload);
  return response.data;
};

export const getMyLeaves = async () => {
  const response = await axiosClient.get('/leaves');
  return response.data;
};

export const getAllLeaves = async () => {
  const response = await axiosClient.get('/leaves/all');
  return response.data;
};

export const approveLeave = async (id) => {
  const response = await axiosClient.put(`/leaves/${id}/approve`);
  return response.data;
};

export const rejectLeave = async (id) => {
  const response = await axiosClient.put(`/leaves/${id}/reject`);
  return response.data;
};
