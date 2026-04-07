import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import '../App.css'; 
import ServerRail from '../components/server/ServerRail';
import ServerSidebar from '../components/server/ServerSidebar';
import ChatArea from '../components/chat/ChatArea';
import useServerStore from '../stores/useServerStore';
import useChatStore from '../stores/useChatStore';
import useAuthStore from '../stores/useAuthStore';

export default function MainPage() {
  const { serverId, channelId } = useParams();
  const { setActiveServer, setActiveChannel } = useServerStore();
  const { connect, disconnect } = useChatStore();
  const { user } = useAuthStore();

  // WebSocket 연결 (로그인한 사용자)
  useEffect(() => {
    if (user?.id) {
      connect(user.id);
      
      // 컴포넌트 언마운트 시 연결 해제
      return () => disconnect();
    }
  }, [user?.id, connect, disconnect]);

  // URL Params -> Store Sync
  useEffect(() => {
    if (serverId) {
      setActiveServer(serverId); // 'dm' or number
    }
    if (channelId) {
      setActiveChannel(Number(channelId));
    } else {
      setActiveChannel(null);
    }
  }, [serverId, channelId, setActiveServer, setActiveChannel]);

  return (
    <div className="app-container">
      {/* 1. Top Rail: Server List */}
      <ServerRail />

      {/* 2. Main Layout: Sidebar + Chat */}
      <div className="main-layout">
        <ServerSidebar />
        <ChatArea />
      </div>
    </div>
  );
}
