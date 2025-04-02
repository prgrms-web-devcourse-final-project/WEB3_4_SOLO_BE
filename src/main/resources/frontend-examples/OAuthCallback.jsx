import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

/**
 * OAuth 콜백을 처리하는 React 컴포넌트
 * 이 컴포넌트는 /auth/callback 경로에 마운트되어야 합니다.
 */
const OAuthCallback = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState(null);
  const [tokenInfo, setTokenInfo] = useState(null);

  useEffect(() => {
    // URL에서 인증 코드 파싱
    const params = new URLSearchParams(location.search);
    const code = params.get('code');
    const error = params.get('error');

    if (error) {
      setStatus('error');
      setError(decodeURIComponent(error));
      return;
    }

    if (!code) {
      setStatus('error');
      setError('인증 코드가 없습니다.');
      return;
    }

    // 백엔드 API에 인증 코드 전송
    const processAuth = async () => {
      try {
        const response = await fetch('/auth/token-display?code=' + code, {
          method: 'GET',
          headers: {
            'Accept': 'application/json'
          }
        });

        const data = await response.json();

        if (!response.ok) {
          setStatus('error');
          setError(data.error || '인증 처리 중 오류가 발생했습니다.');
          return;
        }

        if (data.status === 'processing') {
          // 처리 중인 경우 일정 시간 후 다시 시도
          setTimeout(processAuth, 2000);
          return;
        }

        if (data.status === 'success') {
          // 토큰 저장
          localStorage.setItem('accessToken', data.accessToken);
          localStorage.setItem('refreshToken', data.refreshToken || '');
          
          setStatus('success');
          setTokenInfo({
            accessToken: data.accessToken,
            refreshToken: data.refreshToken
          });
          
          // 잠시 후 홈으로 리다이렉트
          setTimeout(() => navigate('/'), 3000);
        }
      } catch (err) {
        setStatus('error');
        setError('서버 연결 오류: ' + err.message);
      }
    };

    processAuth();
  }, [location, navigate]);

  if (status === 'loading') {
    return (
      <div className="auth-callback">
        <h2>인증 처리 중...</h2>
        <div className="spinner"></div>
        <p>인증 정보를 처리하고 있습니다. 잠시만 기다려주세요.</p>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="auth-callback error">
        <h2>인증 오류</h2>
        <p className="error-message">{error}</p>
        <button onClick={() => navigate('/login')}>다시 로그인</button>
      </div>
    );
  }

  return (
    <div className="auth-callback success">
      <h2>인증 성공!</h2>
      <p>성공적으로 로그인되었습니다.</p>
      <p>곧 홈 화면으로 이동합니다...</p>
      
      {/* 개발 모드에서만 표시 (실제 애플리케이션에서는 토큰을 표시하지 않는 것이 보안상 좋음) */}
      {process.env.NODE_ENV === 'development' && tokenInfo && (
        <div className="token-debug">
          <h3>디버그 정보 (개발 모드에서만 표시)</h3>
          <p>액세스 토큰: {tokenInfo.accessToken.substring(0, 20)}...</p>
          {tokenInfo.refreshToken && (
            <p>리프레시 토큰: {tokenInfo.refreshToken.substring(0, 20)}...</p>
          )}
        </div>
      )}
    </div>
  );
};

export default OAuthCallback; 