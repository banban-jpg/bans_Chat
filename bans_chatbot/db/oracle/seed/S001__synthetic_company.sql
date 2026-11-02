-- Oracle 26ai synthetic fixture seed. Generated from src/main/resources/fixtures.
-- Accounts are PENDING with an unusable marker, never a committed password or activation code.
-- Run only through seed/apply_seed.sql in a dedicated synthetic schema.

MERGE INTO department t USING (
    SELECT 'D01' department_code, '가상 인사부' department_name FROM dual
) s ON (t.department_code = s.department_code)
WHEN NOT MATCHED THEN INSERT (department_code, department_name) VALUES (s.department_code, s.department_name);

MERGE INTO department t USING (
    SELECT 'D02' department_code, '가상 재무부' department_name FROM dual
) s ON (t.department_code = s.department_code)
WHEN NOT MATCHED THEN INSERT (department_code, department_name) VALUES (s.department_code, s.department_name);

MERGE INTO department t USING (
    SELECT 'D03' department_code, '가상 개발부' department_name FROM dual
) s ON (t.department_code = s.department_code)
WHEN NOT MATCHED THEN INSERT (department_code, department_name) VALUES (s.department_code, s.department_name);

MERGE INTO department t USING (
    SELECT 'D04' department_code, '가상 영업부' department_name FROM dual
) s ON (t.department_code = s.department_code)
WHEN NOT MATCHED THEN INSERT (department_code, department_name) VALUES (s.department_code, s.department_name);

MERGE INTO department t USING (
    SELECT 'D05' department_code, '가상 경영지원부' department_name FROM dual
) s ON (t.department_code = s.department_code)
WHEN NOT MATCHED THEN INSERT (department_code, department_name) VALUES (s.department_code, s.department_name);

