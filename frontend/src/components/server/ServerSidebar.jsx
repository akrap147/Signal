import React, { useEffect, useState } from 'react';
import useServerStore from '../../stores/useServerStore';
import useAuthStore from '../../stores/useAuthStore';
import { useServerDetails } from '../../hooks/useServerQueries';
import { useQueryClient } from '@tanstack/react-query';
import clsx from 'clsx';
import CreateChannelModal from '../modals/CreateChannelModal';
import { friendApi } from '../../api/friend';
import { dmApi } from '../../api/channel';

const ServerSidebar = () => {
  const { activeServerId, activeChannelId, setActiveChannel } = useServerStore();
  const { data: serverDetails, isLoading, error } = useServerDetails(activeServerId);
  
  // Modal State
  const [isChannelModalOpen, setIsChannelModalOpen] = useState(false);
  const [targetCategoryId, setTargetCategoryId] = useState(null);

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
      <aside className="sidebar">
        <div className="sidebar-panel">
          <header className="sidebar-header">
            {serverDetails?.name || 'Server'}
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

// 하단 유저 프로필 컴포넌트
const UserPanel = () => {
  const { user, logout } = useAuthStore(); // username -> user 객체로 변경
  const queryClient = useQueryClient();

  // 안전하게 username 접근
  const displayedName = user?.username || user?.email || 'Unknown User';

  const handleLogout = () => {
      // 로그아웃 시 확인
      if (window.confirm("Are you sure you want to log out?")) {
          logout();
          queryClient.clear(); // 모든 캐시 데이터 초기화 (다시 로그인 시 꼬이지 않도록)
      }
  };

  return (
    <div className="user-card">
      <div className="user-avatar" />
      <div style={{ flex: 1, overflow: 'hidden' }}>
        <div style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>{displayedName}</div>
        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Online</div>
      </div>
      <div 
        style={{ fontSize: '1.2rem', cursor: 'pointer' }}
        onClick={handleLogout}
        title="Settings / Logout"
      >
        ⚙️
      </div>
    </div>
  );
};

export default ServerSidebar;
