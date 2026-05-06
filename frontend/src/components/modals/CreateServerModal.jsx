import React, { useState } from 'react';
import { useServerActions } from '../../hooks/server/useServerActions';
import Button from '../ui/Button';
import Input from '../ui/Input';

const CreateServerModal = ({ onClose }) => {
  const [serverName, setServerName] = useState('');
  const { createServer, isCreating } = useServerActions();

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!serverName.trim()) return;
    try {
      await createServer(serverName);
      onClose();
    } catch (error) {
      console.error('Failed to create server:', error);
      alert('Failed to create server');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={onClose}>
      <div className="bg-zinc-800 p-6 rounded-lg shadow-xl w-96 text-white" onClick={(e) => e.stopPropagation()}>
        <h2 className="text-xl font-bold mb-6">서버 만들기</h2>
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-zinc-400 mb-1 uppercase">서버 이름</label>
            <Input
              type="text"
              value={serverName}
              onChange={(e) => setServerName(e.target.value)}
              placeholder="나만의 멋진 서버"
              autoFocus
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

export default CreateServerModal;
