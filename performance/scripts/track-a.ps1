$ErrorActionPreference = 'Stop'
# 인증 정보는 테스트 프로세스가 무작위로 생성한다. 실제 API/DB 설정은 사용하지 않는다.
$modulePath = (Resolve-Path (Join-Path $PSScriptRoot '../../bans_chatbot')).Path
$previousGate = $env:BANS_MOCK_LOAD_TEST
Push-Location $modulePath
try {
    New-Item -ItemType Directory -Path local-notes -Force | Out-Null
    $env:BANS_MOCK_LOAD_TEST = 'YES'
    $previousErrors = $ErrorActionPreference
    try {
        # Windows PowerShell의 native stderr 경고를 종료 코드와 혼동하지 않는다.
        $ErrorActionPreference = 'Continue'
        & .\mvnw.cmd '-Dtest=MockLoadExperimentTest,ProviderFailureExperimentTest' test @args
        $buildExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = $previousErrors }
    if ($buildExit -ne 0) { throw 'Mock 실험 실패: local-notes와 Surefire 결과 확인' }
} finally {
    $env:BANS_MOCK_LOAD_TEST = $previousGate
    Pop-Location
}
