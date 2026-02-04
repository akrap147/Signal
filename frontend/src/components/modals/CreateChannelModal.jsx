import React, { useState } from 'react';
import useServerStore from '../../stores/useServerStore';
import { useServerDetails } from '../../hooks/useServerQueries';
import { useChannelActions } from '../../hooks/channel/useChannelActions';
import Button from '../ui/Button';
import Input from '../ui/Input';

const CreateChannelModal = ({ onClose, selectedCategoryId }) => {
  const { activeServerId } = useServerStore();
  const { data: serverDetails } = useServerDetails(activeServerId);
  const { createChannel, isCreating } = useChannelActions();
  
  const [name, setName] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    
    // Props로 받은 ID가 있으면 그거 쓰고, 실수로 안 보냈으면 첫 번째 카테고리 fallback
    const targetCategoryId = selectedCategoryId || serverDetails?.categories?.[0]?.id;
    
    if (!targetCategoryId) {
        alert("No category found to add channel.");
        return;
    }

    try {
      await createChannel({ 
          serverId: activeServerId,
          categoryId: targetCategoryId,
          name, 
          type: 'TEXT' 
      });
      onClose();
    } catch (error) {
      console.error(error);
      alert('Failed to create channel');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={onClose}>
      <div className="bg-zinc-800 p-6 rounded-lg shadow-xl w-96 text-white" onClick={e => e.stopPropagation()}>
        <h2 className="text-xl font-bold mb-2">채널 만들기</h2>
        <p className="text-sm text-zinc-400 mb-6">in {serverDetails?.name || 'Server'}</p>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-zinc-400 mb-1 uppercase">채널 이름</label>
            <Input 
              autoFocus
              type="text" 
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="새로운 채널"
            />
          </div>
          
          <div className="flex justify-end gap-2 mt-6">
            <Button type="button" onClick={onClose} className="bg-transparent hover:bg-zinc-700 text-zinc-300">취소</Button>
            <Button type="submit" disabled={isCreating}>만들기</Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateChannelModal;
