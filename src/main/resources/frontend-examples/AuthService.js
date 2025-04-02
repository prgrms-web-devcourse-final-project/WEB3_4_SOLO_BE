import axios from 'axios';

/**
 * AuthService - 인증 관련 API 호출 및 토큰 관리를 위한 서비스
 */
class AuthService {
  constructor() {
    this.api = axios.create({
      baseURL: '/api',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    // 요청 인터셉터에 토큰 추가
    this.api.interceptors.request.use(config => {
      const token = this.getAccessToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });
  }

  /**
   * OAuth 인증 코드로 토큰 교환
   * @param {string} code - OAuth 인증 코드
   * @param {string} provider - OAuth 제공자 (kakao)
   * @returns {Promise<Object>} 토큰 정보가 포함된 응답
   */
  async exchangeCodeForToken(code, provider = 'kakao') {
    try {
      const response = await this.api.post(`/auth/token`, { 
        code, 
        provider 
      });
      
      if (response.data && response.data.accessToken) {
        this.setTokens(response.data.accessToken, response.data.refreshToken);
      }
      
      return response.data;
    } catch (error) {
      console.error('토큰 교환 오류:', error);
      throw error;
    }
  }

  /**
   * 토큰 저장
   * @param {string} accessToken - 액세스 토큰
   * @param {string} refreshToken - 리프레시 토큰
   */
  setTokens(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }

  /**
   * 액세스 토큰 조회
   * @returns {string|null} 액세스 토큰
   */
  getAccessToken() {
    return localStorage.getItem('accessToken');
  }

  /**
   * 리프레시 토큰 조회
   * @returns {string|null} 리프레시 토큰
   */
  getRefreshToken() {
    return localStorage.getItem('refreshToken');
  }

  /**
   * 사용자 로그아웃
   */
  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    // 필요시 서버에 로그아웃 요청 보내기
    // this.api.post('/auth/logout');
  }

  /**
   * 사용자 인증 상태 확인
   * @returns {boolean} 인증 여부
   */
  isAuthenticated() {
    return !!this.getAccessToken();
  }

  /**
   * 액세스 토큰 갱신
   * @returns {Promise<Object>} 갱신된 토큰 정보
   */
  async refreshToken() {
    try {
      const refreshToken = this.getRefreshToken();
      if (!refreshToken) {
        throw new Error('리프레시 토큰이 없습니다');
      }

      const response = await this.api.post('/auth/refresh-token', {
        refreshToken
      });

      if (response.data && response.data.accessToken) {
        this.setTokens(response.data.accessToken, response.data.refreshToken || refreshToken);
      }

      return response.data;
    } catch (error) {
      console.error('토큰 갱신 오류:', error);
      this.logout(); // 갱신 실패 시 로그아웃
      throw error;
    }
  }

  /**
   * 현재 사용자 정보 조회
   * @returns {Promise<Object>} 사용자 정보
   */
  async getCurrentUser() {
    try {
      const response = await this.api.get('/user/profile');
      return response.data;
    } catch (error) {
      console.error('사용자 정보 조회 오류:', error);
      
      // 401 에러인 경우 토큰 갱신 시도
      if (error.response && error.response.status === 401) {
        try {
          await this.refreshToken();
          // 토큰 갱신 후 다시 요청
          const newResponse = await this.api.get('/user/profile');
          return newResponse.data;
        } catch (refreshError) {
          // 토큰 갱신도 실패한 경우
          throw refreshError;
        }
      }
      
      throw error;
    }
  }
}

export default new AuthService(); 