# 8181 배포 가이드 (Windows PowerShell)

## 1) 로컬 JAR 배포
```powershell
cd D:\서울IT교육센터\dev\teamwork\project\backend\hospital_reception_backend\reception_back

# 빌드
.\gradlew.bat clean build -x test

# 8181 점유 프로세스 종료(있을 때만)
$conn = Get-NetTCPConnection -LocalPort 8181 -State Listen -ErrorAction SilentlyContinue
if ($conn) { Stop-Process -Id $conn.OwningProcess -Force }

# 실행 (포그라운드)
java -jar .\build\libs\reception-0.0.1-SNAPSHOT.jar --server.port=8181
```

## 2) Docker Compose 배포
```powershell
cd D:\서울IT교육센터\dev\teamwork\project\backend\hospital_reception_backend\reception_back

# Docker Desktop 실행 후
docker compose down
docker compose build backend
docker compose up -d db backend

# 로그 확인
docker logs -f reception-backend
```

## 3) 배포 검증
```powershell
curl.exe -i http://192.168.1.60:8181/api/receptions
curl.exe -i http://192.168.1.60:8181/api/reception/history
curl.exe -i http://192.168.1.60:8181/api/receptions/1/status-history
```

정상 기준:
- 위 3개가 200 + `success:true`
