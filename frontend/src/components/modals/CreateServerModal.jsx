import React, { useState } from 'react';
import { useServerActions } from '../../hooks/server/useServerActions';
import Button from '../ui/Button';
import Input from '../ui/Input';

const CreateServerModal = ({ onClose }) => {
  const [mode, setMode] = useState('create'); // 'create' or 'join'
  const [serverName, setServerName] = useState('');
  const [inviteCode, setInviteCode] = useState('');
  
  const { createServer, joinServer, isCreating, isJoining } = useServerActions();

  // 서버 생성
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

  // 서버 참가
  const handleJoin = async (e) => {
    e.preventDefault();
    if (!inviteCode.trim()) return;

    try {
      await joinServer(inviteCode);
      onClose();
    } catch (error) {
      console.error('Failed to join server:', error);
      alert('참가 실패. 초대 코드를 확인해주세요.');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={onClose}>
      <div className="bg-zinc-800 p-6 rounded-lg shadow-xl w-96 text-white" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold">{mode === 'create' ? '서버 만들기' : '서버 참가하기'}</h2>
          
          <div className="flex bg-zinc-900 rounded p-1">
            <button 
                className={`px-3 py-1 text-sm rounded ${mode === 'create' ? 'bg-zinc-700' : 'hover:bg-zinc-800'}`} 
                onClick={() => setMode('create')}>Create</button>
            <button 
                className={`px-3 py-1 text-sm rounded ${mode === 'join' ? 'bg-zinc-700' : 'hover:bg-zinc-800'}`} 
                onClick={() => setMode('join')}>Join</button>
          </div>
        </div>

        {mode === 'create' ? (
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
        ) : (
          <form onSubmit={handleJoin} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-zinc-400 mb-1 uppercase">초대 코드</label>
              <Input 
                type="text" 
                value={inviteCode}
                onChange={(e) => setInviteCode(e.target.value)}
                placeholder="초대 코드 입력 (예: AbC123XyZ)"
                autoFocus
              />
            </div>
            <div className="flex justify-end gap-2 mt-6">
              <Button type="button" onClick={onClose} className="bg-transparent hover:bg-zinc-700 text-zinc-300">취소</Button>
              <Button type="submit" disabled={isJoining}>참가하기</Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default CreateServerModal;
