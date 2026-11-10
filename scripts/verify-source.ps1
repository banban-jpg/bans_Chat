$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $repoRoot
try {
    # 현재 추적 파일과 새 커밋 후보를 검사한다. 매치 원문/비밀값은 출력하지 않는다.
    $paths = @(& git ls-files --cached --others --exclude-standard | Sort-Object -Unique)
    if ($LASTEXITCODE -ne 0) { throw 'GIT_INVENTORY_FAILED' }
    $findings = @(); $entries = @(); $textFiles = 0
    $rules = @('AIza[0-9A-Za-z_-]{35}', 'gh[pousr]_[0-9A-Za-z]{30,}', '-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----')
    foreach ($path in $paths) {
        if (!(Test-Path -LiteralPath $path -PathType Leaf)) { continue }
        $resolved = (Resolve-Path -LiteralPath $path).Path
        if (!$resolved.StartsWith($repoRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'PATH_OUTSIDE_REPOSITORY' }
        $entries += [pscustomobject]@{path=$path;sha256=(Get-FileHash -LiteralPath $resolved -Algorithm SHA256).Hash}
        if ($path -match '(\.java|\.xml|\.properties|\.md|\.txt|\.py|\.mjs|\.js|\.css|\.html|\.ps1|\.cmd|\.sql|\.psv|\.yml|\.yaml|\.json)$' -or $path -eq '.env.example') {
            $text = [IO.File]::ReadAllText($resolved); $textFiles++
            foreach ($rule in $rules) { if ([regex]::IsMatch($text,$rule)) { $findings += $path; break } }
            if ($path.EndsWith('.md')) {
                foreach ($link in [regex]::Matches($text,'\]\(([^)]+)\)')) {
                    $target=$link.Groups[1].Value.Trim('<','>')
                    if ($target -match '^(https?://|mailto:|#)' -or $target -match '^[A-Za-z]:') { continue }
                    $target=($target -split '#',2)[0]
                    if ($target -and !(Test-Path -LiteralPath (Join-Path (Split-Path $resolved) $target))) { $findings += $path }
                }
            }
            if ($path -match '(^|/)(\.env($|\.)|application-local\.(properties|ya?ml)$)' -and $path -ne '.env.example') { $findings += $path }
        }
    }
    foreach ($line in [IO.File]::ReadAllLines((Join-Path $repoRoot '.env.example'))) {
        if ($line -match '^\s*[A-Z][A-Z0-9_]*=(.*)$' -and $Matches[1].Trim() -ne '') { $findings += '.env.example' }
    }
    if ($findings.Count) { $findings | Sort-Object -Unique | ForEach-Object { Write-Output ('SOURCE_REVIEW_REQUIRED path='+$_) }; throw 'SOURCE_CHECK_FAILED' }
    # 현재 HEAD와 index도 검사한다. 전체 과거 이력/고엔트로피 탐지는 별도 작업이다.
    foreach ($tree in @('HEAD','INDEX')) {
        $arguments=@('grep','-I','-l','-E','-e','AIza[0-9A-Za-z_-]{35}','-e','gh[pousr]_[0-9A-Za-z]{30,}','-e','-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----')
        if ($tree -eq 'INDEX') { $arguments=@('grep','--cached')+$arguments[1..($arguments.Count-1)] } else { $arguments+='HEAD' }
        $hits=@(& git @arguments);$gitExit=$LASTEXITCODE
        if($gitExit -eq 0){$hits | ForEach-Object {Write-Output ('SECRET_REVIEW_REQUIRED file='+$_)};throw 'GIT_SECRET_PATTERN_FOUND'}
        if($gitExit -ne 1){throw 'GIT_SECRET_CHECK_FAILED'}
    }
    $notes = Join-Path $repoRoot 'bans_chatbot/local-notes'; New-Item -ItemType Directory -Path $notes -Force | Out-Null
    [pscustomobject]@{time=(Get-Date -Format o);files=$entries.Count;textFiles=$textFiles;result='PASS';scope='working tree tracked and eligible untracked; bounded patterns, not secret-free certification';inventory=$entries} |
        ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $notes 'step18-source-inventory.json') -Encoding UTF8
    Write-Output ('SOURCE_CHECK_PASS files='+$entries.Count+' textFiles='+$textFiles+' findings=0')
} finally { Pop-Location }
