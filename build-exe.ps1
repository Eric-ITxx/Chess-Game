$JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"
$JAVAFX_JMODS = "C:\javafx-sdk-21\javafx-jmods-21.0.10"
$PROJECT = "C:\Users\hunte\OneDrive\Desktop\chess-backend\chess-backend"

Write-Host "=== Step 1: Creating JAR ===" -ForegroundColor Cyan

New-Item -ItemType Directory -Force -Path "$PROJECT\target" | Out-Null

& "$JAVA_HOME\bin\jar.exe" `
    --create `
    --file "$PROJECT\target\chess-backend.jar" `
    --main-class "Driver.Launcher" `
    -C "$PROJECT\target\classes" .

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: JAR creation failed." -ForegroundColor Red
    exit 1
}
Write-Host "JAR created at target\chess-backend.jar" -ForegroundColor Green

Write-Host ""
Write-Host "=== Step 2: Running jpackage ===" -ForegroundColor Cyan

if (!(Test-Path $JAVAFX_JMODS)) {
    Write-Host "ERROR: JavaFX jmods not found at $JAVAFX_JMODS" -ForegroundColor Red
    Write-Host ""
    Write-Host "Download it from: https://gluonhq.com/products/javafx/" -ForegroundColor Yellow
    Write-Host "  -> Version: 21.0.10, OS: Windows, Type: jmods" -ForegroundColor Yellow
    Write-Host "  -> Extract to: C:\javafx-sdk-21\javafx-jmods-21.0.10\" -ForegroundColor Yellow
    exit 1
}

Remove-Item -Recurse -Force "$PROJECT\ChessGame-dist" -ErrorAction SilentlyContinue

& "$JAVA_HOME\bin\jpackage.exe" `
    --input "$PROJECT\target" `
    --main-jar "chess-backend.jar" `
    --main-class "Driver.Launcher" `
    --module-path $JAVAFX_JMODS `
    --add-modules "javafx.controls,javafx.fxml,javafx.media,javafx.swing" `
    --type "app-image" `
    --name "ChessGame" `
    --dest "$PROJECT\ChessGame-dist"

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: jpackage failed." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== DONE ===" -ForegroundColor Green
Write-Host "Your exe is at: ChessGame-dist\ChessGame\ChessGame.exe" -ForegroundColor Green
Write-Host "Zip up the entire ChessGame-dist\ChessGame\ folder and share it." -ForegroundColor Green
