Reference
---------

- [Groovy](https://groovy-lang.org/)
- [GDK](https://groovy-lang.org/gdk.html)

Install script
--------------

```ps1
<#
.SYNOPSIS
    PowerShell script to install Groovy on Windows.
.DESCRIPTION
    Downloads and installs Groovy, then updates the system PATH.
.NOTES
    Requires PowerShell 5.1+ and Internet access.
#>

# 1. 设置 Groovy 版本和下载 URL
$GroovyVersion = "4.0.21"  # 可替换为最新版本（检查 https://groovy-lang.org/download.html）
$GroovyUrl = "https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-binary-$GroovyVersion.zip"
$TempDir = "$env:TEMP\groovy-install"
$InstallDir = "$env:ProgramFiles\Groovy"

# 2. 检查是否已安装 Groovy
if (Test-Path "$InstallDir") {
    Write-Host "⚠️ Groovy 似乎已经安装在 $InstallDir。跳过安装。" -ForegroundColor Yellow
    exit
}

# 3. 创建临时目录并下载 Groovy
Write-Host "📥 下载 Groovy $GroovyVersion..." -ForegroundColor Cyan
New-Item -ItemType Directory -Path $TempDir -Force | Out-Null
Invoke-WebRequest -Uri $GroovyUrl -OutFile "$TempDir\groovy.zip"

# 4. 解压并安装到 Program Files
Write-Host "📂 解压并安装 Groovy 到 $InstallDir..." -ForegroundColor Cyan
Expand-Archive -Path "$TempDir\groovy.zip" -DestinationPath $TempDir
Move-Item -Path "$TempDir\groovy-$GroovyVersion" -Destination $InstallDir -Force

# 5. 设置环境变量
Write-Host "⚙️ 配置环境变量..." -ForegroundColor Cyan
$GroovyPath = "$InstallDir\bin"
$CurrentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")

if ($CurrentPath -notlike "*$GroovyPath*") {
    [Environment]::SetEnvironmentVariable("Path", "$CurrentPath;$GroovyPath", "Machine")
    Write-Host "✅ 已添加 Groovy 到 PATH。" -ForegroundColor Green
} else {
    Write-Host "ℹ️ Groovy 已经在 PATH 中。" -ForegroundColor Blue
}

# 6. 清理临时文件
Remove-Item -Path $TempDir -Recurse -Force

# 7. 验证安装
Write-Host "🎉 Groovy $GroovyVersion 安装完成！" -ForegroundColor Green
Write-Host "请重新打开终端并运行 `groovy --version` 测试。" -ForegroundColor Yellow
```