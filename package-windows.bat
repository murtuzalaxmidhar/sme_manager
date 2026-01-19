@echo off
set VERSION=1.0
set MAIN_CLASS=com.lax.sme_manager.LaxSmeManagerStarter
set APP_NAME="Lax Yard & SME Manager"
set VENDOR="RASolutions"

echo [1/3] Cleaning and Building Project...
call gradlew clean build copyDependencies

echo [2/3] Creating Runtime Image...
rmdir /s /q build\runtime
jlink --module-path "%JAVA_HOME%\jmods;build\libs\libs;build\libs" ^
      --add-modules javafx.controls,javafx.fxml,javafx.graphics,java.sql,java.naming ^
      --output build\runtime ^
      --strip-debug ^
      --no-header-files ^
      --no-man-pages

echo [3/3] Packaging MSI...
jpackage --name %APP_NAME% ^
         --vendor %VENDOR% ^
         --app-version %VERSION% ^
         --input build\libs ^
         --dest build\dist ^
         --main-jar sme_manager-%VERSION%.jar ^
         --main-class %MAIN_CLASS% ^
         --runtime-image build\runtime ^
         --type msi ^
         --win-dir-chooser ^
         --win-menu ^
         --win-shortcut ^
         --icon assets/app_icon.ico

echo Done! MSI is in build/dist
pause
