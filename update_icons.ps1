Add-Type -AssemblyName System.Drawing

function Resize-Image {
    param([string]$InputFile, [string]$OutputFile, [int]$Width, [int]$Height)

    if (-not (Test-Path $InputFile)) {
        Write-Host "Error: Input file not found: $InputFile"
        return
    }

    try {
        $srcImage = [System.Drawing.Image]::FromFile($InputFile)
        $newSocket = new-object System.Drawing.Bitmap($Width, $Height)
        $graph = [System.Drawing.Graphics]::FromImage($newSocket)
        
        $graph.CompositeMode = [System.Drawing.Drawing2D.CompositeMode]::SourceOver
        $graph.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graph.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graph.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

        $graph.DrawImage($srcImage, 0, 0, $Width, $Height)
        
        $folder = Split-Path $OutputFile
        if (-not (Test-Path $folder)) {
            New-Item -ItemType Directory -Path $folder | Out-Null
        }

        $newSocket.Save($OutputFile, [System.Drawing.Imaging.ImageFormat]::Png)
        
        $srcImage.Dispose()
        $newSocket.Dispose()
        $graph.Dispose()
        
        Write-Host "Generated: $OutputFile"
    } catch {
        Write-Host "Error processing $InputFile : $_"
    }
}

$iconSource = "c:\dev\t_launcher\fastlane\metadata\android\en-US\icon.png"
$baseDir = "c:\dev\t_launcher\app\src\main\res"

# Standard Mipmap Sizes for Launcher Icons
$sizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

foreach ($folderName in $sizes.Keys) {
    $size = $sizes[$folderName]
    $destPath = Join-Path $baseDir $folderName
    $outFile = Join-Path $destPath "ic_launcher.png"
    Resize-Image -InputFile $iconSource -OutputFile $outFile -Width $size -Height $size
}
