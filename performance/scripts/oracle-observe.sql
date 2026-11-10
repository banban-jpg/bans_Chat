-- 전용 합성 스키마에서 운영자가 수동 실행하는 읽기 전용 관측 자료.
-- 동적 성능 뷰 조회 권한이 없으면 DBA가 별도로 수행한다. 앱/AI에 권한을 주지 않는다.
SELECT banner_full FROM v$version;
SELECT name, value FROM v$parameter WHERE name IN ('cpu_count','sga_target','pga_aggregate_target','processes','sessions');
SELECT status, COUNT(*) AS session_count FROM v$session WHERE type='USER' GROUP BY status;
SELECT wait_class, COUNT(*) AS waiting_sessions FROM v$session WHERE state='WAITING' GROUP BY wait_class;
SELECT ROUND(SUM(bytes)/1024/1024,2) AS allocated_segment_mb FROM user_segments;
-- 세그먼트 할당량은 전체 Free 사용자 데이터 한도/실사용량과 동일하지 않다.
-- 실행계획은 해당 실험 SQL_ID를 확인한 운영자가 DBMS_XPLAN.DISPLAY_CURSOR로 별도 수집한다.
-- SQL 본문/바인드/세션 ID/직원 데이터/AI 대화는 결과 파일에 기록하지 않는다.
