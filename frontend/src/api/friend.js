import client from './client';

export const friendApi = {
  getMyFriends: async () => {
    const response = await client.get('/friends');
    return response.data;
  },
  getReceivedRequests: async () => {
    const response = await client.get('/friends/received-requests');
    return response.data;
  },
  getSentRequests: async () => {
    const response = await client.get('/friends/sent-requests');
    return response.data;
  },
  sendRequest: async (friendName) => {
    const response = await client.post('/friends/request', { friendName });
    return response.data;
  },
  acceptRequest: async (requesterId) => {
    await client.post(`/friends/accept/${requesterId}`);
  },
  removeFriend: async (friendId) => {
    await client.delete(`/friends/${friendId}`);
  },
};
