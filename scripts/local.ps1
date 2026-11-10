param(
    [ValidateSet('build','test','run')][string]$Action='test',
    [switch]$Offline,
    [string]$MavenRepository,
    [switch]$PromptAdminPassword
)
$ErrorActionPreference = 'Stop'
$modulePath = (Resolve-Path (Join-Path $PSScriptRoot '../bans_chatbot')).Path
Push-Location $modulePath
try {
    if ($Action -eq 'run') {
        if ($PromptAdminPassword) { throw 'Oracle 계정 비밀번호는 DB에서 관리합니다. -PromptAdminPassword를 제거하세요.' }
        & (Join-Path $PSScriptRoot 'oracle-demo.ps1') -Action run
        if ($LASTEXITCODE -ne 0) { throw 'ORACLE_DEMO_RUN_FAILED' }
    } else {
        $arguments=@();if($Offline){$arguments+='-o'}
        if($MavenRepository){$arguments+=('-Dmaven.repo.local='+$MavenRepository)}
        if($Action -eq 'build'){$arguments+='-Poracle'}
        $arguments+='clean';$arguments+='test';if($Action -eq 'build'){$arguments+='package'}
        $previousErrors=$ErrorActionPreference
        try { $ErrorActionPreference='Continue'; & .\mvnw.cmd @arguments; $buildExit=$LASTEXITCODE }
        finally { $ErrorActionPreference=$previousErrors }
        if($buildExit -ne 0){throw 'BUILD_OR_TEST_FAILED'}
    }
} finally { Pop-Location }