MERGE INTO employee t USING (
    SELECT 'EMP-001' employee_code, '가상인사01' display_name, d.department_id, '인사팀장' job_title, '2101' extension_no, 'emp001@bans.example' company_email, '가상 인사 정책 관리' duty_summary FROM department d WHERE d.department_code = 'D01'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-002' employee_code, '가상인사02' display_name, d.department_id, '인사담당' job_title, '2102' extension_no, 'emp002@bans.example' company_email, '가상 휴가와 온보딩 안내' duty_summary FROM department d WHERE d.department_code = 'D01'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-003' employee_code, '가상인사03' display_name, d.department_id, '채용담당' job_title, '2103' extension_no, 'emp003@bans.example' company_email, '가상 채용 절차 관리' duty_summary FROM department d WHERE d.department_code = 'D01'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-004' employee_code, '가상인사04' display_name, d.department_id, '교육담당' job_title, '2104' extension_no, 'emp004@bans.example' company_email, '가상 교육 일정 관리' duty_summary FROM department d WHERE d.department_code = 'D01'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-005' employee_code, '가상재무01' display_name, d.department_id, '재무팀장' job_title, '2201' extension_no, 'emp005@bans.example' company_email, '가상 정산 정책 관리' duty_summary FROM department d WHERE d.department_code = 'D02'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-006' employee_code, '가상재무02' display_name, d.department_id, '회계담당' job_title, '2202' extension_no, 'emp006@bans.example' company_email, '가상 비용 검토' duty_summary FROM department d WHERE d.department_code = 'D02'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-007' employee_code, '가상재무03' display_name, d.department_id, '예산담당' job_title, '2203' extension_no, 'emp007@bans.example' company_email, '가상 예산 계획 관리' duty_summary FROM department d WHERE d.department_code = 'D02'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-008' employee_code, '가상개발01' display_name, d.department_id, '개발팀장' job_title, '2301' extension_no, 'emp008@bans.example' company_email, '가상 배포 일정 관리' duty_summary FROM department d WHERE d.department_code = 'D03'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-009' employee_code, '가상개발02' display_name, d.department_id, '백엔드개발자' job_title, '2302' extension_no, 'emp009@bans.example' company_email, '가상 API 설계' duty_summary FROM department d WHERE d.department_code = 'D03'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-010' employee_code, '가상개발03' display_name, d.department_id, '프론트엔드개발자' job_title, '2303' extension_no, 'emp010@bans.example' company_email, '가상 UI 구현' duty_summary FROM department d WHERE d.department_code = 'D03'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-011' employee_code, '가상개발04' display_name, d.department_id, '보안담당' job_title, '2304' extension_no, 'emp011@bans.example' company_email, '가상 보안 점검' duty_summary FROM department d WHERE d.department_code = 'D03'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-012' employee_code, '가상개발05' display_name, d.department_id, '품질담당' job_title, '2305' extension_no, 'emp012@bans.example' company_email, '가상 테스트 계획' duty_summary FROM department d WHERE d.department_code = 'D03'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-013' employee_code, '가상개발06' display_name, d.department_id, '운영담당' job_title, '2306' extension_no, 'emp013@bans.example' company_email, '가상 서비스 모니터링' duty_summary FROM department d WHERE d.department_code = 'D03'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-014' employee_code, '가상영업01' display_name, d.department_id, '영업팀장' job_title, '2401' extension_no, 'emp014@bans.example' company_email, '가상 계약 검토' duty_summary FROM department d WHERE d.department_code = 'D04'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-015' employee_code, '가상영업02' display_name, d.department_id, '영업담당' job_title, '2402' extension_no, 'emp015@bans.example' company_email, '가상 고객 일정 관리' duty_summary FROM department d WHERE d.department_code = 'D04'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-016' employee_code, '가상영업03' display_name, d.department_id, '제안담당' job_title, '2403' extension_no, 'emp016@bans.example' company_email, '가상 제안서 관리' duty_summary FROM department d WHERE d.department_code = 'D04'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-017' employee_code, '가상영업04' display_name, d.department_id, '지원담당' job_title, '2404' extension_no, 'emp017@bans.example' company_email, '가상 영업 자료 관리' duty_summary FROM department d WHERE d.department_code = 'D04'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-018' employee_code, '가상지원01' display_name, d.department_id, '경영지원팀장' job_title, '2501' extension_no, 'emp018@bans.example' company_email, '가상 장비 정책 관리' duty_summary FROM department d WHERE d.department_code = 'D05'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-019' employee_code, '가상지원02' display_name, d.department_id, '총무담당' job_title, '2502' extension_no, 'emp019@bans.example' company_email, '가상 비품 신청 관리' duty_summary FROM department d WHERE d.department_code = 'D05'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee t USING (
    SELECT 'EMP-020' employee_code, '가상지원03' display_name, d.department_id, '시설담당' job_title, '2503' extension_no, 'emp020@bans.example' company_email, '가상 시설 일정 관리' duty_summary FROM department d WHERE d.department_code = 'D05'
) s ON (t.employee_code = s.employee_code)
WHEN NOT MATCHED THEN INSERT (employee_code, display_name, department_id, job_title, extension_no, company_email, duty_summary) VALUES (s.employee_code, s.display_name, s.department_id, s.job_title, s.extension_no, s.company_email, s.duty_summary);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp001@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-001' AND r.role_code = 'ACCOUNT_ADMIN' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-001' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp002@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-002'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-002' AND r.role_code = 'DOCUMENT_MANAGER' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-002' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp005@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-005'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-005' AND r.role_code = 'DOCUMENT_EDITOR' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-005' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp006@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-006'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-006' AND r.role_code = 'AUDITOR' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-006' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp008@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-008'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-008' AND r.role_code = 'DOCUMENT_EDITOR' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-008' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp009@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-009'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-009' AND r.role_code = 'DOCUMENT_MANAGER' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-009' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp010@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-010'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-010' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp011@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-011'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-011' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp014@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-014'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-014' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp015@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-015'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-015' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp018@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-018'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-018' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO employee_account t USING (
    SELECT e.employee_id, 'emp019@bans.example' login_id FROM employee e WHERE e.employee_code = 'EMP-019'
) s ON (t.employee_id = s.employee_id)
WHEN NOT MATCHED THEN INSERT (employee_id, login_id, password_hash, account_status) VALUES (s.employee_id, s.login_id, '!PENDING_ACTIVATION!', 'PENDING');

MERGE INTO employee_role t USING (
    SELECT e.employee_id, r.role_code, issuer.employee_id issuer_id FROM employee e CROSS JOIN system_role r CROSS JOIN employee issuer WHERE e.employee_code = 'EMP-019' AND r.role_code = 'EMPLOYEE' AND issuer.employee_code = 'EMP-001'
) s ON (t.employee_id = s.employee_id AND t.role_code = s.role_code)
WHEN NOT MATCHED THEN INSERT (employee_id, role_code, assigned_by_employee_id) VALUES (s.employee_id, s.role_code, s.issuer_id);

