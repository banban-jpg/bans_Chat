param(
    [ValidateSet('build','bootstrap','run')][string]$Action = 'build',
    [switch]$Offline,
    [string]$MavenRepository,
    [string]$ExpectedPdb = 'FREEPDB1'
)

$ErrorActionPreference = 'Stop'
$modulePath = (Resolve-Path (Join-Path $PSScriptRoot '../bans_chatbot')).Path
$jar = Join-Path $modulePath 'target/bans_chatbot-1.0.0.jar'
$localDb = Join-Path $modulePath '.env'

Push-Location $modulePath
try {
    if ($Action -eq 'build') {
        $arguments = @()
        if ($Offline) { $arguments += '-o' }
        if ($MavenRepository) { $arguments += ('-Dmaven.repo.local=' + $MavenRepository) }
        $arguments += '-Poracle', 'clean', 'test', 'package'
        & .\mvnw.cmd @arguments
        if ($LASTEXITCODE -ne 0) { throw 'ORACLE_DEMO_BUILD_FAILED' }
    } elseif ($Action -eq 'bootstrap') {
        if (-not (Test-Path -LiteralPath $jar)) { throw 'Build the Oracle-profile JAR first.' }
        if (-not (Test-Path -LiteralPath $localDb)) { throw '.env is missing.' }
        & java -jar $jar '--spring.profiles.active=oracle,oracle-bootstrap' `
            '--spring.main.web-application-type=none' '--BANS_ORACLE_FIRST_ADMIN_BOOTSTRAP=YES' `
            ('--BANS_ORACLE_BOOTSTRAP_PDB=' + $ExpectedPdb)
        if ($LASTEXITCODE -ne 0) { throw 'ORACLE_FIRST_ADMIN_BOOTSTRAP_FAILED' }
    } else {
        if (-not (Test-Path -LiteralPath $jar)) { throw 'Build the Oracle-profile JAR first.' }
        if (-not (Test-Path -LiteralPath $localDb)) { throw '.env is missing.' }
        $profiles = 'oracle'
        $localAi = $localDb
        if (Test-Path -LiteralPath $localAi) {
            $providerSettings = @(Get-Content -LiteralPath $localAi | Where-Object { $_ -match '^\s*bans\.ai\.provider\s*=' })
            if ($providerSettings.Count -gt 1) { throw 'Duplicate bans.ai.provider in .env.' }
            if ($providerSettings.Count -eq 1) {
                if ($providerSettings[0] -match '^\s*bans\.ai\.provider\s*=\s*gemini\s*$') { $profiles = 'oracle,gemini-live' }
                elseif ($providerSettings[0] -notmatch '^\s*bans\.ai\.provider\s*=\s*mock\s*$') { throw 'Invalid bans.ai.provider in .env.' }
            }
        }
        & java -jar $jar ('--spring.profiles.active=' + $profiles) '--server.address=127.0.0.1' '--server.port=8080' '--server.ssl.enabled=false'
        if ($LASTEXITCODE -ne 0) { throw 'ORACLE_DEMO_RUN_FAILED' }
    }
} finally {
    Pop-Location
}
