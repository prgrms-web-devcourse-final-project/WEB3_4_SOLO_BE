import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import OAuthCallback from './OAuthCallback';
import ErrorHandler from './ErrorHandler';
import LoginButton from './LoginButton';

// 홈 페이지 컴포넌트
const HomePage = () => {
  return (
    <div className="home-page" style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
      <div className="welcome-section" style={{ textAlign: 'center', marginBottom: '2rem' }}>
        <h1>플리지뱅크에 오신 것을 환영합니다</h1>
        <p>안전하고 편리한 금융 서비스를 경험해보세요.</p>
        <div style={{ margin: '2rem 0' }}>
          <LoginButton />
        </div>
      </div>
      
      {/* 기능 섹션 */}
      <div className="features-section" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '2rem', marginBottom: '2rem' }}>
        <FeatureCard 
          title="계좌 관리" 
          description="여러 은행의 계좌를 한 곳에서 관리하세요." 
          icon="💰" 
        />
        <FeatureCard 
          title="송금 서비스" 
          description="쉽고 빠른 계좌 이체를 경험해보세요." 
          icon="💸" 
        />
        <FeatureCard 
          title="금융 분석" 
          description="소비 패턴을 분석하고 맞춤형 조언을 받아보세요." 
          icon="📊" 
        />
      </div>
      
      {/* 푸터 */}
      <footer style={{ textAlign: 'center', borderTop: '1px solid #eee', paddingTop: '1rem', marginTop: '1rem' }}>
        <p>&copy; 2023 플리지뱅크. All rights reserved.</p>
      </footer>
    </div>
  );
};

// 기능 카드 컴포넌트
const FeatureCard = ({ title, description, icon }) => {
  return (
    <div className="feature-card" style={{ 
      padding: '1.5rem', 
      border: '1px solid #eee', 
      borderRadius: '8px',
      boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
    }}>
      <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>{icon}</div>
      <h3 style={{ marginBottom: '0.5rem' }}>{title}</h3>
      <p style={{ color: '#666' }}>{description}</p>
    </div>
  );
};

// 대시보드 페이지 (로그인 후)
const DashboardPage = () => {
  return (
    <div className="dashboard" style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>플리지뱅크 대시보드</h1>
        <LoginButton />
      </header>
      
      <div style={{ marginBottom: '2rem' }}>
        <h2>계좌 현황</h2>
        <p>아직 등록된 계좌가 없습니다. 계좌를 연결해보세요.</p>
        <button style={{ 
          padding: '0.5rem 1rem', 
          backgroundColor: '#4CAF50', 
          color: 'white', 
          border: 'none', 
          borderRadius: '4px',
          cursor: 'pointer'
        }}>
          계좌 연결하기
        </button>
      </div>
      
      <footer style={{ textAlign: 'center', borderTop: '1px solid #eee', paddingTop: '1rem', marginTop: '1rem' }}>
        <p>&copy; 2023 플리지뱅크. All rights reserved.</p>
      </footer>
    </div>
  );
};

// 메인 앱 컴포넌트
const App = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/auth/callback" element={<OAuthCallback />} />
          <Route path="*" element={<ErrorHandler />} errorElement={<ErrorHandler />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App; 