import { useState, useEffect, useRef } from 'react';
import { useTokenHandler } from '../Mainpage/handlers/TokenHandler';

const AttendanceHandler = () => {
  const [scanState, setScanState] = useState('initial'); 
  const [studentData, setStudentData] = useState(null);
  const [currentTime, setCurrentTime] = useState('');
  const [sentQRCode, setSentQRCode] = useState('');
  const qrBufferRef = useRef("");
  const isFirstKeyRef = useRef(true);
  const {
    refreshTokens,
  } = useTokenHandler();
  
  // QR 스캐너 키보드 이벤트 리스너
  useEffect(() => {
    const handleScan = async (event) => {
      if (event.key === "Enter") {
        if (!qrBufferRef.current.trim()) {
          isFirstKeyRef.current = true;
          return;
        }
    
        let qrData = qrBufferRef.current;
    
        try {
          const parsedData = JSON.parse(qrBufferRef.current);
          if (parsedData?.data) {
            qrData = parsedData.data;
          }
        } catch (err) {
        }
    
        try {
          let accessToken = sessionStorage.getItem("accessToken");
          let response = await fetch(`/api/qr/recognize`, {
            method: "POST",
            headers: {
              Authorization: `Bearer ${accessToken}`,
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ qrCode: qrData }),
          });
    
          // 401 에러 처리: refreshTokens 실행 후 다시 요청
          if (!response.ok && response.status === 401) {
            accessToken = await refreshTokens();
    
            // 새 토큰으로 재요청
            response = await fetch(`/api/qr/recognize`, {
              method: "POST",
              headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
              },
              body: JSON.stringify({ qrCode: qrData }),
            });
          }
    
          let responseData;
          const contentType = response.headers.get("content-type");
    
          if (contentType && contentType.includes("application/json")) {
            responseData = await response.json();
          } else {
            responseData = await response.text();
          }
    
          updateCurrentTime();
    
          if (response.status === 400) {
            setStudentData({ name: "출석 오류", message: responseData.message });
            setScanState("complete-error");
          } else if (response.status === 200) {
            if (responseData.data.status === "ENTRANCE") {
              setStudentData({ name: responseData.data.userName || "학생", studentId: responseData.data.userNumber });
              setScanState("complete-present");
            } else if (responseData.data.status === "LATE") {
              setStudentData({ name: responseData.data.userName || "학생", studentId: responseData.data.userNumber });
              setScanState("complete-late");
            }
          } else {
            setStudentData({ name: "오류 발생", message: "잠시 후 다시 이용해주세요." });
            setScanState("complete-error");
          }
    
          setSentQRCode(qrData);
        } catch (error) {
        }
    
        qrBufferRef.current = "";
        isFirstKeyRef.current = true;
      } else if (event.key !== "Shift") {
        if (isFirstKeyRef.current && scanState === "initial") {
          setScanState("scanning");
          isFirstKeyRef.current = false;
        }
        qrBufferRef.current += event.key;
      }
    };
    
  
    window.addEventListener("keydown", handleScan);
    return () => window.removeEventListener("keydown", handleScan);
  }, [scanState]);
  
  // 완료 상태에서 일정 시간 후 자동 리셋
  useEffect(() => {
    // complete 상태일 때만 타이머 시작
    if (scanState.startsWith('complete')) {
      const resetTimer = setTimeout(() => {
        resetScanner();
      }, 2000); // 2초 후 자동 리셋
      
      return () => clearTimeout(resetTimer);
    }
  }, [scanState]);
  
  const updateCurrentTime = () => {
    const now = new Date();
    const timeString = now.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
    const dateString = now.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long'
    });
    
    setCurrentTime(`${dateString} ${timeString}`);
  };
  useEffect(() => {
}, [studentData]);

  const resetScanner = () => {
    setScanState('initial');
    setStudentData(null);
    qrBufferRef.current = "";
    isFirstKeyRef.current = true;
  };

  return {
    scanState,
    studentData,
    currentTime,
    sentQRCode,
    resetScanner
  };
};

export default AttendanceHandler;