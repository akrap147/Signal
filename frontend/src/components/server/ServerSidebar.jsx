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

  // DM / Friends View
  if (activeServerId === 'dm' || activeServerId === '@me') {
    // 추후 API 연동 시 이 배열들을 채우게 됨
    const friends = []; 
    const directMessages = []; 

    return (
      <aside className="sidebar">
        <div className="sidebar-panel">
          <header className="sidebar-header">
             <button 
               className="w-full text-left bg-zinc-900 text-zinc-400 text-sm px-2 py-1 rounded"
               onClick={() => alert('친구 검색 기능 구현 예정')}
             >
               Find or start a conversation
             </button>
          </header>
          <div className="sidebar-list">
             {/* Friends Tab */}
             <div className={clsx('sidebar-item active')}>
                <span className="mr-2">👋</span> Friends
             </div>
             
             {/* DM Header */}
             <div className="flex justify-between items-center mt-4 px-2 mb-1">
                <span className="text-xs font-bold text-zinc-400 uppercase">Direct Messages</span>
                {/* DM 생성 버튼 */}
                <span 
                  className="cursor-pointer text-zinc-400 hover:text-white"
                  onClick={() => alert('DM 생성 기능 구현 예정')}
                >
                  +
                </span>
             </div>

             {/* DM List */}
             {directMessages.length > 0 ? (
               directMessages.map(dm => (
                 <div key={dm.id} className="sidebar-item">
                   {/* DM Item UI */}
                   {dm.name}
                 </div>
               ))
             ) : (
               <div className="px-3 py-2 text-zinc-500 text-sm italic">
                 친구를 추가하고 대화를 시작해보세요!
               </div>
             )}
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
