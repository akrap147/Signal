import client from './client';

export const authApi = {
  login: async (email, password) => {
    const response = await client.post('/auth/login', { email, password });
    return response.data;
  },
  signup: async (data) => {
    // data: { email, password, username }
    const response = await client.post('/users/signup', data);
    return response.data;
  },
  getMyProfile: async () => {
    const response = await client.get('/users/me');
    return response.data;
  },
};
