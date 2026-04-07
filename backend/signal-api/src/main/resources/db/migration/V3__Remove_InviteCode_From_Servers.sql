-- 1. servers 테이블에서 invite_code 컬럼 삭제
-- 이제 초대 코드는 DB가 아닌 Redis(메모리)에서 관리될 예정입니다.
ALTER TABLE servers DROP COLUMN invite_code;