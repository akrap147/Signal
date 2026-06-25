import React from 'react';
import useServerStore from '../../stores/useServerStore';
import { useServerDetails } from '../../hooks/useServerQueries';
import useChatStore from '../../stores/useChatStore';
import useAuthStore from '../../stores/useAuthStore';
import useVoiceStore from '../../stores/useVoiceStore';
import { channelApi } from '../../api/channel';
import FriendsArea from '../friends/FriendsArea';
import CanvasArea from '../canvas/CanvasArea';

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
  const { activeVoiceChannelId, joinVoiceChannel } = useVoiceStore();
  const [inputValue, setInputValue] = React.useState('');
  const bottomRef = React.useRef(null);

  const userId = user?.id;
  const username = user?.username || user?.email || 'Unknown';

  const isDmMode = activeServerId === 'dm' || activeServerId === '@me';
  const showFriends = isDmMode && !activeChannelId;

  // 현재 채널 메타 (type 포함)
  const currentChannel = React.useMemo(() => {
    if (!serverDetails || isDmMode) return null;
    return serverDetails.categories.flatMap((c) => c.channels).find((ch) => ch.id === activeChannelId) ?? null;
  }, [serverDetails, activeChannelId, isDmMode]);

  const isVoiceChannel = currentChannel?.type === 'VOICE';

  const currentChannelName = React.useMemo(() => {
    if (isDmMode) {
      return dmChannels.find((dm) => dm.channelId === activeChannelId)?.friendName || 'DM';
    }
    return currentChannel?.name || (serverDetails ? 'Select Channel' : '...');
  }, [isDmMode, dmChannels, activeChannelId, currentChannel, serverDetails]);

  // 음성 채널 입장 시 자동 음성 연결
  React.useEffect(() => {
    if (!isVoiceChannel || !activeChannelId || !isConnected) return;
    if (activeVoiceChannelId !== activeChannelId) {
      joinVoiceChannel(activeChannelId);
    }
  }, [isVoiceChannel, activeChannelId, isConnected]);

  // 텍스트 채널 채팅 구독 (음성 채널은 제외)
  React.useEffect(() => {
    if (!activeChannelId || !isConnected || isVoiceChannel) return;

    useChatStore.setState({ messages: [] });
    const subscription = subscribeToChannel(activeChannelId, 'channel');

    channelApi.getChannelMessages(activeChannelId).then((history) => {
      useChatStore.setState((state) => {
        const historySeqIds = new Set(history.map((m) => m.seqId));
        const realtime = state.messages.filter((m) => !historySeqIds.has(m.seqId));
        return { messages: [...history, ...realtime] };
      });
    }).catch(() => {});

    return () => subscription?.unsubscribe();
  }, [activeChannelId, isConnected, isVoiceChannel, subscribeToChannel]);

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      if (e.nativeEvent.isComposing) return;
      e.preventDefault();
      if (inputValue.trim()) {
        sendMessage(activeChannelId, userId, username, inputValue);
        setInputValue('');
      }
    }
  };

  React.useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const inviteCode = serverDetails?.inviteCode;

  if (showFriends) return <FriendsArea />;

  // ── 음성 채널: 캔버스 전용 화면 ──
  if (isVoiceChannel) {
    return (
      <main className="chat-workspace">
        <header className="workspace-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ fontSize: '1.1rem', fontWeight: '700' }}>
              🎨 {currentChannelName}
            </div>
            {inviteCode && (
              <span
                style={{ fontSize: '0.8rem', background: '#444', padding: '2px 6px', borderRadius: '4px', cursor: 'pointer' }}
                onClick={() => { navigator.clipboard.writeText(inviteCode); alert('Copied!'); }}
              >
                Code: {inviteCode}
              </span>
            )}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            🎙️ 음성 + 🎨 캔버스
          </div>
        </header>
        <CanvasArea roomId={activeChannelId} />
      </main>
    );
  }

  // ── 텍스트 채널 / DM: 채팅 화면 ──
  return (
    <main className="chat-workspace">
      <header className="workspace-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ fontSize: '1.1rem', fontWeight: '700' }}>
            {isDmMode ? `@ ${currentChannelName}` : `# ${currentChannelName}`}
          </div>
          {!isDmMode && inviteCode && (
            <span
              style={{ fontSize: '0.8rem', background: '#444', padding: '2px 6px', borderRadius: '4px', cursor: 'pointer' }}
              onClick={() => { navigator.clipboard.writeText(inviteCode); alert('Copied!'); }}
            >
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
              <div className="message-text">{msg.content}</div>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>

      <footer className="input-section">
        <div className="input-box">
          <span style={{ fontSize: '1.2rem', cursor: 'pointer' }}>⊕</span>
          <input
            type="text"
            placeholder={`Message ${isDmMode ? `@ ${currentChannelName}` : `# ${currentChannelName}`}`}
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
