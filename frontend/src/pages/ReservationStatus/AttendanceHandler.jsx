import { useState, useEffect, useRef } from 'react';

const AttendanceHandler = () => {
  const [scanState, setScanState] = useState('initial'); 
  const [studentData, setStudentData] = useState(null);
  const [currentTime, setCurrentTime] = useState('');
  const [sentQRCode, setSentQRCode] = useState('');
  const qrBufferRef = useRef("");
  const isFirstKeyRef = useRef(true);
  
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
          
          console.log(responseData);
          const responseData = await response.json();
          
          // 현재 시간 설정
          updateCurrentTime();
          
          // API 응답을 받은 후에 상태 변경
          if (response.status === 403) {
            setStudentData({ name: "수업 시작 전", message: responseData.message });
            setScanState('complete-error');
          } else if (response.status === 401) {
            setStudentData({ name: "만료된 QR", message: responseData.message });
            setScanState('complete-error');
          } else if (response.status === 200) {
            if (responseData.data === "ENTRANCE") {
              setStudentData({ name: responseData.studentName || "학생", studentId: responseData.studentId });
              setScanState('complete-present');
            } else if (responseData.data === "LATE") {
              setStudentData({ name: responseData.studentName || "학생", studentId: responseData.studentId });
              setScanState('complete-late');
            }
          } else {
            setStudentData({ name: "오류 발생", message: responseData.message || "알 수 없는 오류" });
            setScanState('complete-error');
          }
          
          setSentQRCode(qrData);
          
        } catch (error) {
          console.error("QR 인식 중 오류 발생:", error);
          setStudentData({ name: "오류 발생", message: "네트워크 오류" });
          setScanState('complete-error');
          updateCurrentTime();
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