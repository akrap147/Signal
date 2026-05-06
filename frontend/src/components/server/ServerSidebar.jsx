import React, { useEffect, useState } from 'react';
import useServerStore from '../../stores/useServerStore';
import useAuthStore from '../../stores/useAuthStore';
import { useServerDetails } from '../../hooks/useServerQueries';
import { useQueryClient } from '@tanstack/react-query';
import clsx from 'clsx';
import CreateChannelModal from '../modals/CreateChannelModal';
import { friendApi } from '../../api/friend';
import { dmApi } from '../../api/channel';
import { serverApi } from '../../api/server';

const ServerSidebar = () => {
  const { activeServerId, activeChannelId, setActiveChannel } = useServerStore();
  const { user } = useAuthStore();
  const { data: serverDetails, isLoading, error } = useServerDetails(activeServerId);

  // Modal State
  const [isChannelModalOpen, setIsChannelModalOpen] = useState(false);
  const [targetCategoryId, setTargetCategoryId] = useState(null);
  const [toast, setToast] = useState(null); // { code, error }

  const isOwner = serverDetails?.ownerId === user?.id;

  const handleCopyInvite = async () => {
    try {
      const { inviteCode } = await serverApi.createInviteCode(activeServerId);
      await navigator.clipboard.writeText(inviteCode);
      setToast({ code: inviteCode });
      setTimeout(() => setToast(null), 3000);
    } catch {
      setToast({ error: true });
      setTimeout(() => setToast(null), 3000);
    }
  };

  // 서버가 바뀌어서 데이터가 로드되면 첫 번째 채널 자동 선택
  useEffect(() => {
    if (serverDetails?.categories?.length > 0 && !activeChannelId) {
        const firstChannel = serverDetails.categories[0].channels?.[0];
        if (firstChannel) {
            setActiveChannel(firstChannel.id);
        }
    }
  }, [serverDetails, activeChannelId, setActiveChannel]);

  const handleAddChannel = (catId) => {
      setTargetCategoryId(catId);
      setIsChannelModalOpen(true);
  };

  // DM / Friends View
  if (activeServerId === 'dm' || activeServerId === '@me') {
    return <FriendsSidebar />;
  }

  if (isLoading) return <aside className="sidebar">Loading Channels...</aside>;
  if (error) return <aside className="sidebar">Error Loading Server</aside>;

  return (
    <>
      {/* 토스트 */}
      {toast && (
        <div style={{
          position: 'fixed', bottom: '24px', left: '50%', transform: 'translateX(-50%)',
          background: toast.error ? '#ed4245' : '#23272a',
          color: 'white', borderRadius: '8px', padding: '12px 20px',
          boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
          display: 'flex', alignItems: 'center', gap: '12px',
          zIndex: 9999, animation: 'fadeIn 0.2s ease',
        }}>
          {toast.error ? (
            <span>초대 코드 생성에 실패했습니다.</span>
          ) : (
            <>
              <span style={{ fontSize: '1.1rem' }}>✅</span>
              <div>
                <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>초대 코드가 복사됐습니다!</div>
                <div style={{ fontFamily: 'monospace', fontSize: '1rem', color: '#5865f2', marginTop: '2px' }}>{toast.code}</div>
              </div>
            </>
          )}
        </div>
      )}
      <aside className="sidebar">
        <div className="sidebar-panel">
          <header className="sidebar-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>{serverDetails?.name || 'Server'}</span>
            {isOwner && (
              <button
                onClick={handleCopyInvite}
                style={{ background: '#5865f2', border: 'none', color: 'white', cursor: 'pointer', fontSize: '0.75rem', fontWeight: 600, padding: '3px 10px', borderRadius: '4px' }}
              >
                초대
              </button>
            )}
          </header>

          <div className="sidebar-list">
            {serverDetails?.categories?.map((category) => (
              <div key={category.id} style={{ marginBottom: '16px' }}>
                <div className="category-label" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>{category.name}</span>
                  <button 
                    onClick={() => handleAddChannel(category.id)}
                    style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: '1.1rem', lineHeight: 0 }}
                    title="Create Channel"
                  >
                    +
                  </button>
                </div>
                {category.channels?.map((ch) => (
                  <div 
                    key={ch.id} 
                    className={clsx('sidebar-item', { active: activeChannelId === ch.id })}
                    onClick={() => setActiveChannel(ch.id)}
                  >
                    <span style={{ opacity: 0.5 }}>#</span> {ch.name}
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>
         <UserPanel />
      </aside>

      {isChannelModalOpen && (
        <CreateChannelModal 
          selectedCategoryId={targetCategoryId} 
          onClose={() => setIsChannelModalOpen(false)} 
        />
      )}
    </>
  );
};

// DM 모드 사이드바
const FriendsSidebar = () => {
  const { activeChannelId, setActiveChannel, dmChannels, setDmChannels } = useServerStore();
  const [receivedCount, setReceivedCount] = useState(0);

  useEffect(() => {
    Promise.all([friendApi.getReceivedRequests(), dmApi.getMyDmChannels()])
      .then(([r, dms]) => { setReceivedCount(r.length); setDmChannels(dms); })
      .catch(() => {});
  }, [setDmChannels]);

  return (
    <aside className="sidebar">
      <div className="sidebar-panel">
        <header className="sidebar-header">다이렉트 메시지</header>
        <div className="sidebar-list">
          <div
            className={clsx('sidebar-item', { active: !activeChannelId })}
            onClick={() => setActiveChannel(null)}
          >
            <span style={{ marginRight: '8px' }}>👋</span> 친구
            {receivedCount > 0 && (
              <span style={{ marginLeft: 'auto', background: '#ed4245', color: 'white', borderRadius: '10px', padding: '1px 6px', fontSize: '0.75rem' }}>
                {receivedCount}
              </span>
            )}
          </div>
          {dmChannels.length > 0 && (
            <div style={{ padding: '8px 12px', color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', marginTop: '8px' }}>
              다이렉트 메시지
            </div>
          )}
          {dmChannels.map((dm) => (
            <div
              key={dm.channelId}
              className={clsx('sidebar-item', { active: activeChannelId === dm.channelId })}
              onClick={() => setActiveChannel(dm.channelId)}
            >
              <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: '#5865f2', marginRight: '8px', flexShrink: 0 }} />
              {dm.friendName}
            </div>
          ))}
        </div>
      </div>
      <UserPanel />
    </aside>
  );
};

// 로그아웃 확인 모달
const LogoutModal = ({ onConfirm, onCancel }) => (
  <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={onCancel}>
    <div className="bg-zinc-800 p-6 rounded-lg shadow-xl w-80 text-white" onClick={(e) => e.stopPropagation()}>
      <h2 className="text-lg font-bold mb-2">로그아웃</h2>
      <p className="text-zinc-400 text-sm mb-6">정말 로그아웃 하시겠어요?</p>
      <div className="flex justify-end gap-2">
        <button
          onClick={onCancel}
          className="px-4 py-2 text-sm rounded bg-zinc-700 hover:bg-zinc-600 text-zinc-300 transition-colors"
        >
          취소
        </button>
        <button
          onClick={onConfirm}
          className="px-4 py-2 text-sm rounded bg-red-600 hover:bg-red-500 text-white font-semibold transition-colors"
        >
          로그아웃
        </button>
      </div>
    </div>
  </div>
);

// 하단 유저 프로필 컴포넌트
const UserPanel = () => {
  const { user, logout } = useAuthStore();
  const queryClient = useQueryClient();
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const displayedName = user?.username || user?.email || 'Unknown User';

  const handleConfirmLogout = () => {
    logout();
    queryClient.clear();
  };

  return (
    <>
      <div className="user-card">
        <div className="user-avatar" />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <div style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>{displayedName}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Online</div>
        </div>
        <button
          onClick={() => setShowLogoutModal(true)}
          className="px-3 py-1 text-xs rounded bg-zinc-700 hover:bg-red-600 text-zinc-300 hover:text-white font-semibold transition-colors"
        >
          로그아웃
        </button>
      </div>

      {showLogoutModal && (
        <LogoutModal
          onConfirm={handleConfirmLogout}
          onCancel={() => setShowLogoutModal(false)}
        />
      )}
    </>
  );
};

export default ServerSidebar;
