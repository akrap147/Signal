import client from './client';

export const channelApi = {
  createChannel: async (serverId, categoryId, name, type = 'TEXT') => {
    // request: { serverId, categoryId, name, type }
    const response = await client.post('/channels', { serverId, categoryId, name, type });
    return response.data; // channelId
  },
  updateChannel: async (channelId, name) => {
    const response = await client.patch(`/channels/${channelId}`, { name });
    return response.data;
  },
  deleteChannel: async (channelId) => {
    const response = await client.delete(`/channels/${channelId}`);
    return response.data;
  },
  updateOrder: async (categoryId, orderedIds) => {
    const response = await client.put('/channels/order', orderedIds, {
      params: { categoryId }
    });
    return response.data;
  },
};
