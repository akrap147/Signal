import React from 'react';
import useServerStore from '../../stores/useServerStore';
import { useServerDetails } from '../../hooks/useServerQueries';

const ChatArea = () => {
  const { activeServerId, activeChannelId } = useServerStore();
  const { data: serverDetails } = useServerDetails(activeServerId);
  
  // 현재 선택된 채널 이름 찾기 (Memoization 추천되지만 일단 단순 구현)
  const currentChannelName = React.useMemo(() => {
    if (activeServerId === 'dm') return 'Friend';
    if (!serverDetails) return '...';
    
    // 리스트 Flattening해서 찾기
    const allChannels = serverDetails.categories.flatMap(c => c.channels);
    return allChannels.find(ch => ch.id === activeChannelId)?.name || 'Select Channel';
  }, [activeServerId, serverDetails, activeChannelId]);

  const mockMessages = [
    { id: 1, user: 'System', text: `Welcome to #${currentChannelName}!`, mine: false, time: 'Now' },
  ];

  return (
    <main className="chat-workspace">
      <header className="workspace-header">
        <div style={{ fontSize: '1.1rem', fontWeight: '700' }}>
          {activeServerId === 'dm' ? 'Friends' : `# ${currentChannelName}`}
        </div>
        <div style={{ color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer' }}>...</div>
      </header>

      <div className="messages-container">
        {mockMessages.map((msg) => (
          <div key={msg.id} className={`message-bubble ${msg.mine ? 'mine' : ''}`}>
            <div className="message-info">
              <strong>{msg.user}</strong> <span>{msg.time}</span>
            </div>
            <div className="message-text">
              {msg.text}
            </div>
          </div>
        ))}
      </div>

      <footer className="input-section">
        <div className="input-box">
          <span style={{ fontSize: '1.2rem', cursor: 'pointer' }}>⊕</span>
          <input type="text" placeholder={`Message #${currentChannelName}`} />
          <span style={{ cursor: 'pointer' }}>🚀</span>
        </div>
      </footer>
    </main>
  );
};

export default ChatArea;
