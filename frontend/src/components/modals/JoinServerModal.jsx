import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { serverApi } from '../../api/server';
import { useQueryClient } from '@tanstack/react-query';
import useServerStore from '../../stores/useServerStore';

const JoinServerModal = ({ onClose }) => {
  const [code, setCode] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { setActiveServer } = useServerStore();

  const handleJoin = async () => {
    if (!code.trim()) return;
    setLoading(true);
    setError(null);
    try {
      const { serverId } = await serverApi.joinServer(code.trim());
      await queryClient.invalidateQueries({ queryKey: ['myServers'] });
      setActiveServer(serverId);
      navigate(`/channels/${serverId}`);
      onClose();
    } catch {
      setError('유효하지 않은 초대 코드입니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
      <div style={{ background: '#36393f', borderRadius: '8px', padding: '32px', width: '440px' }}>
        <h2 style={{ margin: '0 0 8px', fontSize: '1.4rem' }}>서버 참여하기</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '20px', fontSize: '0.9rem' }}>초대 코드를 입력하여 서버에 참여하세요.</p>

        <label style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>초대 코드</label>
        <input
          type="text"
          placeholder="예: a1b2c3d4"
          value={code}
          onChange={(e) => setCode(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleJoin()}
          style={{ display: 'block', width: '100%', marginTop: '8px', padding: '10px 12px', borderRadius: '4px', border: '1px solid #4f545c', background: '#40444b', color: 'white', fontSize: '1rem', boxSizing: 'border-box' }}
          autoFocus
        />
        {error && <p style={{ color: '#ed4245', fontSize: '0.85rem', marginTop: '8px' }}>{error}</p>}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '24px' }}>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', padding: '10px 16px', borderRadius: '4px' }}>
            취소
          </button>
          <button
            onClick={handleJoin}
            disabled={loading || !code.trim()}
            style={{ background: '#5865f2', border: 'none', color: 'white', cursor: 'pointer', padding: '10px 20px', borderRadius: '4px', fontWeight: 600, opacity: loading || !code.trim() ? 0.6 : 1 }}
          >
            {loading ? '참여 중...' : '참여하기'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default JoinServerModal;
