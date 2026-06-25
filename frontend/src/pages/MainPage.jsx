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

  useEffect(() => {
    if (serverId) setActiveServer(serverId);
    if (channelId) {
      setActiveChannel(Number(channelId));
    } else {
      setActiveChannel(null);
    }
  }, [serverId, channelId, setActiveServer, setActiveChannel]);

  return (
    <div className="app-container">
      <ServerRail />
      <div className="main-layout">
        <ServerSidebar />
        <ChatArea />
      </div>
    </div>
  );
}
