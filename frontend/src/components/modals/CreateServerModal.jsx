import React, { useState } from 'react';
import client from '../../api/client';
import useAuthStore from '../../stores/useAuthStore';
import { useQueryClient } from '@tanstack/react-query';
import './Modal.css';

const CreateServerModal = ({ onClose }) => {
  const { userId } = useAuthStore();
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    
    setIsLoading(true);
    try {
      await client.post('/servers', { name, ownerId: userId });
      // 성공 시 목록 갱신
      await queryClient.invalidateQueries(['myServers']);
      onClose();
    } catch (error) {
      console.error(error);
      alert('Failed to create server');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <h2 className="modal-title">Create a Server</h2>
        <p className="modal-desc">Give your new server a personality with a name and an icon.</p>
        
        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Server Name</label>
            <input 
              autoFocus
              type="text" 
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="My Awesome Server"
            />
          </div>
          
          <div className="modal-actions">
            <button type="button" className="btn-cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-create" disabled={isLoading}>
              {isLoading ? 'Creating...' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateServerModal;
