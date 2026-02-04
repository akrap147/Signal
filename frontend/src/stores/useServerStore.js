import { create } from 'zustand';

const useServerStore = create((set) => ({
  activeServerId: '@me', // '@me', 'dm', or serverId (Long)
  activeChannelId: null,
  
  // Actions
  setActiveServer: (serverId) => set({ 
    activeServerId: serverId,
    activeChannelId: null // 서버 바꾸면 채널 선택도 초기화 (이후 Hook에서 첫 채널 자동 선택)
  }),
  
  setActiveChannel: (channelId) => set({ 
    activeChannelId: channelId 
  }),
}));

export default useServerStore;
