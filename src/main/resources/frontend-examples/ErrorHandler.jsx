import React from 'react';
import { useRouteError, isRouteErrorResponse } from 'react-router-dom';

/**
 * 에러 핸들링을 위한 React 컴포넌트
 * React Router의 errorElement로 사용됩니다.
 */
const ErrorHandler = () => {
  const error = useRouteError();
  
  // HTTP 상태 코드와 메시지 추출
  let errorStatus = 500;
  let errorMessage = '알 수 없는 오류가 발생했습니다.';
  
  if (isRouteErrorResponse(error)) {
    errorStatus = error.status;
    errorMessage = error.data?.message || error.statusText;
  } else if (error instanceof Error) {
    errorMessage = error.message;
  }
  
  // 상태 코드에 따른 제목 설정
  const titleMap = {
    400: '잘못된 요청',
    401: '인증 필요',
    403: '접근 권한 없음',
    404: '페이지를 찾을 수 없음',
    500: '서버 오류'
  };
  
  const errorTitle = titleMap[errorStatus] || '오류 발생';
  
  return (
    <div className="error-page">
      <div className="error-container">
        <h1 className="error-code">{errorStatus}</h1>
        <h2 className="error-title">{errorTitle}</h2>
        <p className="error-message">{errorMessage}</p>
        <div className="error-actions">
          <button 
            className="btn btn-primary" 
            onClick={() => window.history.back()}
          >
            이전 페이지로
          </button>
          <button 
            className="btn btn-secondary" 
            onClick={() => window.location.href = '/'}
          >
            홈으로 이동
          </button>
        </div>
      </div>
    </div>
  );
};

export default ErrorHandler; 