@echo off
set otros_home=C:\java\olv-1.4.0

echo. Installs AEM-related OTROS plugins to %otros_home%
echo. Deletes pre-installed example plugins

del %otros_home%\plugins\logimporters\log4j-1.pattern
del %otros_home%\plugins\logimporters\selenium.pattern
del %otros_home%\plugins\markers\example-regex.marker
del %otros_home%\plugins\markers\example-string.marker

cd plugins

xcopy /s /y *.* %otros_home%\plugins

cd ..

pause
