import { create } from 'zustand';

const useServerStore = create((set) => ({
  activeServerId: '@me', // '@me', 'dm', or serverId (Long)
  activeChannelId: null,
  dmChannels: [], // [{ channelId, friendId, friendName }]

  // Actions
  setActiveServer: (serverId) => set({
    activeServerId: serverId,
    activeChannelId: null // 서버 바꾸면 채널 선택도 초기화 (이후 Hook에서 첫 채널 자동 선택)
  }),

  setActiveChannel: (channelId) => set({
    activeChannelId: channelId
  }),

  setDmChannels: (channels) => set({ dmChannels: channels }),

  addOrUpdateDmChannel: (dm) => set((state) => {
    const exists = state.dmChannels.find((c) => c.channelId === dm.channelId);
    if (exists) return {};
    return { dmChannels: [...state.dmChannels, dm] };
  }),
}));

export default useServerStore;
