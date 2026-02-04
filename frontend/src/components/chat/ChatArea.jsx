import React from 'react';
import useServerStore from '../../stores/useServerStore';
import { useServerDetails } from '../../hooks/useServerQueries';

import useChatStore from '../../stores/useChatStore';
import useAuthStore from '../../stores/useAuthStore';

const ChatArea = () => {
  const { activeServerId, activeChannelId } = useServerStore();
  const { data: serverDetails } = useServerDetails(activeServerId);
  const { user } = useAuthStore();
  const userId = user?.id; // 식별자
  const username = user?.username || user?.email || 'Unknown'; // 표시용 Names
  
  const { messages, sendMessage, subscribeToChannel } = useChatStore();
  const [inputValue, setInputValue] = React.useState('');

  // 1. 채널 바뀔 때마다 구독 (Subscribe)
  React.useEffect(() => {
    if (activeChannelId) {
      // DM인 경우 type='dm'으로 처리해야 함 (추우 구현)
      // 현재는 일단 모두 CHANNEL로 가정
      subscribeToChannel(activeChannelId, 'channel');
    }
  }, [activeChannelId, subscribeToChannel]);
  
  // 현재 선택된 채널 이름 찾기
  const currentChannelName = React.useMemo(() => {
    if (activeServerId === 'dm') return 'Friend';
    if (!serverDetails) return '...';
    const allChannels = serverDetails.categories.flatMap(c => c.channels);
    return allChannels.find(ch => ch.id === activeChannelId)?.name || 'Select Channel';
  }, [activeServerId, serverDetails, activeChannelId]);

  // 2. 메시지 전송 (엔터 키)
  const handleKeyDown = (e) => {
    // 한글 입력 중 엔터 키 입력 시 중복 전송 방지 등을 위해 isComposing 체크를 할 수도 있지만,
    // 현재 "전송이 안 된다"는 이슈가 있으므로 체크를 제거하고 기본 동작 방지(preventDefault)를 먼저 수행
    if (e.key === 'Enter' && !e.shiftKey) {
      // 한글 조합 중이더라도 엔터를 누르면 전송하도록 허용 (사용자 경험상 이게 더 나음)
      if (e.nativeEvent.isComposing) return;
      
      e.preventDefault();
      if (inputValue.trim()) {
        sendMessage(activeChannelId, userId, inputValue); // 전송!
        setInputValue('');
      }
    }
  };

  const inviteCode = serverDetails?.inviteCode; // 서버 상세 정보에 inviteCode 등재 가정

  return (
    <main className="chat-workspace">
      <header className="workspace-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ fontSize: '1.1rem', fontWeight: '700' }}>
            {activeServerId === 'dm' ? 'Friends' : `# ${currentChannelName}`}
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
                <strong>{msg.senderId}</strong> 
                {/* 시간 정보가 아직 없으므로 생략하거나 msg.createdAt 추가 필요 */}
                <span>Now</span> 
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
                  sendMessage(activeChannelId, userId, inputValue);
                  setInputValue('');
              }
          }}>🚀</span>
        </div>
      </footer>
    </main>
  );
};

export default ChatArea;
