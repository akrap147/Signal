import { useState } from 'react'
import './App.css'

function App() {
  const [activeTab, setActiveTab] = useState('server-1')
  const [activeChannel, setActiveChannel] = useState(1)

  const servers = [
    { id: 'dm', name: 'Direct Messages', short: 'DM' },
    { id: 'server-1', name: 'Signal HQ', short: 'HQ' },
    { id: 'server-2', name: 'Product Team', short: 'PT' },
    { id: 'server-3', name: 'Community', short: 'CM' },
  ]

  const currentChannels = [
    { id: 1, name: 'General', type: 'text' },
    { id: 2, name: 'Project-A', type: 'text' },
    { id: 3, name: 'Resource-Box', type: 'text' },
  ]

  const mockMessages = [
    { id: 1, user: 'Alex', text: 'Hey, look at this new layout. It feels way more like a workspace tool than a chat app.', mine: false, time: '14:20' },
    { id: 2, user: 'You', text: 'Yeah, the top navigation for servers makes it much cleaner.', mine: true, time: '14:21' },
    { id: 3, user: 'Sarah', text: 'I love the floating canvas idea. It really separates the zones.', mine: false, time: '14:22' },
  ]

  return (
    <div className="app-container">
      {/* 1. Top Navigation: Server Tabs */}
      <nav className="top-nav">
        <div className="brand">SIGNAL</div>
        <div className="server-tabs">
          {servers.map(s => (
            <div 
              key={s.id} 
              className={`server-tab ${activeTab === s.id ? 'active' : ''}`}
              onClick={() => setActiveTab(s.id)}
            >
              {s.name}
            </div>
          ))}
        </div>
        <div className="user-controls" style={{ color: 'var(--text-muted)' }}>🔍 Search</div>
      </nav>

      {/* 2. Main Layout Area */}
      <div className="main-layout">
        
        {/* 3. Sidebar: Channel Navigation */}
        <aside className="sidebar">
          <div className="sidebar-panel">
            <header className="sidebar-header">Navigation</header>
            <div className="sidebar-list">
              {currentChannels.map(ch => (
                <div 
                  key={ch.id} 
                  className={`sidebar-item ${activeChannel === ch.id ? 'active' : ''}`}
                  onClick={() => setActiveChannel(ch.id)}
                >
                  <span style={{ opacity: 0.5 }}>#</span> {ch.name}
                </div>
              ))}
            </div>
          </div>

          <div className="user-card">
            <div className="user-avatar" />
            <div style={{ flex: 1, overflow: 'hidden' }}>
              <div style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Jongwon Park</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Online</div>
            </div>
            <div style={{ fontSize: '1.2rem', cursor: 'pointer' }}>⚙️</div>
          </div>
        </aside>

        {/* 4. Chat Workspace: The central canvas */}
        <main className="chat-workspace">
          <header className="workspace-header">
            <div style={{ fontSize: '1.1rem', fontWeight: '700' }}>
              # {currentChannels.find(c => c.id === activeChannel)?.name}
            </div>
            <div style={{ color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer' }}>...</div>
          </header>

          <div className="messages-container">
            {mockMessages.map(msg => (
              <div key={msg.id} className={`message-bubble ${msg.mine ? 'mine' : ''}`}>
                <div className="message-info">
                  <strong>{msg.user}</strong> <span>{msg.time}</span>
                </div>
                <div className="message-text">
                  {msg.text}
                </div>
              </div>
            ))}
          </div>

          <footer className="input-section">
            <div className="input-box">
              <span style={{ fontSize: '1.2rem', cursor: 'pointer' }}>⊕</span>
              <input type="text" placeholder="Type a message or command..." />
              <span style={{ cursor: 'pointer' }}>�</span>
            </div>
          </footer>
        </main>

      </div>
    </div>
  )
}

export default App
