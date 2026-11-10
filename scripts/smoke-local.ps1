param([ValidatePattern('^[A-Za-z0-9._-]+$')][string]$EvidencePrefix='local-smoke')
$ErrorActionPreference='Stop'
$modulePath=(Resolve-Path (Join-Path $PSScriptRoot '../bans_chatbot')).Path
$notes=Join-Path $modulePath 'local-notes';New-Item -ItemType Directory -Path $notes -Force | Out-Null
$app=$null;$client=$null
try {
    $app=Start-Process -FilePath (Get-Command java).Source -WorkingDirectory $modulePath -WindowStyle Hidden -PassThru `
        -ArgumentList @('-jar','target/bans_chatbot-1.0.0.jar','--spring.profiles.active=test','--bans.ai.provider=mock','--server.address=127.0.0.1','--server.port=0') `
        -RedirectStandardOutput (Join-Path $notes ($EvidencePrefix+'-app.log')) -RedirectStandardError (Join-Path $notes ($EvidencePrefix+'-error.log'))
    $timer=[Diagnostics.Stopwatch]::StartNew();$port=$null
    while($timer.Elapsed.TotalSeconds -lt 30){
        if($app.HasExited){throw 'MOCK_STARTUP_FAILED'}
        $startup=Get-Content (Join-Path $notes ($EvidencePrefix+'-app.log')) -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
        if($startup){$portMatch=[regex]::Match($startup,'Tomcat started on port (\d+)');if($portMatch.Success){$port=[int]$portMatch.Groups[1].Value;break}}
        Start-Sleep -Milliseconds 200
    }
    if(!$port){throw 'MOCK_STARTUP_TIMEOUT'}
    Add-Type -AssemblyName System.Net.Http
    $handler=[Net.Http.HttpClientHandler]::new();$handler.UseProxy=$false
    $client=[Net.Http.HttpClient]::new($handler);$client.Timeout=[TimeSpan]::FromSeconds(5)
    foreach($route in @('/','/health','/api/health','/api/auth/csrf','/ui/app.js','/api/chat/rooms')){
        $response=$client.GetAsync(('http://127.0.0.1:'+$port+$route)).GetAwaiter().GetResult()
        $expected=if($route -eq '/api/chat/rooms'){401}else{200}
        if([int]$response.StatusCode -ne $expected){throw 'SMOKE_STATUS_FAILED'}
        if($route -eq '/' -and !$response.Content.ReadAsStringAsync().GetAwaiter().GetResult().Contains('가상 데이터 / 영구 저장 아님')){throw 'MOCK_BANNER_MISSING'}
        Write-Output ('PASS GET '+$route+' -> '+$expected);$response.Dispose()
    }
    Write-Output 'MOCK_SMOKE_PASS realOracle=0 realGemini=0'
} finally {
    if($client){$client.Dispose()}
    if($app -and !$app.HasExited){Stop-Process -Id $app.Id -ErrorAction SilentlyContinue;[void]$app.WaitForExit(5000)}
}
