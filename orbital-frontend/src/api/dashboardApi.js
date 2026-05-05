import axiosClient from './axiosClient.js';

export const getDashboardSummary = async () => {
  const response = await axiosClient.get('/dashboard/summary');
  return response.data;
};

export const getLeaveReport = async () => {
  const response = await axiosClient.get('/reports/leaves');
  return response.data;
};

export const getEmployeeReport = async () => {
  const response = await axiosClient.get('/reports/employees');
  return response.data;
};

export const getNotifications = async () => {
  const response = await axiosClient.get('/notifications');
  return response.data;
};
