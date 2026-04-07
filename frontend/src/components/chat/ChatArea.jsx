import React from 'react';
import useServerStore from '../../stores/useServerStore';
import { useServerDetails } from '../../hooks/useServerQueries';

import useChatStore from '../../stores/useChatStore';
import useAuthStore from '../../stores/useAuthStore';
import FriendsView from '../friends/FriendsView';

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
      // DM 모드 감지: activeServerId가 'dm'이면 DM, 아니면 CHANNEL
      const channelType = activeServerId === 'dm' ? 'dm' : 'channel';
      subscribeToChannel(activeChannelId, channelType);
    }
  }, [activeChannelId, activeServerId, subscribeToChannel]);
  
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
        const messageType = activeServerId === 'dm' ? 'DM' : 'CHANNEL';
        sendMessage(activeChannelId, userId, inputValue, messageType); // 전송!
        setInputValue('');
      }
    }
  };

  const inviteCode = serverDetails?.inviteCode; // 서버 상세 정보에 inviteCode 등재 가정

  // '@me' 모드일 때만 FriendsView 렌더링 (친구 목록 화면)
  // 'dm' 모드일 때는 DM 대화창을 표시
  if (activeServerId === '@me') {
    return <FriendsView />;
  }

  // DM 모드인지 확인 (activeServerId가 'dm'이고 activeChannelId가 DM roomId 형태)
  const isDMMode = activeServerId === 'dm' && activeChannelId;
  
  // DM 상대방 정보 추출 (roomId에서 friendId 추출)
  let dmPartnerName = 'Unknown';
  if (isDMMode && activeChannelId) {
    // roomId 형태: "3_5" → userId가 3이거나 5
    const [id1, id2] = activeChannelId.split('_').map(Number);
    const friendId = id1 === userId ? id2 : id1;
    dmPartnerName = `User ${friendId}`; // TODO: 실제 친구 이름으로 변경
  }

  return (
    <main className="chat-workspace">
      <header className="workspace-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ fontSize: '1.1rem', fontWeight: '700' }}>
            {isDMMode ? `💬 ${dmPartnerName}` : `# ${currentChannelName}`}
          </div>
          {!isDMMode && inviteCode && (
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
            placeholder={isDMMode ? `Message ${dmPartnerName}` : `Message #${currentChannelName}`}
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <span style={{ cursor: 'pointer' }} onClick={() => {
              if (inputValue.trim()) {
                  const messageType = activeServerId === 'dm' ? 'DM' : 'CHANNEL';
                  sendMessage(activeChannelId, userId, inputValue, messageType);
                  setInputValue('');
              }
          }}>🚀</span>
        </div>
      </footer>
    </main>
  );
};

export default ChatArea;
