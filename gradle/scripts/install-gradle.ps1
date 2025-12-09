<#
install-gradle.ps1
Script para baixar e instalar manualmente uma distribuição Gradle no diretório do wrapper do usuário.
Uso: execute a partir da raiz do projeto ou salve e execute com PowerShell:
  powershell -ExecutionPolicy Bypass -File .\gradle\scripts\install-gradle.ps1
#>

param(
    [string]$Version = "8.14.3",
    [int]$TimeoutSeconds = 1800,
    [switch]$DryRun
)

try {
    $distFileName = "gradle-$Version-bin.zip"
    $distUrl = "https://services.gradle.org/distributions/$distFileName"
    $gradleUserHome = Join-Path -Path $env:USERPROFILE -ChildPath ".gradle"
    $targetBase = Join-Path -Path $gradleUserHome -ChildPath ("wrapper\dists\gradle-$Version-bin")

    Write-Host "Gradle version: $Version"
    Write-Host "Target base: $targetBase"

    # Create target directory
    if (-not (Test-Path -Path $targetBase)) {
        if ($DryRun) {
            Write-Host "[DryRun] Criaria diretório: $targetBase"
        } else {
            New-Item -ItemType Directory -Force -Path $targetBase | Out-Null
        }
    }

    $zipPath = Join-Path -Path $targetBase -ChildPath $distFileName

    # Download if not exists or user wants to refresh
    $download = $true
    if (Test-Path -Path $zipPath) {
        Write-Host "O arquivo $zipPath já existe." -ForegroundColor Yellow
        if ($DryRun) {
            Write-Host "[DryRun] Perguntaria ao usuário se deseja rebaixar e substituir."
            $resp = 'N'
        } else {
            $resp = Read-Host "Deseja rebaixar e substituir? (s/N)"
        }
        if ($resp -notin @('s','S','y','Y')) { $download = $false }
    }

    if ($download) {
        Write-Host "Baixando $distUrl ..."
        if ($DryRun) {
            Write-Host "[DryRun] Iriamos baixar: $distUrl -> $zipPath"
        } else {
            # UseBasicParsing for PowerShell 5.1
            Invoke-WebRequest -Uri $distUrl -OutFile $zipPath -UseBasicParsing -TimeoutSec $TimeoutSeconds
            Write-Host "Download concluído: $zipPath" -ForegroundColor Green
        }
    }

    # Unzip
    Write-Host "Descompactando $zipPath em $targetBase ..."
    if ($DryRun) {
        Write-Host "[DryRun] Iriamos descompactar $zipPath em $targetBase"
    } else {
        Expand-Archive -LiteralPath $zipPath -DestinationPath $targetBase -Force
    }

    # Verificar se a estrutura foi criada
    $children = Get-ChildItem -Path $targetBase -Directory -ErrorAction SilentlyContinue
    if ($children.Count -eq 0) {
        Write-Host "Aviso: nenhum diretório foi encontrado em $targetBase após a extração." -ForegroundColor Yellow
        Write-Host "Verifique manualmente se o ZIP foi baixado e extraído corretamente." -ForegroundColor Yellow
    } else {
        # Use ${} para garantir que PowerShell não tente incluir caracteres após o nome da variável
        Write-Host "Distribuição Gradle instalada com sucesso. Subpastas em ${targetBase}:" -ForegroundColor Green
        $children | ForEach-Object { Write-Host " - " $_.FullName }
    }

    Write-Host "Próximo passo: execute no seu projeto: .\gradlew.bat --no-daemon :app:assembleDebug --stacktrace" -ForegroundColor Cyan
    exit 0
}
catch {
    Write-Error "Ocorreu um erro: $_"
    exit 1
}
