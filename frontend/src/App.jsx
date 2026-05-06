import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import MainPage from './pages/MainPage';
import ProtectedRoute from './components/ProtectedRoute';
import PublicRoute from './components/PublicRoute';
import useAuthStore from './stores/useAuthStore';
import useChatStore from './stores/useChatStore';
import { authApi } from './api/auth';

function App() {
  const { accessToken, user, logout } = useAuthStore();
  const { connect, disconnect } = useChatStore();

  React.useEffect(() => {
    if (!accessToken) return;
    authApi.getMyProfile().catch(() => logout());
  }, []);

  React.useEffect(() => {
    if (accessToken && user?.id) {
      connect(user.id);
    } else {
      disconnect();
    }
  }, [accessToken, user?.id]);

  return (
    <Routes>
      <Route path="/login" element={
        <PublicRoute>
          <LoginPage />
        </PublicRoute>
      } />
      <Route path="/signup" element={
        <PublicRoute>
          <SignupPage />
        </PublicRoute>
      } />
      
      {/* Protected Routes */}
      {/* Root redirect to DM */}
      <Route path="/" element={
        <ProtectedRoute>
          <Navigate to="/channels/@me" replace />
        </ProtectedRoute>
      } />
      
      {/* Main App Routes with URL params */}
      <Route path="/channels/:serverId/:channelId?" element={
        <ProtectedRoute>
          <MainPage />
        </ProtectedRoute>
      } />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
