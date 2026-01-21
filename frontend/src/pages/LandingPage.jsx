import React, { useState } from 'react';
import client from '../api/client';
import useAuthStore from '../stores/useAuthStore';
import clsx from 'clsx';
import './LandingPage.css'; // 스타일 분리

const LandingPage = () => {
  const { login } = useAuthStore();
  const [isLoginMode, setIsLoginMode] = useState(true); // 기본은 로그인 모드
  const [formData, setFormData] = useState({ email: '', username: '', password: '' });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      let userId;
      let username = formData.username;

      if (isLoginMode) {
        // Login
        const response = await client.post('/users/login', {
          email: formData.email,
          password: formData.password
        });
        userId = response.data;
        // 로그인 시 username을 모르므로 임시 처리 혹은 별도 조회 필요
        if (!username) username = "User"; 
      } else {
        // Signup
        const response = await client.post('/users/signup', formData);
        userId = response.data; 
      }
      
      login(userId, username);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="landing-container">
      <div className="landing-card">
        <h1 className="landing-title">
          {isLoginMode ? 'Welcome Back!' : 'Create an Account'}
        </h1>
        <p className="landing-subtitle">
          {isLoginMode ? 'We are so excited to see you again!' : 'The workspace for every moment.'}
        </p>

        <form onSubmit={handleSubmit} className="landing-form">
          <div className="input-group">
            <label>Email</label>
            <input 
              type="email" 
              required
              name="email"
              value={formData.email}
              onChange={(e) => setFormData({...formData, email: e.target.value})}
            />
          </div>

          {!isLoginMode && (
            <div className="input-group">
              <label>Username</label>
              <input 
                type="text" 
                required
                name="username"
                value={formData.username}
                onChange={(e) => setFormData({...formData, username: e.target.value})}
              />
            </div>
          )}

          <div className="input-group">
            <label>Password</label>
            <input 
              type="password" 
              required
              name="password"
              value={formData.password}
              onChange={(e) => setFormData({...formData, password: e.target.value})}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button 
            type="submit" 
            className={clsx('submit-btn', { loading: isLoading })}
            disabled={isLoading}
          >
            {isLoading ? 'Processing...' : (isLoginMode ? 'Log In' : 'Sign Up')}
          </button>

          <div className="toggle-mode">
            {isLoginMode ? (
              <>Need an account? <span onClick={() => setIsLoginMode(false)}>Register</span></>
            ) : (
              <>Already have an account? <span onClick={() => setIsLoginMode(true)}>Log in</span></>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default LandingPage;
