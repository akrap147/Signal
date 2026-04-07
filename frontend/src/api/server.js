import client from './client';

export const serverApi = {
  createServer: async (name) => {
    // dto: { name: string }
    const response = await client.post('/servers', { name });
    return response.data; // { serverId: number }
  },
  getMyServers: async () => {
    const response = await client.get('/servers/my');
    return response.data; // [{ id, name, iconImage }]
  },
  getServerDetails: async (serverId) => {
    const response = await client.get(`/servers/${serverId}`);
    return response.data;
  },
  createInviteCode: async (serverId) => {
    const response = await client.post(`/servers/${serverId}/invites`);
    return response.data; // { inviteCode: string }
  },
  joinServer: async (inviteCode) => {
    const response = await client.post('/servers/join', { inviteCode });
    return response.data; // { serverId: number }
  },
  updateServerName: async (serverId, name) => {
    const response = await client.patch(`/servers/${serverId}/name`, { name });
    return response.data;
  },
  leaveServer: async (serverId) => {
    const response = await client.post(`/servers/${serverId}/leave`);
    return response.data;
  },
  deleteServer: async (serverId) => {
    const response = await client.delete(`/servers/${serverId}`);
    return response.data;
  },
};
