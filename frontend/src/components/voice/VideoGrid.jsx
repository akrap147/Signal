import React, { useEffect, useRef } from 'react';
import useVoiceStore, { ms } from '../../stores/useVoiceStore';

function VideoTile({ track, label, muted = false }) {
  const videoRef = useRef(null);

  useEffect(() => {
    if (videoRef.current) {
      videoRef.current.srcObject = track ? new MediaStream([track]) : null;
    }
    return () => {
      if (videoRef.current) videoRef.current.srcObject = null;
    };
  }, [track]);

  return (
    <div style={{
      position: 'relative',
      background: '#1a1a2e',
      borderRadius: '8px',
      overflow: 'hidden',
      aspectRatio: '16/9',
      border: '1px solid rgba(255,255,255,0.08)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}>
      {track ? (
        <video
          ref={videoRef}
          autoPlay
          playsInline
          muted={muted}
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', opacity: 0.4 }}>
          <svg width="40" height="40" viewBox="0 0 24 24" fill="white">
            <path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z"/>
          </svg>
        </div>
      )}
      <span style={{
        position: 'absolute', bottom: '6px', left: '8px',
        fontSize: '0.68rem', color: 'white',
        background: 'rgba(0,0,0,0.55)', padding: '2px 7px', borderRadius: '4px',
      }}>
        {label}
      </span>
    </div>
  );
}

export default function VideoGrid() {
  const { isVideoEnabled, participants, remoteVideos, activeVoiceChannelId } = useVoiceStore();

  if (!activeVoiceChannelId) return null;

  const remoteUserIds = Object.keys(participants);
  const totalCount = remoteUserIds.length + 1; // +1 for self
  const cols = Math.min(totalCount, 3);

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: `repeat(${cols}, 1fr)`,
      gap: '8px',
      padding: '12px',
      background: '#0e0e1a',
      borderBottom: '1px solid rgba(255,255,255,0.06)',
      flexShrink: 0,
    }}>
      {/* My tile - always shown */}
      <VideoTile
        track={isVideoEnabled && ms.videoStream ? ms.videoStream.getVideoTracks()[0] : null}
        label="나"
        muted={true}
      />
      {/* One tile per remote participant */}
      {remoteUserIds.map(userId => (
        <VideoTile
          key={userId}
          track={remoteVideos[userId] ?? null}
          label={participants[userId]?.username ?? userId}
        />
      ))}
    </div>
  );
}
