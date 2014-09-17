@echo off
SET GOSS_PROFILE_DIR="%USERPROFILE%\.goss"
if not exist %GOSS_PROFILE_DIR% mkdir %GOSS_PROFILE_DIR%
cp goss.properties %GOSS_PROFILE_DIR%\goss.properties
