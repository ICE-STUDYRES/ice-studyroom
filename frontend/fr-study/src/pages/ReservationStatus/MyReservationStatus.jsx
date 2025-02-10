import React, { useState, useEffect } from "react";
import { ChevronLeft, LogOut, Clock, QrCode, X } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useMainpageHandlers } from '../Mainpage/MainpageHandlers';
import { QRCodeCanvas } from 'qrcode.react';
import useQRCodeFetcher from '../Mainpage/QRCodeFetcher'; // ✅ QR 코드 데이터 가져오는 훅

const MyReservationStatus = () => {
  const {
    studentId,studentName,showQRModal,refreshTokens,
    handleQRClick,
    handleCloseQRModal,
    handleLogout,
  } = useMainpageHandlers();
  const navigate = useNavigate();
  const [myReservations, setMyReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sentQRCode, setSentQRCode] = useState(null); // ✅ 이미 전송된 QR 코드 저장

  // ✅ 예약 ID 가져오기
  const resId = myReservations.length > 0 ? myReservations[0].id : null;

  // ✅ 항상 최상단에서 Hook 호출 (조건문 안에서 실행 X)
  const { qrCode, error: qrError, loading: qrLoading} = useQRCodeFetcher(resId);
  // ✅ QR 코드 리더기로 스캔하면 서버로 전송 (Enter 입력 감지)
  useEffect(() => {
    let qrBuffer = ""; // ✅ QR 코드 데이터를 임시 저장할 버퍼
  
    const handleScan = async (event) => {
      if (event.key === "Enter") {
        if (!qrBuffer.trim()) return; // 빈 값 방지
  
        console.log("✅ Enter 입력 감지됨!");
        // console.log("✅ 원본 QR 코드 입력값:", qrBuffer);
  
        let qrData = qrBuffer;
  
        // ✅ QR 코드 데이터가 JSON 형식인지 확인
        try {
          const parsedData = JSON.parse(qrBuffer);
          if (parsedData?.data) {
            qrData = parsedData.data; // ✅ JSON이면 `data` 필드 값 사용
            console.log("📌 JSON에서 추출한 QR 코드 데이터:", qrData);
          }
        } catch (err) {
          console.warn("⚠️ QR 코드 데이터가 JSON 형식이 아님. 그대로 사용함.");
        }
  
        const accessToken = localStorage.getItem("accessToken");
        const response = await fetch(`/api/qr/recognize`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ qrCode: qrData }),
        });
  
        const result = await response.json();
        console.log("✅ 서버 응답:", result);
  
        // ✅ 중복 스캔 방지
        setSentQRCode(qrData);
  
        qrBuffer = ""; // ✅ 버퍼 초기화
      } else if (event.key !== "Shift") {
        // ✅ Shift 키를 무시하고 QR 코드 문자만 버퍼에 추가
        qrBuffer += event.key;
      }
    };
  
    window.addEventListener("keydown", handleScan);
    return () => window.removeEventListener("keydown", handleScan);
  }, [setSentQRCode]); // 📌 `sentQRCode`가 변경될 때마다 실행
  



  const handleLogoutClick = async () => {
    try {
      await handleLogout();
      navigate('/');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  useEffect(() => {
    fetchMyReservations();
  }, []);

  const fetchMyReservations = async (retry = true) => {
    setLoading(true);
    try {
        let accessToken = localStorage.getItem('accessToken');
        let refreshToken = localStorage.getItem('refreshToken');
        if (!accessToken) {
            throw new Error('로그인이 필요합니다');
        }

        const response = await fetch('/api/reservations/my', {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (response.status === 401 && retry) {
            console.warn("🔄 Access token expired. Refreshing tokens...");
            console.log("Current access token:", accessToken);
            console.log("Current refresh token:", refreshToken);
            accessToken = await refreshTokens();

            if (accessToken) {
                console.log("🔄 New access token after refresh:", accessToken);
                console.log("🔄 Retrying fetchMyReservations with new access token...");
                return fetchMyReservations(false);
            } else {
                console.error("❌ Token refresh failed. Logging out.");
            }
        }

        if (!response.ok) {
            throw new Error('Failed to fetch my reservations');
        }

        const data = await response.json();

        if (data.code === 'S200') {
            const reservations = data.data.reverse().map(item => ({
                id: item.reservation.id, // ✅ 예약 ID 저장
                ...item
            }));
            setMyReservations(reservations);
        } else {
            throw new Error(data.message);
        }
    } catch (err) {
        setError(err.message);
    } finally {
        setLoading(false);
    }
};



  const formatDate = (date) => {
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const dayOfWeek = days[date.getDay()];
    
    return `${year}.${month}.${day} (${dayOfWeek})`;
  };

  const formatReservationDate = (dateString) => {
    const date = new Date(dateString);
    return formatDate(date);
  };

  const today = new Date();
  const formattedDate = formatDate(today);

  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-b">
        <div className="flex items-center gap-2">
          <button 
            onClick={() => navigate('/')}
            className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ChevronLeft className="w-5 h-5 text-gray-700" />
          </button>
          <h1 className="font-semibold text-gray-900">내 예약 현황</h1>
        </div>
        <button 
          onClick={handleLogoutClick}
          className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
        >
          <LogOut className="w-4 h-4" />
          로그아웃
        </button>
      </div>

      {/* Date Header */}
      <div className="bg-white border-b">
        <div className="px-4 py-3 flex items-center justify-between">
          <h2 className="text-lg font-semibold">예약 현황</h2>
          <span className="text-sm text-gray-500">{formattedDate}</span>
        </div>
      </div>

      {/* QR Code Section */}
      <div className="bg-white border-b p-4">
        <div className="flex flex-col items-center justify-center gap-3">
          <button
          onClick={handleQRClick} // 함수 호출로 수정
          className="p-4 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <QrCode className="w-32 h-32" />
          </button>
          <p className="text-sm text-gray-500">스터디룸 입실 시 QR코드를 스캔해주세요</p>
        </div>
      </div>

      {/* My Reservations Section */}
      <div className="p-4">
        <h2 className="text-lg font-semibold mb-4">내 예약 현황</h2>
        {loading ? (
          <p className="text-sm text-gray-500">로딩 중...</p>
        ) : error ? (
          <p className="text-sm text-red-500">{error}</p>
        ) : myReservations.length > 0 ? (
          <div className="space-y-4">
            {myReservations.map(( {reservation} ) => (
              <div 
                key={reservation.id} 
                className="border-t border-gray-100 pt-4 first:border-t-0 first:pt-0"
              >
                <div className="flex items-center justify-between mb-2">
                  <h4 className="font-medium">{reservation.roomNumber}</h4>
                  <span className="text-sm text-gray-500">
                    {formatReservationDate(reservation.scheduleDate)}
                  </span>
                </div>
                <div className="flex items-center gap-3 text-sm text-gray-900">
                  <Clock className="w-4 h-4 text-gray-400" />
                  <span>{reservation.startTime.slice(0, 5)}-{reservation.endTime.slice(0, 5)}</span>
                  <span className="text-blue-600">
                    {reservation.status === 'RESERVED' ? '예약됨' : '완료됨'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-gray-500">예약된 정보가 없습니다.</p>
        )}
      </div>

      {/* QR Modal */}
      {showQRModal && (
        <div 
        className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50" 
        onClick={handleCloseQRModal}
        >
          <div 
          className="relative bg-white p-6 rounded-lg w-[80vw] h-[80vh] flex flex-col items-center justify-center" 
          onClick={e => e.stopPropagation()}
          >
            {/* ✅ QRCodeFetcher에서 가져온 QR 코드 표시 */}
            {qrLoading ? (
              <p className="text-sm text-gray-500">QR 코드 로딩 중...</p>
            ) : qrError ? (
              <p className="text-sm text-red-500">{qrError}</p>
            ) : qrCode ? (
              <>
              <QRCodeCanvas 
                value={qrCode} // ✅ QR코드 데이터 적용
                size={256} 
                level={"H"} 
                includeMargin={true} 
              />
              </>
            ) : (
              <p className="text-sm text-gray-500">QR 코드 데이터를 불러올 수 없습니다.</p>
            )}

            <button 
              onClick={handleCloseQRModal}
              className="absolute top-2 right-2 p-2 bg-white rounded-full hover:bg-gray-100"
            >
              <X className="w-6 h-6" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};


export default MyReservationStatus;