MERGE INTO document t USING (
    SELECT 'DOC-001' document_code, '가상 휴가 신청 안내' document_title, 'VACATION' document_category, e.employee_id owner_employee_id, NULL department_id, 'COMPANY' document_scope FROM employee e WHERE e.employee_code = 'EMP-001'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 가상 휴가 안내: 합성 직원은 예시 양식을 사용합니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-001' WHERE d.document_code = 'DOC-001'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 가상 휴가 안내: 합성 직원은 예시 양식을 사용합니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-001'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'Y', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-001'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-001'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-001') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-001' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-002' document_code, '가상 출장 정산 절차' document_title, 'EXPENSE' document_category, e.employee_id owner_employee_id, (SELECT department_id FROM department WHERE department_code = 'D02') department_id, 'DEPARTMENT' document_scope FROM employee e WHERE e.employee_code = 'EMP-005'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 가상 정산: 합성 영수증 번호만 사용합니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-005' WHERE d.document_code = 'DOC-002'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 가상 정산: 합성 영수증 번호만 사용합니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-002'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-002'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-014' WHERE d.document_code = 'DOC-002'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-002'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-002'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-002') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-002' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-003' document_code, '가상 배포 체크리스트' document_title, 'DEPLOYMENT' document_category, e.employee_id owner_employee_id, (SELECT department_id FROM department WHERE department_code = 'D03') department_id, 'DEPARTMENT' document_scope FROM employee e WHERE e.employee_code = 'EMP-008'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 가상 배포: 테스트 환경만 점검합니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-008' WHERE d.document_code = 'DOC-003'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 가상 배포: 테스트 환경만 점검합니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-003'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'Y', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-003'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-003'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-003') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-003' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-004' document_code, '가상 장비 신청 기준' document_title, 'EQUIPMENT' document_category, e.employee_id owner_employee_id, NULL department_id, 'COMPANY' document_scope FROM employee e WHERE e.employee_code = 'EMP-018'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '가상 장비 신청은 예시 승인 절차를 따릅니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-018' WHERE d.document_code = 'DOC-004'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '가상 장비 신청은 예시 승인 절차를 따릅니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-004'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-004'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-004'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-004') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-004' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-005' document_code, '가상 계약 검토 흐름' document_title, 'CONTRACT' document_category, e.employee_id owner_employee_id, (SELECT department_id FROM department WHERE department_code = 'D04') department_id, 'DEPARTMENT' document_scope FROM employee e WHERE e.employee_code = 'EMP-014'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 가상 계약: 실제 거래처 정보는 포함하지 않습니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-014' WHERE d.document_code = 'DOC-005'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 가상 계약: 실제 거래처 정보는 포함하지 않습니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-005'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-005'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-005'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-005'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-005') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-005' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-006' document_code, '가상 신입 온보딩' document_title, 'ONBOARDING' document_category, e.employee_id owner_employee_id, NULL department_id, 'COMPANY' document_scope FROM employee e WHERE e.employee_code = 'EMP-003'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 가상 온보딩: 예시 계정 신청 순서입니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-003' WHERE d.document_code = 'DOC-006'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 가상 온보딩: 예시 계정 신청 순서입니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-006'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'Y', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-006'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-006'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-006') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-006' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-007' document_code, '합성 보안 접근 규칙 예시' document_title, 'SECURITY' document_category, e.employee_id owner_employee_id, NULL department_id, 'PRIVATE' document_scope FROM employee e WHERE e.employee_code = 'EMP-011'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '가상 보안 문서입니다. 실제 비밀값은 존재하지 않습니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-011' WHERE d.document_code = 'DOC-007'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '가상 보안 문서입니다. 실제 비밀값은 존재하지 않습니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-007'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'N');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-007'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-007'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-007'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-007') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-007' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-008' document_code, '가상 연차 일정 예시' document_title, 'VACATION' document_category, e.employee_id owner_employee_id, (SELECT department_id FROM department WHERE department_code = 'D01') department_id, 'DEPARTMENT' document_scope FROM employee e WHERE e.employee_code = 'EMP-004'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '가상 인사부의 연차 일정 예시입니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-004' WHERE d.document_code = 'DOC-008'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '가상 인사부의 연차 일정 예시입니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-008'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'N', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-008'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-008'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-008') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-008' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-009' document_code, '가상 비용 코드 표' document_title, 'EXPENSE' document_category, e.employee_id owner_employee_id, NULL department_id, 'COMPANY' document_scope FROM employee e WHERE e.employee_code = 'EMP-007'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '가상 비용 코드는 예시 분류만 표시합니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-007' WHERE d.document_code = 'DOC-009'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '가상 비용 코드는 예시 분류만 표시합니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-009'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-009'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-009'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-009') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-009' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-010' document_code, '가상 내부 배포 메모' document_title, 'DEPLOYMENT' document_category, e.employee_id owner_employee_id, NULL department_id, 'PRIVATE' document_scope FROM employee e WHERE e.employee_code = 'EMP-013'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 가상 배포 메모: 실제 서버 주소는 없습니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-013' WHERE d.document_code = 'DOC-010'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 가상 배포 메모: 실제 서버 주소는 없습니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-010'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-010'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-010'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-010'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-010') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-010' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-011' document_code, '가상 비품 반납 안내' document_title, 'EQUIPMENT' document_category, e.employee_id owner_employee_id, (SELECT department_id FROM department WHERE department_code = 'D05') department_id, 'DEPARTMENT' document_scope FROM employee e WHERE e.employee_code = 'EMP-019'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '가상 비품 반납은 예시 장부에 기록합니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-019' WHERE d.document_code = 'DOC-011'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '가상 비품 반납은 예시 장부에 기록합니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-011'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'N', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-011'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-011'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-011'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-011') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-011' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-012' document_code, '가상 영업 계약 초안 지침' document_title, 'CONTRACT' document_category, e.employee_id owner_employee_id, NULL department_id, 'PRIVATE' document_scope FROM employee e WHERE e.employee_code = 'EMP-015'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 가상 계약 초안: 실제 고객명은 없습니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-015' WHERE d.document_code = 'DOC-012'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 가상 계약 초안: 실제 고객명은 없습니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-012'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-012'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-012'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-012'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-012') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-012' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-013' document_code, '가상 입사 첫날 안내' document_title, 'ONBOARDING' document_category, e.employee_id owner_employee_id, NULL department_id, 'COMPANY' document_scope FROM employee e WHERE e.employee_code = 'EMP-002'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '가상 입사 첫날에는 예시 팀 소개를 읽습니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-013'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '가상 입사 첫날에는 예시 팀 소개를 읽습니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-013'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-013'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-013'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-013') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-013' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-014' document_code, '합성 보안 점검 체크리스트' document_title, 'SECURITY' document_category, e.employee_id owner_employee_id, (SELECT department_id FROM department WHERE department_code = 'D03') department_id, 'DEPARTMENT' document_scope FROM employee e WHERE e.employee_code = 'EMP-011'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 가상 보안 점검: 실제 취약점이나 키는 없습니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-011' WHERE d.document_code = 'DOC-014'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 가상 보안 점검: 실제 취약점이나 키는 없습니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-014'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'N');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-014'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-014'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-014') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-014' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-015' document_code, '가상 휴가 FAQ' document_title, 'VACATION' document_category, e.employee_id owner_employee_id, NULL department_id, 'COMPANY' document_scope FROM employee e WHERE e.employee_code = 'EMP-003'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '가상 휴가 질문은 예시 답변을 참고합니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-003' WHERE d.document_code = 'DOC-015'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '가상 휴가 질문은 예시 답변을 참고합니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-015'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'Y', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-015'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-015'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-015') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-015' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-016' document_code, '가상 경비 증빙 예시' document_title, 'EXPENSE' document_category, e.employee_id owner_employee_id, NULL department_id, 'PRIVATE' document_scope FROM employee e WHERE e.employee_code = 'EMP-006'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '가상 경비 증빙에는 합성 금액만 표시합니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-006' WHERE d.document_code = 'DOC-016'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DRAFT', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '가상 경비 증빙에는 합성 금액만 표시합니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-016'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'N', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-016'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-016'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'APPROVED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-016'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document_version v SET version_state = 'PUBLISHED', published_at = SYSTIMESTAMP WHERE v.version_state = 'DRAFT' AND v.document_id = (SELECT document_id FROM document WHERE document_code = 'DOC-016') AND v.version_no = 1;

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'APPROVED' WHERE d.document_code = 'DOC-016' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-017' document_code, '가상 배포 절차 반려안' document_title, 'DEPLOYMENT' document_category, e.employee_id owner_employee_id, (SELECT department_id FROM department WHERE department_code = 'D03') department_id, 'DEPARTMENT' document_scope FROM employee e WHERE e.employee_code = 'EMP-010'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'MARKDOWN' content_format, '# 반려 예시: 합성 검토 의견을 반영해야 합니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-010' WHERE d.document_code = 'DOC-017'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'SUBMITTED', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '# 반려 예시: 합성 검토 의견을 반영해야 합니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-017'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'N', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-017'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'REJECTED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-009' WHERE d.document_code = 'DOC-017'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'DRAFT' WHERE d.document_code = 'DOC-017' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-018' document_code, '가상 계약 서식 반려안' document_title, 'CONTRACT' document_category, e.employee_id owner_employee_id, (SELECT department_id FROM department WHERE department_code = 'D04') department_id, 'DEPARTMENT' document_scope FROM employee e WHERE e.employee_code = 'EMP-016'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '반려 예시 서식에는 실제 계약 내용이 없습니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-016' WHERE d.document_code = 'DOC-018'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'SUBMITTED', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '반려 예시 서식에는 실제 계약 내용이 없습니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-018'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'N', 'N', 'Y', 'Y');

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'READ' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-018'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_grant t USING (
    SELECT d.document_id, e.employee_id, d.owner_employee_id granter_id, 'APPROVE' permission_code FROM document d JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-018'
) s ON (t.document_id = s.document_id AND t.employee_id = s.employee_id AND t.permission_code = s.permission_code)
WHEN NOT MATCHED THEN INSERT (document_id, employee_id, permission_code, granted_by_employee_id) VALUES (s.document_id, s.employee_id, s.permission_code, s.granter_id);

