param(
    [string]$PackPath,
    [string]$PackVersion
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$version = (Select-String -LiteralPath (Join-Path $root 'gradle.properties') -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value
if (-not $PackVersion) { $PackVersion = "$version-full.1" }
if ([string]::IsNullOrWhiteSpace($PackPath)) {
    $PackPath = Join-Path $root "dist\Tensura-New-World-$PackVersion.mrpack"
}
if (-not (Test-Path -LiteralPath $PackPath)) {
    throw "Pack not found: $PackPath"
}

function Assert-Release([bool]$condition, [string]$message) {
    if (-not $condition) {
        throw "Release validation failed: $message"
    }
}

function Read-ZipText($entry) {
    $entryStream = $entry.Open()
    $reader = New-Object System.IO.StreamReader($entryStream, [System.Text.Encoding]::UTF8, $true)
    try {
        return $reader.ReadToEnd()
    }
    finally {
        $reader.Dispose()
        $entryStream.Dispose()
    }
}

$expectedDownloads = @(
    "tensura-neoforge-2.0.1.3.jar",
    "manascore-neoforge-4.0.0.2.jar",
    "architectury-13.0.11-neoforge.jar",
    "geckolib-neoforge-1.21.1-4.9.3.jar",
    "SmartBrainLib-neoforge-1.21.1-1.16.11.jar",
    "TerraBlender-neoforge-1.21.1-4.1.0.8.jar"
)
$requiredCoreEntries = @(
    "com/gabriel/tensuranewworld/client/TensuraTitleScreen.class",
    "com/gabriel/tensuranewworld/client/RaphaelScreen.class",
    "com/gabriel/tensuranewworld/client/RaphaelSkillPanel.class",
    "com/gabriel/tensuranewworld/client/ReincarnatedSkillControls.class",
    "com/gabriel/tensuranewworld/client/ArchitecturySkillTransport.class",
    "com/gabriel/tensuranewworld/integration/ReincarnatedSkills.class",
    "com/gabriel/tensuranewworld/data/SkillView.class",
    "com/gabriel/tensuranewworld/registry/SkillCategory.class",
    "com/gabriel/tensuranewworld/registry/SkillAvailability.class",
    "com/gabriel/tensuranewworld/data/MimicForm.class",
    "com/gabriel/tensuranewworld/data/BodyProfile.class",
    "com/gabriel/tensuranewworld/gameplay/BodyPhysiology.class",
    "com/gabriel/tensuranewworld/gameplay/BodyRules.class",
    "com/gabriel/tensuranewworld/integration/MorphBridge.class",
    "com/gabriel/tensuranewworld/client/SurvivalHud.class",
    "com/gabriel/tensuranewworld/client/FormCamera.class",
    "com/gabriel/tensuranewworld/client/RpgTheme.class",
    "com/gabriel/tensuranewworld/client/MenuGuide.class",
    "com/gabriel/tensuranewworld/mixin/FormCameraMixin.class",
    "com/gabriel/tensuranewworld/mixin/RpgButtonMixin.class",
    "com/gabriel/tensuranewworld/network/SyncMimicFormPayload.class",
    "com/gabriel/tensuranewworld/registry/EvolutionRegistry.class",
    "assets/tensuranewworld/textures/gui/title_background.png",
    "META-INF/neoforge.mods.toml"
)

Add-Type -AssemblyName System.IO.Compression
$resolvedPack = (Resolve-Path -LiteralPath $PackPath).Path
$packStream = [System.IO.File]::OpenRead($resolvedPack)
$pack = New-Object System.IO.Compression.ZipArchive(
    $packStream,
    [System.IO.Compression.ZipArchiveMode]::Read)
try {
    $indexEntry = $pack.GetEntry("modrinth.index.json")
    Assert-Release ($null -ne $indexEntry) "modrinth.index.json is missing"
    $index = (Read-ZipText $indexEntry) | ConvertFrom-Json

    Assert-Release ($index.formatVersion -eq 1) "unsupported formatVersion"
    Assert-Release ($index.game -eq "minecraft") "game must be minecraft"
    Assert-Release ($index.versionId -eq $PackVersion) "versionId must be $PackVersion"
    Assert-Release ($index.dependencies.minecraft -eq "1.21.1") "Minecraft must be 1.21.1"
    Assert-Release ($index.dependencies.neoforge -eq "21.1.252") "NeoForge must be 21.1.252"
    $extraFiles = @(Get-Content -LiteralPath (Join-Path $PSScriptRoot 'modrinth-extra-files.json') -Raw | ConvertFrom-Json | ForEach-Object { $_ })
    $expectedDownloads += @($extraFiles | ForEach-Object { Split-Path -Leaf $_.path })
    Assert-Release (@($index.files).Count -eq $expectedDownloads.Count) "incorrect dependency count"

    foreach ($file in @($index.files)) {
        $name = Split-Path -Leaf $file.path
        Assert-Release ($expectedDownloads -contains $name) "unexpected dependency $name"
        $expectedPath = if ($name.EndsWith('.zip')) { "shaderpacks/$name" } else { "mods/$name" }
        Assert-Release ($file.path -eq $expectedPath) "invalid dependency path for $name"
        $expectedServer = if ($name.EndsWith('.zip') -or $name -match '^(iris-|sodium-)') { 'unsupported' } else { 'required' }
        Assert-Release ($file.env.client -eq 'required' -and $file.env.server -eq $expectedServer) "invalid environment for $name"
        Assert-Release (-not [string]::IsNullOrWhiteSpace($file.hashes.sha1)) "missing SHA-1 for $name"
        Assert-Release (-not [string]::IsNullOrWhiteSpace($file.hashes.sha512)) "missing SHA-512 for $name"
        Assert-Release (@($file.downloads).Count -eq 1) "exactly one download is required for $name"
        Assert-Release ($file.downloads[0].StartsWith("https://cdn.modrinth.com/")) "non-Modrinth download for $name"
    }
    foreach ($expected in $extraFiles) {
        $actual = @($index.files | Where-Object { $_.path -eq $expected.path })
        Assert-Release ($actual.Count -eq 1) "missing or duplicate extra content $($expected.path)"
        Assert-Release ($actual[0].hashes.sha1 -eq $expected.hashes.sha1 -and $actual[0].hashes.sha512 -eq $expected.hashes.sha512 -and $actual[0].downloads[0] -eq $expected.downloads[0] -and $actual[0].fileSize -eq $expected.fileSize) "extra content differs from pinned Modrinth file $($expected.path)"
    }
    $irisConfig = $pack.GetEntry('client-overrides/config/iris.properties')
    Assert-Release ($null -ne $irisConfig) 'default shader config missing'
    $irisText = Read-ZipText $irisConfig
    Assert-Release ($irisText -match '(?m)^enableShaders=true\s*$' -and $irisText -match '(?m)^shaderPack=ComplementaryUnbound_r5.9.3.zip\s*$') 'Complementary must be enabled by default'

    $coreEntries = @($pack.Entries | Where-Object {
        $_.FullName -like "overrides/mods/tensuranewworld-*.jar"
    })
    Assert-Release ($coreEntries.Count -eq 1) "the pack must contain exactly one New World core JAR"
    Assert-Release ($null -ne $pack.GetEntry("overrides/TENSURA_NEW_WORLD_CREDITS.md")) "credits are missing"
    Assert-Release ($null -ne $pack.GetEntry("overrides/TENSURA_NEW_WORLD_GRANDE_SABIO.md")) "Great Sage instructions are missing"
    Assert-Release ($null -ne $pack.GetEntry("overrides/TENSURA_NEW_WORLD_MENUS.md")) "menu guide is missing"
    $hudConfigEntry = $pack.GetEntry("overrides/config/tensura/client/hud_config.toml")
    Assert-Release ($null -ne $hudConfigEntry) "the single-progression HUD config is missing"
    $hudConfig = Read-ZipText $hudConfigEntry
    Assert-Release ($hudConfig -match "(?m)^tensuraHud\s*=\s*false\s*$") "Tensura's competing HUD must be disabled"
    Assert-Release ($hudConfig -match "(?m)^vanillaHud\s*=\s*true\s*$") "vanilla HUD must be enabled for New World's selective replacement"
    Assert-Release (@($pack.Entries | Where-Object {
        $_.FullName -match "(?i)greatsage.*\.jar$"
    }).Count -eq 0) "Great Sage cannot be embedded in the public pack"

    $coreMemory = New-Object System.IO.MemoryStream
    $coreStream = $coreEntries[0].Open()
    try {
        $coreStream.CopyTo($coreMemory)
    }
    finally {
        $coreStream.Dispose()
    }
    $coreMemory.Position = 0
    $coreJar = New-Object System.IO.Compression.ZipArchive(
        $coreMemory,
        [System.IO.Compression.ZipArchiveMode]::Read)
    try {
        foreach ($entryName in $requiredCoreEntries) {
            Assert-Release ($null -ne $coreJar.GetEntry($entryName)) "core JAR is missing $entryName"
        }
        Assert-Release ($null -eq $coreJar.GetEntry("com/gabriel/tensuranewworld/gameplay/BodyGameTests.class")) "GameTests must not be shipped in the core"
        $metadata = Read-ZipText $coreJar.GetEntry("META-INF/neoforge.mods.toml")
        Assert-Release ($metadata.Contains($version)) "core metadata does not contain version $version"
    }
    finally {
        $coreJar.Dispose()
        $coreMemory.Dispose()
    }
}
finally {
    $pack.Dispose()
    $packStream.Dispose()
}

$hash = (Get-FileHash -LiteralPath $resolvedPack -Algorithm SHA256).Hash.ToLowerInvariant()
$size = (Get-Item -LiteralPath $resolvedPack).Length
Write-Host "Release validated: $resolvedPack"
Write-Host "Size: $size bytes"
Write-Host "SHA-256: $hash"
