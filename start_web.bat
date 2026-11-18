@echo off
title TenderX - Web Platform
echo ==========================================================
echo       Starting TenderX LLM-Assisted Procurement Platform
echo ==========================================================
cd /d "%~dp0web"
start http://localhost:5173
call npm run dev

