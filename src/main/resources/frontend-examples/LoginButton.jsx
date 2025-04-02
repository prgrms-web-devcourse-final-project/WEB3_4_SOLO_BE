import React from 'react';
import { useAuth } from './AuthContext';

/**
 * 카카오 로그인 버튼 컴포넌트
 * 
 * @param {Object} props 컴포넌트 속성
 * @param {string} [props.className] 추가 CSS 클래스
 * @param {string} [props.redirectUri='/auth/callback'] 인증 후 리다이렉트 URI
 */
const KakaoLoginButton = ({ className = '', redirectUri = '/auth/callback' }) => {
  // 백엔드 API 엔드포인트 설정
  const OAUTH_URI = `/api/auth/oauth2/authorize/kakao?redirect_uri=${encodeURIComponent(window.location.origin + redirectUri)}`;

  return (
    <a
      href={OAUTH_URI}
      className={`kakao-login-btn ${className}`}
      style={{
        display: 'inline-block',
        padding: '10px 20px',
        backgroundColor: '#FEE500',
        color: '#000000',
        textDecoration: 'none',
        borderRadius: '4px',
        fontWeight: 'bold',
        textAlign: 'center',
        cursor: 'pointer',
        border: 'none',
        fontSize: '16px'
      }}
    >
      카카오계정으로 로그인
    </a>
  );
};

/**
 * 로그인/로그아웃 버튼 컴포넌트
 * 
 * @param {Object} props 컴포넌트 속성 
 * @param {string} [props.className] 추가 CSS 클래스
 * @param {string} [props.redirectUri='/auth/callback'] 인증 후 리다이렉트 URI
 * @param {Function} [props.onLogout] 로그아웃 시 실행할 콜백 함수
 */
const LoginButton = ({ className = '', redirectUri = '/auth/callback', onLogout }) => {
  const { isAuthenticated, logout, user } = useAuth();

  const handleLogout = () => {
    logout();
    if (onLogout) {
      onLogout();
    }
  };

  // 로그인 상태에 따라 다른 버튼 렌더링
  if (isAuthenticated) {
    return (
      <div className="login-status">
        <span className="user-info">
          {user?.name || '사용자'} 님
        </span>
        <button
          onClick={handleLogout}
          className={`logout-btn ${className}`}
          style={{
            marginLeft: '10px',
            padding: '8px 16px',
            backgroundColor: '#f0f0f0',
            color: '#333',
            border: '1px solid #ddd',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '14px'
          }}
        >
          로그아웃
        </button>
      </div>
    );
  }

  // 비로그인 상태일 때 카카오 로그인 버튼 표시
  return <KakaoLoginButton className={className} redirectUri={redirectUri} />;
};

export default LoginButton; 