MERGE INTO document_review t USING (
    SELECT d.document_id, v.version_id, e.employee_id reviewer_id, 'REJECTED' decision_code FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 JOIN employee e ON e.employee_code = 'EMP-002' WHERE d.document_code = 'DOC-018'
) s ON (t.version_id = s.version_id AND t.decision_code = s.decision_code)
WHEN NOT MATCHED THEN INSERT (document_id, version_id, reviewer_employee_id, decision_code) VALUES (s.document_id, s.version_id, s.reviewer_id, s.decision_code);

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'DRAFT' WHERE d.document_code = 'DOC-018' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-019' document_code, '가상 구형 장비 안내 폐기본' document_title, 'EQUIPMENT' document_category, e.employee_id owner_employee_id, NULL department_id, 'COMPANY' document_scope FROM employee e WHERE e.employee_code = 'EMP-020'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '폐기된 합성 장비 안내 버전입니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-020' WHERE d.document_code = 'DOC-019'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DISCARDED', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '폐기된 합성 장비 안내 버전입니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-019'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'N', 'N', 'Y', 'Y');

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'ARCHIVED' WHERE d.document_code = 'DOC-019' AND d.current_version_id IS NULL;

MERGE INTO document t USING (
    SELECT 'DOC-020' document_code, '가상 구형 온보딩 안내 폐기본' document_title, 'ONBOARDING' document_category, e.employee_id owner_employee_id, NULL department_id, 'COMPANY' document_scope FROM employee e WHERE e.employee_code = 'EMP-004'
) s ON (t.document_code = s.document_code)
WHEN NOT MATCHED THEN INSERT (document_code, document_title, document_category, owner_employee_id, department_id, document_scope, document_status) VALUES (s.document_code, s.document_title, s.document_category, s.owner_employee_id, s.department_id, s.document_scope, 'DRAFT');

