import client from './client';

/**
 * 친구 요청 보내기
 * @param {string} friendEmail - 친구 요청을 보낼 사용자 Email
 */
export const sendFriendRequest = async (friendEmail) => {
  const response = await client.post('/friends/request', { friendEmail });
  return response.data;
};

/**
 * 친구 요청 수락
 * @param {number} requesterId - 요청을 보낸 사용자 ID
 */
export const acceptFriendRequest = async (requesterId) => {
  const response = await client.post(`/friends/accept/${requesterId}`);
  return response.data;
};

/**
 * 친구 요청 거절 또는 친구 삭제
 * @param {number} friendId - 친구 ID
 */
export const removeFriendship = async (friendId) => {
  const response = await client.delete(`/friends/${friendId}`);
  return response.data;
};

/**
 * 내 친구 목록 조회 (accepted 상태)
 */
export const getMyFriends = async () => {
  const response = await client.get('/friends');
  return response.data;
};

/**
 * 내가 보낸 친구 요청 목록 조회 (pending 상태)
 */
export const getMySentRequests = async () => {
  const response = await client.get('/friends/sent-requests');
  return response.data;
};

/**
 * 내가 받은 친구 요청 목록 조회 (pending 상태)
 */
export const getMyReceivedRequests = async () => {
  const response = await client.get('/friends/received-requests');
  return response.data;
};
