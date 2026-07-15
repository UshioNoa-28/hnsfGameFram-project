$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
& "$root\build-game.ps1"
java -cp bin com.tedu.game.GameStart
