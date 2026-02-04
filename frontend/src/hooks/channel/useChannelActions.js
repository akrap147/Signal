import { useMutation, useQueryClient } from '@tanstack/react-query';
import { channelApi } from '../../api/channel';

export const useChannelActions = () => {
  const queryClient = useQueryClient();

  const createChannelMutation = useMutation({
    mutationFn: ({ serverId, categoryId, name, type }) => 
      channelApi.createChannel(serverId, categoryId, name, type),
    onSuccess: (_, variables) => {
      // 서버 상세 정보 갱신 (채널 목록이 바뀜)
      queryClient.invalidateQueries(['serverDetails', variables.serverId]);
    },
  });

  return {
    createChannel: createChannelMutation.mutateAsync,
    isCreating: createChannelMutation.isPending,
  };
};
