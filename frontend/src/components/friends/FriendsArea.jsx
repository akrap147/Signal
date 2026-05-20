import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { friendApi, presenceApi } from '../../api/friend';
import { dmApi } from '../../api/channel';
import useServerStore from '../../stores/useServerStore';
import useChatStore from '../../stores/useChatStore';

const TABS = ['전체 친구', '대기 중', '친구 추가'];

export default function FriendsArea() {
  const [tab, setTab] = useState('전체 친구');
  const navigate = useNavigate();
  const { setActiveServer, setActiveChannel, addOrUpdateDmChannel } = useServerStore();

  const handleOpenDm = async (friendId) => {
    try {
      const dm = await dmApi.getOrCreateDmChannel(friendId);
      addOrUpdateDmChannel(dm);
      setActiveServer('@me');
      setActiveChannel(dm.channelId);
      navigate(`/channels/@me/${dm.channelId}`);
    } catch {
      // 조용히 실패
    }
  };
  const [friends, setFriends] = useState([]);
  const [received, setReceived] = useState([]);
  const [friendNameInput, setFriendNameInput] = useState('');
  const [message, setMessage] = useState(null); // { type: 'success'|'error', text }
  const [onlineIds, setOnlineIds] = useState(new Set()); // Set<string>

  const { client, isConnected } = useChatStore();

  const loadFriends = useCallback(async () => {
    let friendsList = [];
    try {
      const [fl, receivedList] = await Promise.all([
        friendApi.getMyFriends(),
        friendApi.getReceivedRequests(),
      ]);
      friendsList = fl;
      setFriends(fl);
      setReceived(receivedList);
    } catch {
      // 조용히 실패
    }

    try {
      const friendIds = friendsList.map((f) => f.friendId);
      const onlineSet = await presenceApi.getOnlineAmong(friendIds);
      setOnlineIds(onlineSet);
    } catch {
      // presence 실패해도 친구 목록은 정상 표시
    }
  }, []);

  useEffect(() => {
    loadFriends();
  }, [loadFriends]);

  // /topic/presence 구독으로 실시간 온/오프라인 반영
  useEffect(() => {
    if (!client || !isConnected) return;

    const sub = client.subscribe('/topic/presence', (msg) => {
      const { userId, status } = JSON.parse(msg.body);
      setOnlineIds((prev) => {
        const next = new Set(prev);
        if (status === 'ONLINE') next.add(String(userId));
        else next.delete(String(userId));
        return next;
      });
    });

    return () => sub.unsubscribe();
  }, [client, isConnected]);

  const handleSendRequest = async () => {
    const name = friendNameInput.trim();
    if (!name) return;
    try {
      await friendApi.sendRequest(name);
      setMessage({ type: 'success', text: '친구 요청을 보냈습니다.' });
      setFriendNameInput('');
      await loadFriends();
    } catch {
      setMessage({ type: 'error', text: '친구 요청에 실패했습니다. 사용자명을 확인해주세요.' });
    }
  };

  const handleAccept = async (requesterId) => {
    try {
      await friendApi.acceptRequest(requesterId);
      await loadFriends();
    } catch {
      setMessage({ type: 'error', text: '요청 수락에 실패했습니다.' });
    }
  };

  const handleRemove = async (friendId) => {
    try {
      await friendApi.removeFriend(friendId);
      await loadFriends();
    } catch {
      setMessage({ type: 'error', text: '친구 삭제에 실패했습니다.' });
    }
  };

  return (
    <main className="chat-workspace">
      <header className="workspace-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <span style={{ fontWeight: 700 }}>👋 친구</span>
          {TABS.map((t) => (
            <button
              key={t}
              onClick={() => { setTab(t); setMessage(null); }}
              style={{
                background: tab === t ? 'var(--bg-tertiary, #3a3c42)' : 'none',
                border: 'none',
                color: tab === t ? 'white' : 'var(--text-muted)',
                padding: '4px 12px',
                borderRadius: '4px',
                cursor: 'pointer',
                fontWeight: tab === t ? 600 : 400,
              }}
            >
              {t}
              {t === '대기 중' && received.length > 0 && (
                <span style={{ marginLeft: '6px', background: '#ed4245', color: 'white', borderRadius: '10px', padding: '1px 6px', fontSize: '0.75rem' }}>
                  {received.length}
                </span>
              )}
            </button>
          ))}
        </div>
      </header>

      <div style={{ padding: '24px', overflowY: 'auto', flex: 1 }}>
        {message && (
          <div style={{
            marginBottom: '16px',
            padding: '10px 16px',
            borderRadius: '6px',
            background: message.type === 'success' ? '#2d7d46' : '#8c1c1c',
            color: 'white',
          }}>
            {message.text}
          </div>
        )}

        {/* 전체 친구 */}
        {tab === '전체 친구' && (
          <div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', marginBottom: '12px' }}>
              친구 — {friends.length}
            </div>
            {friends.length === 0 ? (
              <p style={{ color: 'var(--text-muted)' }}>아직 친구가 없습니다.</p>
            ) : (
              friends.map((f) => (
                <div key={f.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 12px', borderRadius: '8px', marginBottom: '4px', background: 'var(--bg-secondary, #2f3136)' }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>{f.friendInfo?.username ?? `User #${f.friendId}`}</div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '5px', marginTop: '2px' }}>
                      <span style={{
                        width: '8px', height: '8px', borderRadius: '50%', flexShrink: 0,
                        background: onlineIds.has(String(f.friendId)) ? '#3ba55d' : '#747f8d',
                      }} />
                      <span style={{ fontSize: '0.8rem', color: onlineIds.has(String(f.friendId)) ? '#3ba55d' : '#96989d' }}>
                        {onlineIds.has(String(f.friendId)) ? '온라인' : '오프라인'}
                      </span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      onClick={() => handleOpenDm(f.friendId)}
                      style={{ background: '#5865f2', border: 'none', color: 'white', borderRadius: '4px', padding: '4px 10px', cursor: 'pointer', fontSize: '0.85rem' }}
                    >
                      메시지
                    </button>
                    <button
                      onClick={() => handleRemove(f.friendId)}
                      style={{ background: 'none', border: '1px solid #ed4245', color: '#ed4245', borderRadius: '4px', padding: '4px 10px', cursor: 'pointer', fontSize: '0.85rem' }}
                    >
                      삭제
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* 대기 중 */}
        {tab === '대기 중' && (
          <div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', marginBottom: '12px' }}>
              받은 요청 — {received.length}
            </div>
            {received.length === 0 ? (
              <p style={{ color: 'var(--text-muted)' }}>받은 친구 요청이 없습니다.</p>
            ) : (
              received.map((r) => (
                <div key={r.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 12px', borderRadius: '8px', marginBottom: '4px', background: 'var(--bg-secondary, #2f3136)' }}>
                  <div style={{ fontWeight: 600 }}>{r.friendInfo?.username ?? `User #${r.userId}`}</div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      onClick={() => handleAccept(r.userId)}
                      style={{ background: '#3ba55d', border: 'none', color: 'white', borderRadius: '4px', padding: '4px 10px', cursor: 'pointer', fontSize: '0.85rem' }}
                    >
                      수락
                    </button>
                    <button
                      onClick={() => handleRemove(r.userId)}
                      style={{ background: 'none', border: '1px solid #ed4245', color: '#ed4245', borderRadius: '4px', padding: '4px 10px', cursor: 'pointer', fontSize: '0.85rem' }}
                    >
                      거절
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* 친구 추가 */}
        {tab === '친구 추가' && (
          <div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '16px' }}>
              상대방의 사용자명을 입력해 친구 요청을 보내세요.
            </div>
            <div style={{ display: 'flex', gap: '8px' }}>
              <input
                type="text"
                placeholder="사용자명 입력"
                value={friendNameInput}
                onChange={(e) => setFriendNameInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSendRequest()}
                style={{
                  flex: 1,
                  padding: '10px 14px',
                  borderRadius: '6px',
                  border: '1px solid #4f545c',
                  background: '#40444b',
                  color: 'white',
                  fontSize: '1rem',
                }}
              />
              <button
                onClick={handleSendRequest}
                style={{ background: '#5865f2', border: 'none', color: 'white', borderRadius: '6px', padding: '10px 20px', cursor: 'pointer', fontWeight: 600 }}
              >
                친구 요청
              </button>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
