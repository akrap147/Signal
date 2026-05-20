import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import '../App.css';
import ServerRail from '../components/server/ServerRail';
import ServerSidebar from '../components/server/ServerSidebar';
import ChatArea from '../components/chat/ChatArea';
import useServerStore from '../stores/useServerStore';
import useAuthStore from '../stores/useAuthStore';
import useVoiceStore from '../stores/useVoiceStore';
import VideoGrid from '../components/voice/VideoGrid';

export default function MainPage() {
  const { serverId, channelId } = useParams();
  const { setActiveServer, setActiveChannel } = useServerStore();
  const { user } = useAuthStore();
  const { joinVoiceChannel } = useVoiceStore();

  // 새로고침 후 음성 채널 자동 재연결
  useEffect(() => {
    if (!user) return;
    const saved = sessionStorage.getItem('voiceChannelId');
    if (saved) joinVoiceChannel(Number(saved));
  }, [user]);

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
        <div style={{ display: 'flex', flexDirection: 'column', flex: 1, minWidth: 0, overflow: 'hidden' }}>
          <VideoGrid />
          <ChatArea />
        </div>
      </div>
    </div>
  );
}
