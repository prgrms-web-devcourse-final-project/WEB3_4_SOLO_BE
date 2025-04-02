# 프론트엔드 예제 코드

이 디렉토리에는 백엔드 API와 함께 사용할 수 있는 프론트엔드 예제 코드가 포함되어 있습니다.

## 파일 구조

- `OAuthCallback.jsx`: OAuth 인증 콜백을 처리하는 React 컴포넌트
- `ErrorHandler.jsx`: 에러 처리를 위한 React 컴포넌트

## 사용 방법

이 파일들은 React 기반 프론트엔드 프로젝트에서 사용할 수 있습니다. 다음과 같이 프로젝트에 통합하세요:

### 1. 프로젝트 설정

```bash
# React 프로젝트 생성
npx create-react-app pleasybank-frontend
cd pleasybank-frontend

# 필요한 라이브러리 설치
npm install react-router-dom axios
```

### 2. 라우터 설정

`src/App.js` 파일에 다음과 같이 라우터를 설정합니다:

```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import OAuthCallback from './components/OAuthCallback';
import ErrorHandler from './components/ErrorHandler';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/auth/callback" element={<OAuthCallback />} />
        <Route path="*" element={<ErrorHandler />} errorElement={<ErrorHandler />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

### 3. 프록시 설정

프론트엔드 개발 서버에서 백엔드 API로 요청을 프록시하려면 `package.json`에 다음 설정을 추가합니다:

```json
{
  "proxy": "http://localhost:8080"
}
```

### 4. 인증 및 API 요청

백엔드 API와의 통신을 위한 유틸리티 함수를 생성합니다:

```jsx
// src/utils/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// 요청 인터셉터에 토큰 추가
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터에 토큰 갱신 로직 추가
api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    
    // 401 에러이고 재시도하지 않은 경우
    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        // 리프레시 토큰으로 새 액세스 토큰 요청
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post('/api/auth/refresh-token', {
          refreshToken
        });
        
        // 새 토큰 저장
        localStorage.setItem('accessToken', response.data.accessToken);
        
        // 원래 요청 재시도
        return api(originalRequest);
      } catch (e) {
        // 토큰 갱신 실패시 로그아웃
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(e);
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;
```

## Thymeleaf 템플릿 대체

이 프론트엔드 예제 코드는 백엔드의 Thymeleaf 템플릿을 대체할 수 있습니다. 주요 대체 사항:

1. **인증 콜백 처리**: `OAuthCallback.jsx`가 `auth/token-display.html`을 대체
2. **에러 페이지**: `ErrorHandler.jsx`가 `error/*` 템플릿을 대체

## 추가 고려사항

1. **보안**: 프론트엔드에서 토큰을 안전하게 저장하는 방법 고려 (localStorage 대신 httpOnly 쿠키 등)
2. **상태 관리**: Redux 또는 Context API를 사용하여 사용자 인증 상태 관리
3. **코드 분할**: 성능 최적화를 위한 코드 스플리팅 적용 