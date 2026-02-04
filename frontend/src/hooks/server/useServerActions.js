import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { serverApi } from '../../api/server';
import useServerStore from '../../stores/useServerStore';
import useAuthStore from '../../stores/useAuthStore';

export const useServerActions = () => {
  const queryClient = useQueryClient();
  const { setActiveServer } = useServerStore();
  const { user } = useAuthStore();
  const userId = user?.id;
  const navigate = useNavigate();

  const createServerMutation = useMutation({
    mutationFn: (name) => serverApi.createServer(name),
    onSuccess: (data, variables) => {
      // 1. Optimistic Update (Optional)
      if (data?.serverId && userId) {
        queryClient.setQueryData(['myServers', userId], (old = []) => {
          const newServer = { id: data.serverId, name: variables };
          return [...old, newServer];
        });
      }
      
      // 2. Invalidate
      queryClient.invalidateQueries(['myServers']);

      // 3. Navigate
      if (data?.serverId) {
        setActiveServer(data.serverId); // UI 즉시 반영 (MainPage useEffect보다 빠를 수 있음)
        navigate(`/channels/${data.serverId}`);
      }
    },
  });

  const joinServerMutation = useMutation({
    mutationFn: (inviteCode) => serverApi.joinServer(inviteCode),
    onSuccess: (data) => {
      queryClient.invalidateQueries(['myServers']);
      if (data?.serverId) {
        setActiveServer(data.serverId);
        navigate(`/channels/${data.serverId}`);
      }
    },
  });

  return {
    createServer: createServerMutation.mutateAsync,
    joinServer: joinServerMutation.mutateAsync,
    isCreating: createServerMutation.isPending,
    isJoining: joinServerMutation.isPending,
  };
};
