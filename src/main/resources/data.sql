-- 금융상품 테이블에 기본 데이터 삽입
INSERT INTO financial_products (name, category, interest_rate, term, min_amount, max_amount, description, features, is_active, status, created_at, updated_at)
VALUES 
('플리지뱅크 정기예금', 'DEPOSIT', 3.6, 36, 1000000.00, 50000000.00, '안정적인 수익을 제공하는 정기예금 상품입니다.', '["안정적인 수익", "만기 시 이자 지급", "예금자 보호"]', true, 'ACTIVE', NOW(), NOW()),
('플리지뱅크 정기적금', 'SAVINGS', 4.2, 36, 100000.00, 3000000.00, '매월 일정액을 저축하여 목돈을 모으는 정기적금 상품입니다.', '["높은 이자율", "정기적 저축", "목돈 마련"]', true, 'ACTIVE', NOW(), NOW()),
('플리지뱅크 주택담보대출', 'LOAN', 5.1, 360, 10000000.00, 500000000.00, '주택 구입을 위한 저금리 대출 상품입니다.', '["저금리", "장기 상환", "주택 담보"]', true, 'ACTIVE', NOW(), NOW()),
('청년 우대 적금', 'SAVINGS', 4.8, 24, 50000.00, 1000000.00, '39세 이하 청년을 위한 우대금리 적금 상품입니다.', '["청년 우대", "높은 금리", "소액 시작"]', true, 'ACTIVE', NOW(), NOW()),
('플리지 자유입출금', 'DEPOSIT', 1.8, NULL, 0, NULL, '언제든지 자유롭게 입금하고 출금할 수 있는 예금 상품', '["입출금 자유", "연 1.8% 금리", "수수료 면제", "모바일뱅킹 연동"]', true, 'ACTIVE', NOW(), NOW()); 