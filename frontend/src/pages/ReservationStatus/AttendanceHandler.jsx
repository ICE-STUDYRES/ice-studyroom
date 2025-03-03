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
      // Enter는 QR 데이터의 끝을 의미
      if (event.key === "Enter") {
        if (!qrBufferRef.current.trim()) {
          // 버퍼가 비어있으면 초기화
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
          console.warn("⚠️ QR 코드 데이터가 JSON 형식이 아님. 그대로 사용함.");
        }
  
        try {
          const accessToken = sessionStorage.getItem("accessToken");
          const response = await fetch(`/api/qr/recognize`, {
            method: "POST",
            headers: {
              Authorization: `Bearer ${accessToken}`,
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ qrCode: qrData }),
          });
          

          let responseData;
          const contentType = response.headers.get("content-type");
      
          if (contentType && contentType.includes("application/json")) {
              responseData = await response.json();
          } else {
              responseData = await response.text(); // JSON이 아닐 경우 텍스트로 처리
              console.warn("⚠️ 서버가 JSON이 아닌 응답을 반환함:", responseData);
          }
      
          // 현재 시간 설정
          updateCurrentTime();
          
          // API 응답을 받은 후에 상태 변경
          if (response.status === 400) {
            setStudentData({ name: "출석 오류", message: responseData.message});
            setScanState('complete-error');

          } else if (response.status === 200) {
            if (responseData.data.status === "ENTRANCE") {
              setStudentData({ name: responseData.data.userName || "학생", studentId: responseData.data.userNumber });
              setScanState('complete-present');
            } else if (responseData.data.status === "LATE") {
              setStudentData({ name: responseData.data.userName || "학생", studentId: responseData.data.userNumber });
              setScanState('complete-late');
            }
          } 
          else if (response.status === 401) {
              console.warn("Access token expired. Refreshing tokens...");
              accessToken = await refreshTokens();
  
              if (accessToken) {
                  return handleScan(event);
              }
          }
          
          setSentQRCode(qrData);
          
        } catch (error) {
          if (error.response && error.response.status === 401) { // 토큰 만료 시 새로고침 후 재요청
            console.warn("Access token expired. Refreshing tokens...");
            accessToken = await refreshTokens();

            if (accessToken) {
                return handleScan(event);
            }
        }

          // setStudentData({ name: "오류 발생", message: "네트워크 오류" });
          // setScanState('complete-error');
          // updateCurrentTime();
        }
        
        // 버퍼와 첫 키 입력 플래그 초기화
        qrBufferRef.current = "";
        isFirstKeyRef.current = true;
        
      } else if (event.key !== "Shift") {
        // 첫 키 입력 시 scanning 상태로 전환 (Shift 키 제외)
        if (isFirstKeyRef.current && scanState === 'initial') {
          setScanState('scanning');
          isFirstKeyRef.current = false;
        }
        
        // 버퍼에 키 추가
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