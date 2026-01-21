import React, { useEffect, useState } from 'react';
import useServerStore from '../../stores/useServerStore';
import useAuthStore from '../../stores/useAuthStore'; // Add Import
import { useServerDetails } from '../../hooks/useServerQueries';
import { useQueryClient } from '@tanstack/react-query'; // Add Import
import clsx from 'clsx';
import CreateChannelModal from '../modals/CreateChannelModal';

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

  if (activeServerId === 'dm') {
    return (
      <aside className="sidebar">
        <div className="sidebar-panel">
          <header className="sidebar-header">Direct Messages</header>
          <div className="sidebar-list">
             <div style={{ padding: '20px', color: 'var(--text-muted)' }}>Work in Progress...</div>
          </div>
        </div>
        <UserPanel />
      </aside>
    );
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

// 하단 유저 프로필 컴포넌트
const UserPanel = () => {
  const { username, logout } = useAuthStore();
  const queryClient = useQueryClient();

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
        <div style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>{username || 'Unknown User'}</div>
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
