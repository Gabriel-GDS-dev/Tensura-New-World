param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$version = (Select-String -LiteralPath (Join-Path $root 'gradle.properties') -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value
$dist = Join-Path $root "dist"
$stage = Join-Path $dist "pack-stage"
$archive = Join-Path $dist "Tensura-New-World-$version.zip"
$output = Join-Path $dist "Tensura-New-World-$version.mrpack"

if (-not $SkipBuild) {
    & (Join-Path $root "gradlew.bat") build --no-configuration-cache
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }
}

$coreJar = Get-ChildItem (Join-Path $root "build\libs\tensuranewworld-$version.jar") |
    Where-Object { $_.Name -notmatch "-(sources|javadoc)\.jar$" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if (-not $coreJar) {
    throw "Core JAR not found. Run .\gradlew.bat build first."
}

$mods = @(
    @{ File = "tensura-neoforge-2.0.1.3.jar"; Sha1 = "b2bb4a9cd022765b8c4542c1f049a7c57d8526b1"; Url = "https://cdn.modrinth.com/data/Br0kXPwc/versions/LttkLzXM/tensura-neoforge-2.0.1.3.jar" },
    @{ File = "manascore-neoforge-4.0.0.2.jar"; Sha1 = "f11a0062d7829e26a8705183762a5e0f2d022dd3"; Url = "https://cdn.modrinth.com/data/wRRSWpd1/versions/yhLQMaxS/manascore-neoforge-4.0.0.2.jar" },
    @{ File = "architectury-13.0.11-neoforge.jar"; Sha1 = "008656a0702801174b8ec245ed7aad1921d6e9f1"; Url = "https://cdn.modrinth.com/data/lhGA9TYQ/versions/1IiqEQGl/architectury-13.0.11-neoforge.jar" },
    @{ File = "geckolib-neoforge-1.21.1-4.9.3.jar"; Sha1 = "5cfe835472a5c5457ae9ac192d9d7b317454d5ac"; Url = "https://cdn.modrinth.com/data/8BmcQJ2H/versions/Grwn5rUB/geckolib-neoforge-1.21.1-4.9.3.jar" },
    @{ File = "SmartBrainLib-neoforge-1.21.1-1.16.11.jar"; Sha1 = "0310135a01eeceefbc7f1ab017498a65f3ad6836"; Url = "https://cdn.modrinth.com/data/PuyPazRT/versions/O5EpeqI3/SmartBrainLib-neoforge-1.21.1-1.16.11.jar" },
    @{ File = "TerraBlender-neoforge-1.21.1-4.1.0.8.jar"; Sha1 = "744394d979b422be560babf9df7a6800ede9ac5b"; Url = "https://cdn.modrinth.com/data/kkmrDlKT/versions/6e8GCrLb/TerraBlender-neoforge-1.21.1-4.1.0.8.jar" }
)

$files = foreach ($mod in $mods) {
    $local = Join-Path $root ("libs\" + $mod.File)
    if (-not (Test-Path -LiteralPath $local)) {
        throw "Missing dependency: $($mod.File)"
    }
    $sha1 = (Get-FileHash -LiteralPath $local -Algorithm SHA1).Hash.ToLowerInvariant()
    if ($sha1 -ne $mod.Sha1) {
        throw "SHA1 mismatch for $($mod.File): expected $($mod.Sha1), got $sha1"
    }
    [ordered]@{
        path = "mods/$($mod.File)"
        hashes = [ordered]@{
            sha1 = $sha1
            sha512 = (Get-FileHash -LiteralPath $local -Algorithm SHA512).Hash.ToLowerInvariant()
        }
        env = [ordered]@{ client = "required"; server = "required" }
        downloads = @($mod.Url)
        fileSize = (Get-Item -LiteralPath $local).Length
    }
}

foreach ($generatedPath in @($stage, $archive, $output)) {
    $absoluteGenerated = [System.IO.Path]::GetFullPath($generatedPath)
    $absoluteDist = [System.IO.Path]::GetFullPath($dist).TrimEnd('\') + '\'
    if (-not $absoluteGenerated.StartsWith($absoluteDist, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Generated path is outside dist: $absoluteGenerated"
    }
}
if (Test-Path -LiteralPath $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
if (Test-Path -LiteralPath $archive) { Remove-Item -LiteralPath $archive -Force }
if (Test-Path -LiteralPath $output) { Remove-Item -LiteralPath $output -Force }
New-Item -ItemType Directory -Path (Join-Path $stage "overrides\mods") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $stage "overrides\config\tensura\client") -Force | Out-Null
Copy-Item -LiteralPath $coreJar.FullName -Destination (Join-Path $stage "overrides\mods\$($coreJar.Name)")
Copy-Item -LiteralPath (Join-Path $root "CREDITS.md") -Destination (Join-Path $stage "overrides\TENSURA_NEW_WORLD_CREDITS.md")
Copy-Item -LiteralPath (Join-Path $root "docs\GREAT_SAGE.md") -Destination (Join-Path $stage "overrides\TENSURA_NEW_WORLD_GRANDE_SABIO.md")
Copy-Item -LiteralPath (Join-Path $root "docs\MORPH.md") -Destination (Join-Path $stage "overrides\TENSURA_NEW_WORLD_MORPH.md")
Copy-Item -LiteralPath (Join-Path $root "docs\RELEASE_0.3.0.md") -Destination (Join-Path $stage "overrides\TENSURA_NEW_WORLD_0.3.0.md")
Copy-Item -LiteralPath (Join-Path $root "docs\MENUS.md") -Destination (Join-Path $stage "overrides\TENSURA_NEW_WORLD_MENUS.md")
Copy-Item -LiteralPath (Join-Path $root "docs\RELEASE_0.3.1.md") -Destination (Join-Path $stage "overrides\TENSURA_NEW_WORLD_0.3.1.md")
Copy-Item -LiteralPath (Join-Path $root "overrides\config\tensura\client\hud_config.toml") `
    -Destination (Join-Path $stage "overrides\config\tensura\client\hud_config.toml")

$index = [ordered]@{
    formatVersion = 1
    game = "minecraft"
    versionId = $version
    name = "Tensura - New World"
    summary = "Progressao Tensura unificada: EP e Essencia por abates, racas, atributos, mimetizacao Slime e evolucao no Raphael."
    files = @($files)
    dependencies = [ordered]@{
        minecraft = "1.21.1"
        neoforge = "21.1.252"
    }
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText(
    (Join-Path $stage "modrinth.index.json"),
    ($index | ConvertTo-Json -Depth 8),
    $utf8NoBom)
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archiveStream = [System.IO.File]::Open($archive, [System.IO.FileMode]::Create)
$zip = New-Object System.IO.Compression.ZipArchive(
    $archiveStream,
    [System.IO.Compression.ZipArchiveMode]::Create)
try {
    Get-ChildItem -LiteralPath $stage -Recurse -File | ForEach-Object {
        $entryName = $_.FullName.Substring($stage.Length + 1).Replace("\", "/")
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
            $zip,
            $_.FullName,
            $entryName,
            [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
}
finally {
    $zip.Dispose()
    $archiveStream.Dispose()
}
Move-Item -LiteralPath $archive -Destination $output
Remove-Item -LiteralPath $stage -Recurse -Force

Write-Host "Created $output"
& (Join-Path $PSScriptRoot "validate-release.ps1") -PackPath $output
