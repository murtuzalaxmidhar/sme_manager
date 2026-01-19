@echo off
REM Script to create a cut-down JRE for Windows 7/8
REM User must download JavaFX JMODs first.

set PATH_TO_FX_JMODS="C:\path\to\javafx-jmods-21"

echo Creating minimal JRE...
jlink --module-path %PATH_TO_FX_JMODS% ^
      --add-modules java.base,java.logging,java.sql,java.scripting,jdk.unsupported,javafx.controls,javafx.fxml,javafx.graphics ^
      --output minimal_jre --strip-debug --compress 2 --no-header-files --no-man-pages

echo Done. JRE created in 'minimal_jre' folder.
pause
