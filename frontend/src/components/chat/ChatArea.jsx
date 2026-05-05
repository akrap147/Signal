import React from 'react';
import useServerStore from '../../stores/useServerStore';
import { useServerDetails } from '../../hooks/useServerQueries';

import useChatStore from '../../stores/useChatStore';
import useAuthStore from '../../stores/useAuthStore';
import { channelApi } from '../../api/channel';
import FriendsArea from '../friends/FriendsArea';

function formatTimestamp(ts, createdAt) {
  const date = ts ? new Date(ts) : createdAt ? new Date(createdAt) : null;
  if (!date) return '';
  const now = new Date();
  const isToday = date.toDateString() === now.toDateString();
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  if (isToday) return `${hh}:${mm}`;
  return `${date.getMonth() + 1}월 ${date.getDate()}일 ${hh}:${mm}`;
}

const ChatArea = () => {
  const { activeServerId, activeChannelId, dmChannels } = useServerStore();
  const { data: serverDetails } = useServerDetails(activeServerId);
  const { user } = useAuthStore();
  const { messages, sendMessage, subscribeToChannel, isConnected } = useChatStore();
  const [inputValue, setInputValue] = React.useState('');

  const userId = user?.id;
  const username = user?.username || user?.email || 'Unknown';

  const isDmMode = activeServerId === 'dm' || activeServerId === '@me';
  const showFriends = isDmMode && !activeChannelId;

  // 1. 채널 바뀔 때마다 구독 + 히스토리 로드
  React.useEffect(() => {
    if (!activeChannelId || !isConnected) return;

    // 초기화 후 구독 시작 (실시간 메시지는 여기서부터 쌓임)
    useChatStore.setState({ messages: [] });
    subscribeToChannel(activeChannelId, 'channel');

    // 히스토리 로드 후, 구독 중 도착한 실시간 메시지와 merge
    channelApi.getChannelMessages(activeChannelId).then((history) => {
      useChatStore.setState((state) => {
        const historySeqIds = new Set(history.map((m) => m.seqId));
        const realtime = state.messages.filter((m) => !historySeqIds.has(m.seqId));
        return { messages: [...history, ...realtime] };
      });
    }).catch(() => {});
  }, [activeChannelId, isConnected, subscribeToChannel]);
  
  // 현재 선택된 채널 이름 찾기
  const currentChannelName = React.useMemo(() => {
    if (activeServerId === '@me' || activeServerId === 'dm') {
      return dmChannels.find((dm) => dm.channelId === activeChannelId)?.friendName || 'DM';
    }
    if (!serverDetails) return '...';
    const allChannels = serverDetails.categories.flatMap(c => c.channels);
    return allChannels.find(ch => ch.id === activeChannelId)?.name || 'Select Channel';
  }, [activeServerId, serverDetails, activeChannelId, dmChannels]);

  // 2. 메시지 전송 (엔터 키)
  const handleKeyDown = (e) => {
    // 한글 입력 중 엔터 키 입력 시 중복 전송 방지 등을 위해 isComposing 체크를 할 수도 있지만,
    // 현재 "전송이 안 된다"는 이슈가 있으므로 체크를 제거하고 기본 동작 방지(preventDefault)를 먼저 수행
    if (e.key === 'Enter' && !e.shiftKey) {
      // 한글 조합 중이더라도 엔터를 누르면 전송하도록 허용 (사용자 경험상 이게 더 나음)
      if (e.nativeEvent.isComposing) return;
      
      e.preventDefault();
      if (inputValue.trim()) {
        sendMessage(activeChannelId, userId, username, inputValue); // 전송!
        setInputValue('');
      }
    }
  };

  const inviteCode = serverDetails?.inviteCode;

  if (showFriends) return <FriendsArea />;

  return (
    <main className="chat-workspace">
      <header className="workspace-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ fontSize: '1.1rem', fontWeight: '700' }}>
            {(activeServerId === '@me' || activeServerId === 'dm') ? `@ ${currentChannelName}` : `# ${currentChannelName}`}
          </div>
          {activeServerId !== 'dm' && inviteCode && (
            <span style={{ fontSize: '0.8rem', background: '#444', padding: '2px 6px', borderRadius: '4px', cursor: 'pointer' }}
                  onClick={() => {navigator.clipboard.writeText(inviteCode); alert('Copied!')}}>
              Code: {inviteCode}
            </span>
          )}
        </div>
        <div style={{ color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer' }}>...</div>
      </header>

      <div className="messages-container">
        {messages.map((msg, index) => {
          const isMine = msg.senderId === userId;
          return (
            <div key={index} className={`message-bubble ${isMine ? 'mine' : ''}`}>
              <div className="message-info">
                <strong>{msg.senderName ?? msg.senderId}</strong>
                <span>{formatTimestamp(msg.ts, msg.createdAt)}</span>
              </div>
              <div className="message-text">
                {msg.content}
              </div>
            </div>
          );
        })}
      </div>

      <footer className="input-section">
        <div className="input-box">
          <span style={{ fontSize: '1.2rem', cursor: 'pointer' }}>⊕</span>
          <input 
            type="text" 
            placeholder={`Message #${currentChannelName}`} 
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <span style={{ cursor: 'pointer' }} onClick={() => {
              if (inputValue.trim()) {
                  sendMessage(activeChannelId, userId, username, inputValue);
                  setInputValue('');
              }
          }}>🚀</span>
        </div>
      </footer>
    </main>
  );
};

export default ChatArea;
