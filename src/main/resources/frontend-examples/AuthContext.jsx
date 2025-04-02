import React, { createContext, useState, useContext, useEffect } from 'react';
import AuthService from './AuthService';

// 인증 컨텍스트 생성
const AuthContext = createContext(null);

/**
 * 인증 상태 및 기능을 제공하는 Provider 컴포넌트
 * 
 * @param {Object} props 컴포넌트 속성
 * @param {React.ReactNode} props.children 자식 컴포넌트
 */
export const AuthProvider = ({ children }) => {
  // 사용자 정보 상태
  const [user, setUser] = useState(null);
  // 로딩 상태
  const [loading, setLoading] = useState(true);
  // 오류 상태
  const [error, setError] = useState(null);

  // 컴포넌트 마운트 시 인증 상태 확인
  useEffect(() => {
    const loadUserProfile = async () => {
      // 인증되지 않은 경우 바로 로딩 종료
      if (!AuthService.isAuthenticated()) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        // 사용자 정보 로드
        const userData = await AuthService.getCurrentUser();
        setUser(userData);
        setError(null);
      } catch (err) {
        console.error('사용자 정보 로드 실패:', err);
        setUser(null);
        setError('인증 정보를 확인할 수 없습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadUserProfile();
  }, []);

  /**
   * OAuth 인증 코드로 로그인
   * 
   * @param {string} code OAuth 인증 코드
   * @param {string} provider OAuth 제공자 (kakao, 등)
   */
  const login = async (code, provider = 'kakao') => {
    try {
      setLoading(true);
      // 인증 코드로 토큰 교환
      await AuthService.exchangeCodeForToken(code, provider);
      // 사용자 정보 로드
      const userData = await AuthService.getCurrentUser();
      setUser(userData);
      setError(null);
      return true;
    } catch (err) {
      console.error('로그인 실패:', err);
      setUser(null);
      setError('로그인에 실패했습니다. 다시 시도해주세요.');
      return false;
    } finally {
      setLoading(false);
    }
  };

  /**
   * 로그아웃 처리
   */
  const logout = () => {
    AuthService.logout();
    setUser(null);
  };

  /**
   * 액세스 토큰 갱신
   */
  const refreshUserToken = async () => {
    try {
      setLoading(true);
      await AuthService.refreshToken();
      // 토큰 갱신 후 사용자 정보 다시 로드
      const userData = await AuthService.getCurrentUser();
      setUser(userData);
      setError(null);
      return true;
    } catch (err) {
      console.error('토큰 갱신 실패:', err);
      // 갱신 실패 시 로그아웃 처리
      logout();
      setError('인증이 만료되었습니다. 다시 로그인해주세요.');
      return false;
    } finally {
      setLoading(false);
    }
  };

  // 컨텍스트에 제공할 값
  const value = {
    user,
    loading,
    error,
    isAuthenticated: !!user,
    login,
    logout,
    refreshUserToken
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * AuthContext를 사용하기 위한 커스텀 훅
 * 
 * @returns {Object} 인증 관련 상태 및 함수
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === null) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext; 