import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import useServerStore from '../../stores/useServerStore';
import useAuthStore from '../../stores/useAuthStore';
import { useMyServers } from '../../hooks/useServerQueries';
import clsx from 'clsx';
import CreateServerModal from '../modals/CreateServerModal';

const ServerRail = () => {
  const { activeServerId } = useServerStore(); // Store는 읽기 전용으로만 사용 (UI Highlight용)
  const { user } = useAuthStore();
  const userId = user?.id;
  const { data: servers, isLoading } = useMyServers(userId);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const navigate = useNavigate();

  if (isLoading) return <nav className="top-nav">Loading...</nav>;

  return (
    <>
      <nav className="top-nav">
        <div className="brand" onClick={() => navigate('/channels/@me')}>SIGNAL</div>
        <div className="server-tabs">
          {/* DM Tab */}
          <div 
            className={clsx('server-tab', { active: activeServerId === 'dm' || activeServerId === '@me' })}
            onClick={() => navigate('/channels/@me')}
          >
            DM
          </div>

          {/* Server List */}
          {servers?.map((s) => (
            <div 
              key={s.id} 
              className={clsx('server-tab', { active: String(activeServerId) === String(s.id) })}
              onClick={() => navigate(`/channels/${s.id}`)}
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
