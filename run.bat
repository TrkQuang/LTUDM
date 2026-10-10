@echo off
chcp 65001 > nul
title ỨNG DỤNG MẠNG HỢP NHẤT
echo ========================================================
echo   KHỞI ĐỘNG ỨNG DỤNG LẬP TRÌNH MẠNG (GUI - BUS - DAO)
echo   Tích hợp TCP Server và HTTP Server
echo ========================================================
echo.

if exist "target\LTUDM_App-1.0.0.jar" (
    java -jar target\LTUDM_App-1.0.0.jar
) else (
    echo Chưa tìm thấy file JAR đóng gói, đang tiến hành build Maven...
    call mvn clean package -DskipTests
    java -jar target\LTUDM_App-1.0.0.jar
)

pause
