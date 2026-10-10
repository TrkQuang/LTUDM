@echo off
chcp 65001 > nul
title LTUDM CLIENT APPLICATION
echo Đang mở ứng dụng LTUDM Client (TCP + HTTP Client)...
java -cp target\LTUDM_App-1.0.0.jar com.ltudm.ClientLauncher
