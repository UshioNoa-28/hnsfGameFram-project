$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$sources = Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' | ForEach-Object FullName
if (-not $sources) { throw 'No Java source files found.' }

New-Item -ItemType Directory -Path 'bin\com\tedu\text' -Force | Out-Null
javac -encoding UTF-8 -d bin $sources
Copy-Item -LiteralPath 'src\com\tedu\text\GameData.pro','src\com\tedu\text\obj.pro','src\com\tedu\text\lev1.map','src\com\tedu\text\lev2.map','src\com\tedu\text\lev3.map' -Destination 'bin\com\tedu\text' -Force
java -cp bin com.tedu.game.GameSmokeTest | Select-Object -Last 4
Write-Host 'Build complete.' -ForegroundColor Green
