import { useQuery } from '@tanstack/react-query';
import client from '../api/client';

// 1. 내 서버 목록 조회
export const useMyServers = (userId) => {
  return useQuery({
    queryKey: ['myServers', userId],
    queryFn: async () => {
      // userId가 없으면(로그인 전) 빈 배열 반환하거나 에러 처리
      if (!userId) return [];
      const response = await client.get(`/servers/my?userId=${userId}`);
      return response.data;
    },
    enabled: !!userId, // userId가 있을 때만 실행
    staleTime: 1000 * 60 * 5, // 5분간 캐시 유지 (서버 목록이 자주 안 바뀐다고 가정)
  });
};

// 2. 서버 상세 조회 (카테고리 & 채널 포함)
export const useServerDetails = (serverId) => {
  return useQuery({
    queryKey: ['serverDetails', serverId],
    queryFn: async () => {
      const response = await client.get(`/servers/${serverId}`);
      return response.data;
    },
    enabled: !!serverId && serverId !== 'dm', // 'dm'이 아니고 ID가 있을 때만 실행
    staleTime: 1000 * 60, // 1분간 캐시
  });
};
