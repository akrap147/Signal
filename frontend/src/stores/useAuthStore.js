import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// persist 미들웨어를 사용해 새로고침해도 로그인 유지 (localStorage 저장)
const useAuthStore = create(
  persist(
    (set) => ({
      userId: null,
      username: null,
      isAuthenticated: false,

      // Actions
      login: (userId, username) => set({ userId, username, isAuthenticated: true }),
      logout: () => set({ userId: null, username: null, isAuthenticated: false }),
    }),
    {
      name: 'auth-storage', // localStorage key
    }
  )
);

export default useAuthStore;
