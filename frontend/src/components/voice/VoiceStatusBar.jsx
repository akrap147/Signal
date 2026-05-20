import React, { useEffect, useRef, useState } from 'react';
import useVoiceStore, { ms } from '../../stores/useVoiceStore';
import useServerStore from '../../stores/useServerStore';
import { useServerDetails } from '../../hooks/useServerQueries';

const SEGMENTS = 16;

function VolumeSegments({ analyser, color }) {
  const segRefs = useRef([]);

  useEffect(() => {
    if (!analyser) return;

    const data = new Uint8Array(analyser.frequencyBinCount);
    let raf;

    const tick = () => {
      raf = requestAnimationFrame(tick);
      analyser.getByteFrequencyData(data);
      const avg = data.reduce((a, b) => a + b, 0) / data.length;
      const level = Math.min(Math.round((avg / 80) * SEGMENTS), SEGMENTS);
      segRefs.current.forEach((el, i) => {
        if (!el) return;
        el.style.background = i < level ? color : 'rgba(255,255,255,0.08)';
        el.style.transform = i < level ? 'scaleY(1)' : 'scaleY(0.5)';
      });
    };

    tick();
    return () => cancelAnimationFrame(raf);
  }, [analyser, color]);

  return (
    <div style={{ display: 'flex', gap: '2px', alignItems: 'center', height: '16px' }}>
      {Array.from({ length: SEGMENTS }).map((_, i) => (
        <div
          key={i}
          ref={(el) => (segRefs.current[i] = el)}
          style={{
            width: '5px',
            height: '100%',
            borderRadius: '2px',
            background: 'rgba(255,255,255,0.08)',
            transition: 'background 0.05s, transform 0.05s',
            transformOrigin: 'bottom',
          }}
        />
      ))}
    </div>
  );
}

const VoiceStatusBar = () => {
  const { activeVoiceChannelId, isMuted, isConnected, isVideoEnabled, leaveVoiceChannel, toggleMute, toggleVideo } = useVoiceStore();
  const { activeServerId } = useServerStore();
  const { data: serverDetails } = useServerDetails(activeServerId);

  const [inputAnalyser, setInputAnalyser] = useState(null);
  const [outputAnalyser, setOutputAnalyser] = useState(null);
  const audioCtxRef = useRef(null);

  const channelName = serverDetails?.categories
    ?.flatMap((c) => c.channels)
    ?.find((ch) => ch.id === activeVoiceChannelId)
    ?.name ?? '음성 채널';

  useEffect(() => {
    if (!isConnected) return;

    const timer = setTimeout(async () => {
      const ctx = new AudioContext();
      await ctx.resume();
      audioCtxRef.current = ctx;

      if (ms.inputStream) {
        const src = ctx.createMediaStreamSource(ms.inputStream);
        const analyser = ctx.createAnalyser();
        analyser.fftSize = 256;
        src.connect(analyser);
        setInputAnalyser(analyser);
      }

      // Poll for output tracks (added when others join)
      const poll = setInterval(() => {
        if (ms.outputTracks.length > 0 && !outputAnalyser) {
          const stream = new MediaStream([...ms.outputTracks]);
          const src = ctx.createMediaStreamSource(stream);
          const analyser = ctx.createAnalyser();
          analyser.fftSize = 256;
          src.connect(analyser);
          setOutputAnalyser(analyser);
        }
      }, 1000);

      return () => clearInterval(poll);
    }, 600);

    return () => {
      clearTimeout(timer);
      setInputAnalyser(null);
      setOutputAnalyser(null);
      if (audioCtxRef.current) {
        audioCtxRef.current.close();
        audioCtxRef.current = null;
      }
    };
  }, [isConnected]);

  if (!activeVoiceChannelId) return null;

  return (
    <div style={{
      padding: '10px 12px',
      background: '#111118',
      borderTop: '1px solid rgba(255,255,255,0.06)',
    }}>
      {/* Connection status */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px' }}>
        <span style={{
          width: '7px', height: '7px', borderRadius: '50%',
          background: isConnected ? '#3ba55d' : '#faa61a',
          boxShadow: isConnected ? '0 0 4px #3ba55d' : 'none',
          flexShrink: 0,
        }} />
        <span style={{ fontSize: '0.72rem', color: isConnected ? '#3ba55d' : '#faa61a', fontWeight: 700 }}>
          {isConnected ? '음성 연결됨' : '연결 중...'}
        </span>
        <span style={{ marginLeft: 'auto', fontSize: '0.72rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          🔊 {channelName}
        </span>
      </div>

      {/* Input meter */}
      <div style={{ marginBottom: '8px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
          <span style={{ fontSize: '0.68rem', color: isMuted ? '#ed4245' : '#5865f2', fontWeight: 700, width: '36px' }}>
            {isMuted ? '🔇 MIC' : '🎤 MIC'}
          </span>
          {isMuted
            ? <span style={{ fontSize: '0.65rem', color: '#ed4245' }}>음소거됨</span>
            : <VolumeSegments analyser={inputAnalyser} color="#5865f2" />
          }
        </div>
      </div>

      {/* Output meter */}
      <div style={{ marginBottom: '10px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span style={{ fontSize: '0.68rem', color: '#3ba55d', fontWeight: 700, width: '36px' }}>🔈 OUT</span>
          <VolumeSegments analyser={outputAnalyser} color="#3ba55d" />
        </div>
      </div>

      {/* Controls */}
      <div style={{ display: 'flex', gap: '6px' }}>
        <button
          onClick={toggleMute}
          style={{
            flex: 1, padding: '5px 0', fontSize: '0.72rem', fontWeight: 700,
            borderRadius: '4px', border: 'none', cursor: 'pointer',
            background: isMuted ? '#ed4245' : '#1e1e2e',
            color: isMuted ? 'white' : 'var(--text-secondary)',
            transition: 'background 0.15s',
          }}
        >
          {isMuted ? '🔇 해제' : '🎤 음소거'}
        </button>
        <button
          onClick={toggleVideo}
          style={{
            flex: 1, padding: '5px 0', fontSize: '0.72rem', fontWeight: 700,
            borderRadius: '4px', border: 'none', cursor: 'pointer',
            background: isVideoEnabled ? '#5865f2' : '#1e1e2e',
            color: isVideoEnabled ? 'white' : 'var(--text-secondary)',
            transition: 'background 0.15s',
          }}
        >
          {isVideoEnabled ? '📹 카메라 끄기' : '📷 카메라'}
        </button>
        <button
          onClick={leaveVoiceChannel}
          style={{
            padding: '5px 12px', fontSize: '0.72rem', fontWeight: 700,
            borderRadius: '4px', border: 'none', cursor: 'pointer',
            background: '#1e1e2e', color: '#ed4245',
          }}
        >
          ✕ 나가기
        </button>
      </div>
    </div>
  );
};

export default VoiceStatusBar;
