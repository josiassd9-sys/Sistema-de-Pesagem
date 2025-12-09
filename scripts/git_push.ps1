<#
Script: scripts/git_push.ps1
Descrição: Automatiza os passos comuns para preparar, commitar e enviar um projeto Android para um repositório remoto GitHub.
Uso seguro: revise o script antes de executar. Não envia credenciais: será usado o helper de credenciais do Git ou GitHub CLI.

Parâmetros:
  -Remote   (string) remote name (default: origin)
  -Branch   (string) branch destino (default: main)
  -Message  (string) mensagem de commit padrão
  -DryRun   (switch) simula as ações (não executa git commit/push)
  -Force    (switch) força alguns passos que pedem confirmação

Exemplos:
  # Simular as ações
  powershell -ExecutionPolicy Bypass -File .\scripts\git_push.ps1 -DryRun

  # Commit e push para main com mensagem padrão
  powershell -ExecutionPolicy Bypass -File .\scripts\git_push.ps1

  # Commit com mensagem personalizada e branch master
  powershell -ExecutionPolicy Bypass -File .\scripts\git_push.ps1 -Message "Meu commit" -Branch master
#>

param(
    [string]$Remote = "origin",
    [string]$Branch = "main",
    [string]$Message = "Initial project import: Gradle setup, Compose support, Room, TCP scale listener, README and .gitignore",
    [switch]$DryRun,
    [switch]$Force
)

function Run-Git {
    param([string]$Args)
    Write-Host "git $Args"
    if (-not $DryRun) {
        & git $Args
        if ($LASTEXITCODE -ne 0) {
            throw "git command failed: git $Args"
        }
    }
}

try {
    Write-Host "[INFO] Starting git push helper script"

    # Verifica se o git está disponível
    try {
        & git --version > $null 2>&1
    } catch {
        throw "Git não encontrado no PATH. Instale o Git e tente novamente."
    }

    # Verifica se estamos dentro de um repositório git
    $isRepo = (& git rev-parse --is-inside-work-tree 2>$null)
    if ($isRepo -ne "true") {
        throw "Este diretório não parece ser um repositório Git. Execute a partir da raiz do projeto."
    }

    # Exibe configurações de usuário (e oferece setar se ausentes)
    $userName = (& git config user.name) -join ""
    $userEmail = (& git config user.email) -join ""

    if ([string]::IsNullOrWhiteSpace($userName) -or [string]::IsNullOrWhiteSpace($userEmail)) {
        Write-Host "Configurações de usuário Git não estão definidas (user.name/user.email)." -ForegroundColor Yellow
        if (-not $DryRun) {
            $yn = Read-Host "Deseja configurar agora? (s/N)"
            if ($yn -in @('s','S','y','Y')) {
                $n = Read-Host "Digite seu nome (ex: Josias Silva)"
                $e = Read-Host "Digite seu email (ex: voce@exemplo.com)"
                if (-not [string]::IsNullOrWhiteSpace($n)) { & git config --global user.name "$n" }
                if (-not [string]::IsNullOrWhiteSpace($e)) { & git config --global user.email "$e" }
                Write-Host "Configurado: $n <$e>"
            } else {
                Write-Host "Continuando sem configurar user.name/user.email. O commit pode falhar se não estiver configurado."
            }
        }
    } else {
        Write-Host "Git user: $userName <$userEmail>"
    }

    # Checa se remote existe
    $remotes = (& git remote) -join " `n"
    if (-not $remotes.Contains($Remote)) {
        Write-Host "Remote '$Remote' não existe. Vou adicionar o remote que você especificar." -ForegroundColor Yellow
        if (-not $DryRun) {
            $url = Read-Host "Informe a URL do remote (ex: https://github.com/usuario/repo.git)"
            if ([string]::IsNullOrWhiteSpace($url)) { throw "URL do remote não informada." }
            Run-Git "remote add $Remote $url"
        } else {
            Write-Host "[DryRun] Skipping remote add (would add remote '$Remote')"
        }
    }

    # Remove do índice arquivos que devem ser ignorados (sem apagar localmente)
    Write-Host "[INFO] Limpando do índice arquivos que devem ser ignorados (.gradle, build, gradle/wrapper/dists)"
    if (-not $DryRun) {
        & git rm -r --cached .gradle 2>$null | Out-Null
        & git rm -r --cached gradle/wrapper/dists 2>$null | Out-Null
        # Remover caches de build (padrão) - usar --ignore-unmatch para não causar erro se não existirem
        & git rm -r --cached **/build 2>$null | Out-Null
    } else {
        Write-Host "[DryRun] git rm --cached .gradle"
        Write-Host "[DryRun] git rm --cached gradle/wrapper/dists"
        Write-Host "[DryRun] git rm --cached **/build"
    }

    # Mostrar status
    Write-Host "[INFO] Git status antes do add:"
    if (-not $DryRun) { & git status --short }

    # Adicionar tudo
    Run-Git "add ."

    Write-Host "[INFO] Git status após add:"
    if (-not $DryRun) { & git status --short }

    # Commit
    $hasChanges = (& git status --porcelain) -ne ""
    if (-not $hasChanges) {
        Write-Host "Nenhuma alteração a commitar." -ForegroundColor Yellow
    } else {
        if ($DryRun) {
            Write-Host "[DryRun] git commit -m \"$Message\""
        } else {
            Run-Git "commit -m \"$Message\""
        }
    }

    # Define branch principal
    Write-Host "[INFO] Definindo branch principal para '$Branch'"
    if ($DryRun) {
        Write-Host "[DryRun] git branch -M $Branch"
    } else {
        Run-Git "branch -M $Branch"
    }

    # Push
    Write-Host "[INFO] Enviando para $Remote/$Branch"
    if ($DryRun) {
        Write-Host "[DryRun] git push -u $Remote $Branch"
    } else {
        # Tenta push; se falhar por autenticação, orienta o usuário
        try {
            Run-Git "push -u $Remote $Branch"
            Write-Host "Push realizado com sucesso." -ForegroundColor Green
        } catch {
            Write-Host "Erro ao fazer push: $_" -ForegroundColor Red
            Write-Host "Se for erro de autenticação, execute: gh auth login  (ou configure um PAT nas credenciais)" -ForegroundColor Yellow
            throw $_
        }
    }

    Write-Host "[DONE] Script finalizado. Verifique no GitHub se os arquivos foram enviados." -ForegroundColor Green
}
catch {
    Write-Error "Erro durante execução: $_"
    exit 1
}

