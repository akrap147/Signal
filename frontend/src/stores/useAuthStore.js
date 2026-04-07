import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

const useAuthStore = create(
  persist(
    (set) => ({
      accessToken: null,
      user: null, // { id, email, username, ... }

      setAccessToken: (token) => set({ accessToken: token }),
      setUser: (user) => set({ user }),
      
      logout: () => set({ accessToken: null, user: null }),
    }),
    {
      name: 'auth-storage', // local storage key
      storage: createJSONStorage(() => localStorage),
    }
  )
);

export default useAuthStore;
