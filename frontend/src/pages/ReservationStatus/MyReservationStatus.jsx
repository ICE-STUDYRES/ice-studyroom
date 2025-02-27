import React, { useState, useEffect } from "react";
import { ChevronLeft, LogOut, Clock, QrCode, X, Scan } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useMainpageHandlers } from '../Mainpage/handlers/MainpageHandlers';
import { QRCodeCanvas } from 'qrcode.react';
import useQRCodeFetcher from '../Mainpage/components/QRCodeFetcher';
import { useTokenHandler } from "../Mainpage/handlers/TokenHandler";
import { useMemberHandlers } from '../Mainpage/handlers/MemberHandlers';

const MyReservationStatus = () => {
  const {
    showQRModal,
    handleQRClick,
    handleCloseQRModal,
  } = useMainpageHandlers();

  const {
    refreshTokens,
  } = useTokenHandler();

  const {
    handleLogout
  } =useMemberHandlers();

  const navigate = useNavigate();
  const [myReservations, setMyReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const resId = myReservations.length > 0 ? myReservations[0].id : null;
  const { qrCode, error: qrError, loading: qrLoading} = useQRCodeFetcher(resId);

  const handleQRScannerClick = () => {
    navigate('/attendance');
  };


  useEffect(() => {
    fetchMyReservations();
  }, []);

  const fetchMyReservations = async (retry = true) => {
    setLoading(true);
    try {
        let accessToken = sessionStorage.getItem('accessToken');
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
            accessToken = await refreshTokens();

            if (accessToken) {
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
            id: item.reservation.id,
            status: item.reservation.status,
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

  const formatReservationDate = (dateString) => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      weekday: "short",
    }).format(date);
  };

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
          onClick={handleLogout}
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
          <span className="text-sm text-gray-500">{new Date().toLocaleDateString()}</span>
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
          <div className="flex items-center gap-2">
            <p className="text-sm text-gray-500">스터디룸 입실 시 QR코드를 스캔해주세요</p>
            <button 
              onClick={handleQRScannerClick}
              className="flex items-center gap-1 px-3 py-1 text-sm text-gray-600 bg-gray-100 rounded-full hover:bg-gray-200 transition-colors"
            >
              QR스캐너
              <Scan className="w-4 h-4" />
            </button>
          </div>
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
            {myReservations.map(( {reservation, status} ) => (
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
                  <span className={
                    ['RESERVED', 'ENTRANCE', 'COMPLETED'].includes(status) ? 'text-blue-700' :
                    ['CANCELLED', 'NO_SHOW', 'LATE'].includes(status) ? 'text-red-500' :
                    'text-gray-700'                    
                  }>
                    {status === 'RESERVED' ? '예약됨' :
                    status === 'CANCELLED' ? '취소됨' :
                    status === 'NO_SHOW' ? '노쇼' :
                    status === 'LATE' ? '지각' :
                    status == 'ENTRANCE' ? '출석됨' :
                    status == 'COMPLETED' ? '정상 출석' :
                    '알 수 없음'}
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
                value={qrCode} 
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
