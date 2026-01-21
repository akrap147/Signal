import './App.css'
import ServerRail from './components/server/ServerRail'
import ServerSidebar from './components/server/ServerSidebar'
import ChatArea from './components/chat/ChatArea'
import LandingPage from './pages/LandingPage'
import useAuthStore from './stores/useAuthStore'

function App() {
  const { isAuthenticated } = useAuthStore()

  if (!isAuthenticated) {
    return <LandingPage />
  }

  return (
    <div className="app-container">
      {/* 1. Left Rail: Server List */}
      <ServerRail />

      {/* 2. Main Layout: Sidebar + Chat */}
      <div className="main-layout">
        <ServerSidebar />
        <ChatArea />
      </div>
    </div>
  )
}

export default App
