import React, { useState } from 'react';
import useServerStore from '../../stores/useServerStore';
import useAuthStore from '../../stores/useAuthStore';
import { useMyServers } from '../../hooks/useServerQueries';
import clsx from 'clsx';
import CreateServerModal from '../modals/CreateServerModal';

const ServerRail = () => {
  const { activeServerId, setActiveServer } = useServerStore();
  const { userId } = useAuthStore();
  const { data: servers, isLoading } = useMyServers(userId);
  const [isModalOpen, setIsModalOpen] = useState(false);

  if (isLoading) return <nav className="top-nav">Loading...</nav>;

  return (
    <>
      <nav className="top-nav">
        <div className="brand">SIGNAL</div>
        <div className="server-tabs">
          {/* DM Tab */}
          <div 
            className={clsx('server-tab', { active: activeServerId === 'dm' })}
            onClick={() => setActiveServer('dm')}
          >
            DM
          </div>

          {/* Server List */}
          {servers?.map((s) => (
            <div 
              key={s.id} 
              className={clsx('server-tab', { active: activeServerId === s.id })}
              onClick={() => setActiveServer(s.id)}
            >
              {s.name}
            </div>
          ))}
          
          {/* Create Server Button */}
          <div 
            className="server-tab create-btn"
            onClick={() => setIsModalOpen(true)}
            style={{ color: 'var(--primary-color)', borderColor: 'var(--primary-color)' }}
          >
            +
          </div>
        </div>
        <div className="user-controls" style={{ color: 'var(--text-muted)' }}>🔍 Search</div>
      </nav>

      {isModalOpen && <CreateServerModal onClose={() => setIsModalOpen(false)} />}
    </>
  );
};

export default ServerRail;
