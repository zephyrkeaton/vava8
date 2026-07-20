# Push this folder to https://github.com/zephyrkeaton/vava8
# Usage (PowerShell as your logged-in GitHub user with push access):
#   cd C:\tmp\1\vava8
#   .\PUSH_TO_GITHUB.ps1

$ErrorActionPreference = "Stop"
$repo = "https://github.com/zephyrkeaton/vava8.git"
$branch = "main"

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
  Write-Error "git not found. Install Git for Windows first."
}

if (-not (Test-Path .git)) {
  git init
  git checkout -b $branch
  git add .
  git commit -m "Initial release: Vava8 1.0.0"
}

$remote = git remote 2>$null
if ($remote -notcontains "origin") {
  git remote add origin $repo
} else {
  git remote set-url origin $repo
}

git push -u origin $branch
Write-Host "Done. Open: https://github.com/zephyrkeaton/vava8"
