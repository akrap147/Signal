import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import '../App.css'; 
import ServerRail from '../components/server/ServerRail';
import ServerSidebar from '../components/server/ServerSidebar';
import ChatArea from '../components/chat/ChatArea';
import useServerStore from '../stores/useServerStore';

export default function MainPage() {
  const { serverId, channelId } = useParams();
  const { setActiveServer, setActiveChannel } = useServerStore();

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