MERGE INTO document_version t USING (
    SELECT d.document_id, e.employee_id creator_id, 'TEXT' content_format, '폐기된 합성 온보딩 안내 버전입니다.' body_text FROM document d JOIN employee e ON e.employee_code = 'EMP-004' WHERE d.document_code = 'DOC-020'
) s ON (t.document_id = s.document_id AND t.version_no = 1)
WHEN NOT MATCHED THEN INSERT (document_id, version_no, content_format, body_text, version_state, created_by_employee_id) VALUES (s.document_id, 1, s.content_format, s.body_text, 'DISCARDED', s.creator_id);

MERGE INTO document_fragment t USING (
    SELECT v.version_id, '폐기된 합성 온보딩 안내 버전입니다.' body_text FROM document d JOIN document_version v ON v.document_id = d.document_id AND v.version_no = 1 WHERE d.document_code = 'DOC-020'
) s ON (t.version_id = s.version_id AND t.fragment_no = 1)
WHEN NOT MATCHED THEN INSERT (version_id, fragment_no, body_text, ai_searchable, external_ai_allowed, synthetic_yn, non_sensitive_yn) VALUES (s.version_id, 1, s.body_text, 'N', 'N', 'Y', 'Y');

UPDATE document d SET current_version_id = (SELECT v.version_id FROM document_version v WHERE v.document_id = d.document_id AND v.version_no = 1), document_status = 'ARCHIVED' WHERE d.document_code = 'DOC-020' AND d.current_version_id IS NULL;

