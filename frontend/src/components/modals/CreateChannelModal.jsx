import React, { useState } from 'react';
import client from '../../api/client';
import useServerStore from '../../stores/useServerStore';
import { useServerDetails } from '../../hooks/useServerQueries';
import { useQueryClient } from '@tanstack/react-query';
import './Modal.css';

const CreateChannelModal = ({ onClose, selectedCategoryId }) => {
  const { activeServerId } = useServerStore();
  const { data: serverDetails } = useServerDetails(activeServerId);
  const queryClient = useQueryClient();
  
  const [name, setName] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    
    // Props로 받은 ID가 있으면 그거 쓰고, 실수로 안 보냈으면 첫 번째 카테고리 fallback
    const targetCategoryId = selectedCategoryId || serverDetails?.categories?.[0]?.id;
    
    if (!targetCategoryId) {
        alert("No category found to add channel.");
        return;
    }

    setIsLoading(true);
    try {
      await client.post('/channels', { 
          serverId: activeServerId,
          categoryId: targetCategoryId,
          name, 
          type: 'TEXT' 
      });
      // 성공 시 서버 상세 정보 갱신
      await queryClient.invalidateQueries(['serverDetails', activeServerId]);
      onClose();
    } catch (error) {
      console.error(error);
      alert('Failed to create channel');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <h2 className="modal-title">Create a Channel</h2>
        <p className="modal-desc">in {serverDetails?.name || 'Server'}</p>
        
        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Channel Name</label>
            <input 
              autoFocus
              type="text" 
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="new-channel"
            />
          </div>
          
          <div className="modal-actions">
            <button type="button" className="btn-cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-create" disabled={isLoading}>
              {isLoading ? 'Creating...' : 'Create Channel'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateChannelModal;
