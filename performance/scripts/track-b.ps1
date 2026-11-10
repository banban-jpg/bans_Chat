$ErrorActionPreference = 'Stop'
# 이 스크립트는 Oracle 설치/기동/마이그레이션/seed/중단을 자동 수행하지 않는다.
foreach ($name in @('ORACLE_JDBC_URL','ORACLE_DB_USER','ORACLE_DB_PASSWORD')) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) { throw 'BLOCKED: ORACLE_CONFIG_MISSING' }
}
if ($env:ORACLE_LOAD_TEST -ne 'YES' -or $env:ORACLE_LOAD_WRITES_ACK -ne 'YES') {
    throw '전용 합성 Oracle 스키마의 커밋 쓰기 실험임을 확인한 뒤 두 환경 gate를 YES로 설정하세요.'
}
Push-Location (Resolve-Path (Join-Path $PSScriptRoot '../../bans_chatbot')).Path
try {
    $previousErrors = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & .\oracleIntegrationTest.cmd '-Dit.test=ChatConcurrencyOracleIT' @args
        $buildExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = $previousErrors }
    if ($buildExit -ne 0) { throw 'Oracle 실험 실패: Failsafe 결과 확인' }
} finally { Pop-Location }
