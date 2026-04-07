import { create } from 'zustand';
import {
  getMyFriends,
  getMySentRequests,
  getMyReceivedRequests,
  sendFriendRequest,
  acceptFriendRequest,
  removeFriendship,
} from '../api/friendship';

const useFriendStore = create((set, get) => ({
  // 친구 목록 (accepted)
  friends: [],
  
  // 받은 친구 요청 (pending, 내가 수락/거절 가능)
  receivedRequests: [],
  
  // 보낸 친구 요청 (pending, 상대방이 수락/거절 가능)
  sentRequests: [],
  
  // 로딩 상태
  isLoading: false,
  
  // 에러 상태
  error: null,

  /**
   * 모든 친구 관련 데이터 로드
   */
  loadAllFriendData: async () => {
    set({ isLoading: true, error: null });
    try {
      const [friends, receivedRequests, sentRequests] = await Promise.all([
        getMyFriends(),
        getMyReceivedRequests(),
        getMySentRequests(),
      ]);
      
      set({
        friends,
        receivedRequests,
        sentRequests,
        isLoading: false,
      });
    } catch (error) {
      console.error('Failed to load friend data:', error);
      set({ 
        error: error.response?.data?.message || '친구 데이터 로드 실패',
        isLoading: false 
      });
    }
  },

  /**
   * 친구 목록만 다시 로드
   */
  loadFriends: async () => {
    try {
      const friends = await getMyFriends();
      set({ friends });
    } catch (error) {
      console.error('Failed to load friends:', error);
      set({ error: error.response?.data?.message || '친구 목록 로드 실패' });
    }
  },

  /**
   * 받은 요청 목록만 다시 로드
   */
  loadReceivedRequests: async () => {
    try {
      const receivedRequests = await getMyReceivedRequests();
      set({ receivedRequests });
    } catch (error) {
      console.error('Failed to load received requests:', error);
    }
  },

  /**
   * 보낸 요청 목록만 다시 로드
   */
  loadSentRequests: async () => {
    try {
      const sentRequests = await getMySentRequests();
      set({ sentRequests });
    } catch (error) {
      console.error('Failed to load sent requests:', error);
    }
  },

  /**
   * 친구 요청 보내기
   */
  sendRequest: async (friendEmail) => {
    try {
      await sendFriendRequest(friendEmail);
      // 보낸 요청 목록 갱신
      await get().loadSentRequests();
      return { success: true };
    } catch (error) {
      console.error('Failed to send friend request:', error);
      const message = error.response?.data?.message || '친구 요청 전송 실패';
      set({ error: message });
      return { success: false, error: message };
    }
  },

  /**
   * 친구 요청 수락
   */
  acceptRequest: async (requesterId) => {
    try {
      await acceptFriendRequest(requesterId);
      // 전체 데이터 갱신 (받은 요청 제거 + 친구 목록 추가)
      await get().loadAllFriendData();
      return { success: true };
    } catch (error) {
      console.error('Failed to accept friend request:', error);
      const message = error.response?.data?.message || '친구 요청 수락 실패';
      set({ error: message });
      return { success: false, error: message };
    }
  },

  /**
   * 친구 요청 거절 또는 친구 삭제
   */
  removeFriend: async (friendId) => {
    try {
      await removeFriendship(friendId);
      // 전체 데이터 갱신
      await get().loadAllFriendData();
      return { success: true };
    } catch (error) {
      console.error('Failed to remove friendship:', error);
      const message = error.response?.data?.message || '친구 삭제 실패';
      set({ error: message });
      return { success: false, error: message };
    }
  },

  /**
   * 에러 초기화
   */
  clearError: () => set({ error: null }),
}));

export default useFriendStore;
