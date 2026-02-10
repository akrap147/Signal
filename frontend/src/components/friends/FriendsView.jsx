import React, { useEffect, useState } from 'react';
import useFriendStore from '../../stores/useFriendStore';
import useAuthStore from '../../stores/useAuthStore';
import useServerStore from '../../stores/useServerStore';
import AddFriendModal from '../modals/AddFriendModal';
import './FriendsView.css';

export default function FriendsView() {
  const {
    friends,
    receivedRequests,
    sentRequests,
    isLoading,
    loadAllFriendData,
    acceptRequest,
    removeFriend,
  } = useFriendStore();
  
  const { user } = useAuthStore();
  const { setActiveServer, setActiveChannel } = useServerStore();

  const [activeTab, setActiveTab] = useState('all');
  const [isAddFriendModalOpen, setIsAddFriendModalOpen] = useState(false);

  useEffect(() => {
    loadAllFriendData();
  }, [loadAllFriendData]);

  // DM 대화창 열기
  const handleOpenDM = (friendId) => {
    if (!user?.id) {
      alert('로그인이 필요합니다.');
      return;
    }
    
    // DM roomId 생성: 두 사용자 ID를 정렬해서 조합
    // 예: userId=5, friendId=3 → roomId="3_5"
    const myId = user.id;
    const roomId = myId < friendId ? `${myId}_${friendId}` : `${friendId}_${myId}`;
    
    // DM 모드로 전환
    setActiveServer('dm');
    setActiveChannel(roomId);
  };

  const handleAccept = async (requesterId) => {
    const result = await acceptRequest(requesterId);
    if (result.success) {
      // Success feedback could be added here
    }
  };

  const handleRemove = async (friendId, friendName) => {
    if (window.confirm(`${friendName}님을 친구 목록에서 삭제하시겠습니까?`)) {
      await removeFriend(friendId);
    }
  };

  const handleReject = async (requesterId, requesterName) => {
    if (window.confirm(`${requesterName}님의 친구 요청을 거절하시겠습니까?`)) {
      await removeFriend(requesterId);
    }
  };

  const handleCancelRequest = async (friendId, friendName) => {
    if (window.confirm(`${friendName}님에게 보낸 친구 요청을 취소하시겠습니까?`)) {
      await removeFriend(friendId);
    }
  };

  const tabs = [
    { id: 'all', label: '전체', count: friends.length },
    { id: 'pending', label: '대기중', count: receivedRequests.length },
    { id: 'sent', label: '보낸 요청', count: sentRequests.length },
  ];

  return (
    <div className="friends-view">
      {/* Header */}
      <div className="friends-header">
        <div className="friends-tabs">
          <div className="friends-icon">👋</div>
          <h2>친구</h2>
          <div className="friends-tab-list">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                className={`friends-tab ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
                {tab.count > 0 && <span className="tab-badge">{tab.count}</span>}
              </button>
            ))}
          </div>
        </div>
        <button className="add-friend-btn" onClick={() => setIsAddFriendModalOpen(true)}>
          친구 추가
        </button>
      </div>

      {/* Content */}
      <div className="friends-content">
        {isLoading ? (
          <div className="friends-loading">로딩 중...</div>
        ) : (
          <>
            {/* 전체 친구 */}
            {activeTab === 'all' && (
              <div className="friends-list">
                <h3 className="list-title">전체 친구 — {friends.length}</h3>
                {friends.length === 0 ? (
                  <div className="empty-state">
                    <p>아직 친구가 없습니다.</p>
                    <p className="empty-hint">친구 추가 버튼을 눌러 친구를 추가해보세요!</p>
                  </div>
                ) : (
                  friends.map((friendship) => (
                    <FriendCard
                      key={friendship.id}
                      user={friendship.friendInfo}
                      actions={[
                        {
                          label: '메시지',
                          icon: '💬',
                          onClick: () => handleOpenDM(friendship.friendId),
                        },
                        {
                          label: '삭제',
                          icon: '🗑️',
                          onClick: () => handleRemove(friendship.friendId, friendship.friendInfo.username),
                          variant: 'danger',
                        },
                      ]}
                    />
                  ))
                )}
              </div>
            )}

            {/* 받은 친구 요청 */}
            {activeTab === 'pending' && (
              <div className="friends-list">
                <h3 className="list-title">받은 요청 — {receivedRequests.length}</h3>
                {receivedRequests.length === 0 ? (
                  <div className="empty-state">
                    <p>받은 친구 요청이 없습니다.</p>
                  </div>
                ) : (
                  receivedRequests.map((request) => (
                    <FriendCard
                      key={request.id}
                      user={request.friendInfo}
                      actions={[
                        {
                          label: '수락',
                          icon: '✓',
                          onClick: () => handleAccept(request.userId),
                          variant: 'success',
                        },
                        {
                          label: '거절',
                          icon: '✕',
                          onClick: () => handleReject(request.userId, request.friendInfo.username),
                          variant: 'danger',
                        },
                      ]}
                    />
                  ))
                )}
              </div>
            )}

            {/* 보낸 친구 요청 */}
            {activeTab === 'sent' && (
              <div className="friends-list">
                <h3 className="list-title">보낸 요청 — {sentRequests.length}</h3>
                {sentRequests.length === 0 ? (
                  <div className="empty-state">
                    <p>보낸 친구 요청이 없습니다.</p>
                  </div>
                ) : (
                  sentRequests.map((request) => (
                    <FriendCard
                      key={request.id}
                      user={request.friendInfo}
                      status="pending"
                      actions={[
                        {
                          label: '취소',
                          icon: '✕',
                          onClick: () => handleCancelRequest(request.friendId, request.friendInfo.username),
                          variant: 'secondary',
                        },
                      ]}
                    />
                  ))
                )}
              </div>
            )}
          </>
        )}
      </div>

      {/* Add Friend Modal */}
      {isAddFriendModalOpen && (
        <AddFriendModal onClose={() => setIsAddFriendModalOpen(false)} />
      )}
    </div>
  );
}

// Friend Card Component
function FriendCard({ user, status, actions }) {
  return (
    <div className="friend-card">
      <div className="friend-info">
        <div className="friend-avatar">
          {user.profileImage ? (
            <img src={user.profileImage} alt={user.username} />
          ) : (
            <div className="avatar-placeholder">{user.username?.charAt(0).toUpperCase()}</div>
          )}
        </div>
        <div className="friend-details">
          <div className="friend-name">{user.username}</div>
          <div className="friend-status">
            {status === 'pending' ? '대기중' : '온라인'}
          </div>
        </div>
      </div>
      <div className="friend-actions">
        {actions.map((action, index) => (
          <button
            key={index}
            className={`action-btn ${action.variant || ''}`}
            onClick={action.onClick}
            title={action.label}
          >
            <span className="action-icon">{action.icon}</span>
            <span className="action-label">{action.label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
