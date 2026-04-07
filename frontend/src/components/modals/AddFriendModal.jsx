import React, { useState } from 'react';
import useFriendStore from '../../stores/useFriendStore';
import Button from '../ui/Button';
import Input from '../ui/Input';
import './Modal.css';

export default function AddFriendModal({ onClose }) {
  const [friendEmail, setFriendEmail] = useState('');
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const { sendRequest } = useFriendStore();

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!friendEmail.trim()) {
      setMessage({ type: 'error', text: '친구 이메일을 입력해주세요.' });
      return;
    }

    // 간단한 이메일 형식 검증
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(friendEmail)) {
      setMessage({ type: 'error', text: '유효한 이메일 주소를 입력해주세요.' });
      return;
    }

    setIsLoading(true);
    const result = await sendRequest(friendEmail);
    setIsLoading(false);

    if (result.success) {
      setMessage({ type: 'success', text: '친구 요청을 보냈습니다!' });
      setTimeout(() => {
        onClose();
      }, 1500);
    } else {
      setMessage({ type: 'error', text: result.error || '친구 요청 전송에 실패했습니다.' });
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>친구 추가</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <p style={{ color: 'var(--text-muted)', marginBottom: '16px', fontSize: '0.9rem' }}>
              친구의 이메일 주소를 입력하여 친구 요청을 보낼 수 있습니다.
            </p>

            <Input
              type="email"
              placeholder="친구 이메일 (예: friend@example.com)"
              value={friendEmail}
              onChange={(e) => setFriendEmail(e.target.value)}
              autoFocus
            />

            {message && (
              <div
                style={{
                  marginTop: '12px',
                  padding: '10px',
                  borderRadius: '4px',
                  fontSize: '0.9rem',
                  backgroundColor: message.type === 'error' ? 'rgba(220, 38, 38, 0.1)' : 'rgba(34, 197, 94, 0.1)',
                  color: message.type === 'error' ? '#ef4444' : '#22c55e',
                  border: `1px solid ${message.type === 'error' ? '#ef4444' : '#22c55e'}`,
                }}
              >
                {message.text}
              </div>
            )}
          </div>

          <div className="modal-footer">
            <Button variant="secondary" onClick={onClose} disabled={isLoading}>
              취소
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? '전송 중...' : '친구 요청'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
