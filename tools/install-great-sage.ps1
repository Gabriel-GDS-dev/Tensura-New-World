param(
    [string]$InstancePath,
    [switch]$DevelopmentOnly
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$fileName = "greatsage-0.0.6.1.jar"
$downloadUrl = "https://mediafilez.forgecdn.net/files/8931/858/greatsage-0.0.6.1.jar"
$expectedSha256 = "70d921688e106fa03c00fe0cad2a31e7a695847b8b5353d677d913b4830744b7"
$developmentTarget = Join-Path $root "libs\$fileName"

function Install-VerifiedFile([string]$target) {
    $parent = Split-Path -Parent $target
    New-Item -ItemType Directory -Path $parent -Force | Out-Null
    if (-not (Test-Path -LiteralPath $target)) {
        Write-Host "Downloading Great Sage 0.0.6.1 from CurseForge..."
        Invoke-WebRequest -Uri $downloadUrl -OutFile $target
    }
    $actual = (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actual -ne $expectedSha256) {
        Remove-Item -LiteralPath $target -Force
        throw "Great Sage checksum mismatch. The downloaded file was removed."
    }
    Write-Host "Verified: $target"
}

Install-VerifiedFile $developmentTarget

if (-not $DevelopmentOnly) {
    if ([string]::IsNullOrWhiteSpace($InstancePath)) {
        throw "Pass -InstancePath with the Modrinth instance folder, or use -DevelopmentOnly."
    }
    $resolvedInstance = (Resolve-Path -LiteralPath $InstancePath).Path
    $modsFolder = Join-Path $resolvedInstance "mods"
    $instanceTarget = Join-Path $modsFolder $fileName
    New-Item -ItemType Directory -Path $modsFolder -Force | Out-Null
    Copy-Item -LiteralPath $developmentTarget -Destination $instanceTarget -Force
    Write-Host "Installed Great Sage in: $instanceTarget"
}
