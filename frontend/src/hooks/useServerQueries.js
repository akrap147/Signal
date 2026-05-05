import { useQuery } from '@tanstack/react-query';
import { serverApi } from '../api/server';

// 1. 내 서버 목록 조회
export const useMyServers = (userId) => {
  return useQuery({
    queryKey: ['myServers', userId],
    queryFn: async () => {
      if (!userId) return [];
      return await serverApi.getMyServers();
    },
    enabled: !!userId,
    staleTime: 1000 * 60 * 5,
  });
};

// 2. 서버 상세 조회
export const useServerDetails = (serverId) => {
  return useQuery({
    queryKey: ['serverDetails', serverId],
    queryFn: async () => {
      return await serverApi.getServerDetails(serverId);
    },
    enabled: !!serverId && serverId !== 'dm' && serverId !== '@me',
    staleTime: 1000 * 60,
  });
};
