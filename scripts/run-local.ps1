$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env"

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $separator = $line.IndexOf("=")
            if ($separator -gt 0) {
                $name = $line.Substring(0, $separator).Trim()
                $value = $line.Substring($separator + 1).Trim()
                [Environment]::SetEnvironmentVariable($name, $value, "Process")
            }
        }
    }
}

Set-Location $root
mvn spring-boot:run
