import React, { useState } from 'react';
import './Modal.css';
import useAuthStore from '../../stores/useAuthStore';
import { useQueryClient } from '@tanstack/react-query';
import apiClient from '../../api/client';

const CreateServerModal = ({ onClose }) => {
  const [mode, setMode] = useState('create'); // 'create' or 'join'
  const [serverName, setServerName] = useState('');
  const [inviteCode, setInviteCode] = useState('');
  
  const { userId } = useAuthStore();
  const queryClient = useQueryClient();

  // 서버 생성
  const handleCreate = async (e) => {
    e.preventDefault();
    if (!serverName.trim()) return;

    try {
      await apiClient.post('/servers', {
        name: serverName,
        ownerId: userId // TODO: 백엔드에서 토큰으로 처리하면 제거 가능
      });
      // 성공 시 캐시 무효화 (목록 갱신) & 닫기
      queryClient.invalidateQueries(['myServers']);
      onClose();
    } catch (error) {
      console.error('Failed to create server:', error);
      alert('Failed to create server');
    }
  };

  // 서버 참가
  const handleJoin = async (e) => {
    e.preventDefault();
    if (!inviteCode.trim()) return;

    try {
      await apiClient.post('/servers/join', {
        inviteCode: inviteCode,
        userId: userId
      });
      queryClient.invalidateQueries(['myServers']);
      onClose();
    } catch (error) {
      console.error('Failed to join server:', error);
      alert('Failed to join server. Check your invite code.');
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{mode === 'create' ? 'Create a Server' : 'Join a Server'}</h2>
          <div className="mode-toggle">
            <button 
                className={mode === 'create' ? 'active' : ''} 
                onClick={() => setMode('create')}>Create</button>
            <button 
                className={mode === 'join' ? 'active' : ''} 
                onClick={() => setMode('join')}>Join</button>
          </div>
        </div>

        {mode === 'create' ? (
          <form onSubmit={handleCreate}>
            <div className="form-group">
              <label>Server Name</label>
              <input 
                type="text" 
                value={serverName}
                onChange={(e) => setServerName(e.target.value)}
                placeholder="My Awesome Server"
                autoFocus
              />
            </div>
            <div className="modal-actions">
              <button type="button" className="btn-cancel" onClick={onClose}>Cancel</button>
              <button type="submit" className="btn-create">Create</button>
            </div>
          </form>
        ) : (
          <form onSubmit={handleJoin}>
            <div className="form-group">
              <label>Invite Code</label>
              <input 
                type="text" 
                value={inviteCode}
                onChange={(e) => setInviteCode(e.target.value)}
                placeholder="Enter invite code (e.g. AbC123XyZ)"
                autoFocus
              />
            </div>
            <div className="modal-actions">
              <button type="button" className="btn-cancel" onClick={onClose}>Cancel</button>
              <button type="submit" className="btn-create">Join Server</button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default CreateServerModal;
