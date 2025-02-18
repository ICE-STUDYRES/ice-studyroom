import React, { useState, useEffect } from 'react';
import { ChevronLeft, Clock, LogOut, CalendarDays, AlertCircle, CheckCircle2, X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useNotification } from '../Notification/Notification';
import { useTokenHandler } from '../Mainpage/handlers/TokenHandler';
import { useMemberHandlers } from '../Mainpage/handlers/MemberHandlers';

const StudyRoomManage = () => {
  const { addNotification } = useNotification();
  const {
    handleLogout
  } = useMemberHandlers();
  const navigate = useNavigate();
  const [selectedExtension, setSelectedExtension] = useState(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [booking, setBooking] = useState({
    id: '',
    room: '',
    date: '',
    time: '',
    userName: '',
    userEmail: '',
    participants: [{ studentNum: '', name: '' }],
    endTime: '',
    extendDeadline: '',
  }); 

    const {
      refreshTokens
    } = useTokenHandler();

    useEffect(() => {
      const fetchBookingData = async () => {
        try {
          let accessToken = localStorage.getItem('accessToken');
          const response = await fetch('/api/reservations/my', {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${accessToken}`
            }
          });
    
          if (response.status === 401) { // Unauthorized 발생 시
            console.warn('토큰이 만료됨. 새로고침 시도.');
            const newAccessToken = await refreshTokens();
            if (newAccessToken) {
              return fetchBookingData(); // 새 토큰으로 다시 실행
            } else {
              console.error('토큰 갱신 실패. 로그아웃 필요.');
              return;
            }
          }
    
          const result = await response.json();
    
          if (result.code === 'S200' && result.data.length > 0) {
            // ✅ `status === "RESERVED"` 또는 `reserved === true`인 예약만 필터링
            const reservedBookings = result.data.filter(
              booking => booking.reservation?.status === "RESERVED" || booking.reservation?.reserved === true
            );
    
            if (reservedBookings.length > 0) {
              const bookingData = getNearestBooking(reservedBookings);
              if (bookingData) {
                setBooking({
                  id: bookingData.id || '',
                  room: bookingData.roomNumber || '',
                  date: bookingData.scheduleDate || '',
                  time: `${getFormattedTime(bookingData.startTime)}~${getFormattedTime(bookingData.endTime)}`,
                  userName: bookingData.userName || '',
                  userEmail: bookingData.userEmail || '',
                  userId: bookingData.studentId || '',
                  participants: Array.isArray(bookingData.participants) ? bookingData.participants : [],
                  endTime: getFormattedTime(bookingData.endTime),
                  extendDeadline: getExtendDeadline(bookingData.endTime),
                });
              }
            } else {
              setBooking({}); // 예약이 없으면 초기화
            }
          }
        } catch (error) {
          console.error('Error fetching booking data:', error);
        }
      };
    
      fetchBookingData();
    }, []);
    

  const isWithinExtensionTime = () => {
    if (!booking.endTime) return false;
  
    const now = new Date();
    const [endHour, endMinute] = booking.endTime.split(':').map(Number);
  
    // 연장 가능 시작 시간 (예약 종료 10분 전)
    const extensionStartTime = new Date();
    extensionStartTime.setHours(endHour, endMinute - 10, 0, 0);
  
    // 예약 종료 시간
    const extensionEndTime = new Date();
    extensionEndTime.setHours(endHour, endMinute, 0, 0);
  
    return now >= extensionStartTime && now < extensionEndTime;
  };

  const getExtensionSlots = () => {
    if (!booking.endTime) return [];
  
    const [endHour, endMinute] = booking.endTime.split(':').map(Number);
  
    // 연장 시간 (종료 시간 +1시간)
    const extendedHour = endHour + 1;
    const startTime = `${String(endHour).padStart(2, '0')}:${String(endMinute).padStart(2, '0')}`;
    const endTime = `${String(extendedHour).padStart(2, '0')}:${String(endMinute).padStart(2, '0')}`;
  
    return [{ time: `${startTime}~${endTime}`, available: isWithinExtensionTime() }];
  };
  
  const extensionSlots = getExtensionSlots();  

  const getFormattedTime = (time) => {
    if (!time) return '';
    return time.slice(0, 5); // "HH:MM:SS" → "HH:MM"
  };

  const getExtendDeadline = (endTime) => {
    if (!endTime) return '';
  
    let [endHour, endMinute] = endTime.split(':').map(Number);
  
    // 10분 전으로 계산
    endMinute -= 10;
    if (endMinute < 0) {
      endMinute += 60;
      endHour -= 1;
    }
  
    return `${String(endHour).padStart(2, '0')}:${String(endMinute).padStart(2, '0')}`;
  };

  const getNearestBooking = (reservations) => {
    if (!reservations || reservations.length === 0) return null;
  
    const now = new Date();
  
    return reservations
      .map(({ reservation, participants }) => { // 🔥 `reservation` 안의 값 추출
        if (!reservation) {
          console.warn("⚠️ reservation 객체가 없습니다:", reservation);
          return null;
        }
  
        const startTimeString = reservation.startTime || '00:00';
        const endTimeString = reservation.endTime || '00:00';
  
        const [startHour, startMinute] = startTimeString.split(':').map(Number);
        const [endHour, endMinute] = endTimeString.split(':').map(Number);
  
        const startTime = new Date(reservation.scheduleDate);
        startTime.setHours(startHour, startMinute, 0, 0);
  
        const endTime = new Date(reservation.scheduleDate);
        endTime.setHours(endHour, endMinute, 0, 0);
  
        return { 
          ...reservation, 
          startTimeObj: startTime, 
          endTimeObj: endTime,
          participants // 🔥 `participants`도 함께 반환
        };
      })
      .filter(booking => booking && booking.endTimeObj >= now) // 🔥 유효한 데이터만 필터링
      .sort((a, b) => a.startTimeObj - b.startTimeObj)[0]; // 🔥 가장 가까운 예약 반환
  };
  

  const handleCancelReservation = async () => {
    try {
      const accessToken = localStorage.getItem("accessToken");
  
      if (!booking.id) {
        alert("취소할 예약이 없습니다.");
        return;
      }
  
      const response = await axios.delete(`/api/reservations/${booking.id}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
      });
  
      console.log("예약 취소 응답:", response);
  
      if (response.data?.code === "S200") {
        setShowCancelConfirm(false); // 모달 닫기
        setBooking({}); // 예약 데이터 초기화
        addNotification('cancellation', 'success');
        navigate("/"); // 예약 목록으로 이동
      } else {
        alert("예약 취소 실패: " + (response.data?.message || "알 수 없는 오류"));
      }
    } catch (error) {
      console.error("예약 취소 오류:", error);
  
      if (error.response) {
        // 🔥 서버에서 응답을 보내온 경우 (400, 500 등)
        const errorMessage = error.response.data?.message || "예약 취소 중 오류가 발생했습니다.";
        alert(errorMessage);
      } else if (error.request) {
        // 🔥 요청이 보내졌으나 응답을 받지 못한 경우
        alert("서버 응답이 없습니다. 네트워크 상태를 확인해주세요.");
      } else {
        // 🔥 기타 에러
        alert("알 수 없는 오류가 발생했습니다. 다시 시도해주세요.");
      }
    }
  };  
  
  const extendReservation = async () => {
    try {
      let accessToken = localStorage.getItem("accessToken");
      const response = await axios.patch(
        `/api/reservations/${booking.id}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        }
      );
  
      if (response.status === 401) { // Unauthorized 발생 시
        const newAccessToken = await refreshTokens();
        if (newAccessToken) {
          return extendReservation(); // 새 토큰으로 다시 실행
        } else {
          console.error('토큰 갱신 실패. 로그아웃 필요.');
          return;
        }
      }
  
      addNotification('extension', 'success');

      if (response.code !== "S200") {
        addNotification('extension', 'error', response.data.message);
      }
    } catch (error) {
    }
  };

  const handleCancelClick = () => {
    if (!booking.id) {
      alert("진행 중인 예약이 없습니다.");
      return;
    }
    setShowCancelConfirm(true); // 바로 모달을 띄움
  };

  const isPastReservation = () => {
    if (!booking.time || !booking.date) return true; // 예약 정보가 없으면 비활성화
  
    const now = new Date();
    const [startHour, startMinute] = booking.time.split("~")[0].split(":").map(Number); // 예약 시작 시간
  
    const reservationStartTime = new Date(booking.date);
    reservationStartTime.setHours(startHour, startMinute, 0, 0);
  
    return now >= reservationStartTime; // 현재 시간이 예약 시작 시간을 넘었으면 true (비활성화)
  };

  const CancelConfirmation = () => {
    const now = new Date();
  const [startHour, startMinute] = booking.time.split("~")[0].split(":").map(Number); // 예약 시작 시간 가져오기
  const startTime = new Date(booking.date);
  startTime.setHours(startHour, startMinute, 0, 0);

  const timeDifference = (startTime - now) / (1000 * 60); // 분 단위 차이 계산
  return (
    <div 
      className="fixed inset-0 bg-black/30 z-40 flex items-center justify-center"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          setShowCancelConfirm(false);
        }
      }}
    >
      <div className="max-w-[480px] w-full mx-4">
        <div className="bg-white rounded-2xl p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-slate-900">예약 취소</h3>
            <button 
              onClick={() => setShowCancelConfirm(false)}
              className="p-1 hover:bg-gray-100 rounded-full transition-colors"
            >
              <X className="w-5 h-5 text-gray-500" />
            </button>
          </div>
          
          {/* 취소할 예약 정보 표시 */}
          <div className="p-4 bg-gray-50 rounded-xl space-y-3">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-lg font-bold text-slate-900">{booking.room}</span>
                <span className="text-gray-600">| {booking.date}</span>
              </div>
              <div className="flex items-center gap-2 mt-1">
                <Clock className="w-4 h-4 text-gray-600" />
                <span className="text-gray-900">{booking.time}</span>
              </div>
            </div>

            <div className="space-y-2">
              <div>
                <div className="text-sm text-gray-600 mb-1">예약자</div>
                <div className="flex items-center gap-1">
                  <span className="font-medium text-gray-900">{booking.userName}</span>
                </div>
              </div>
              
              {booking.participants.length > 1 && (
                <div>
                  <div className="text-sm text-gray-600 mb-1">참여자</div>
                  {booking.participants.slice(1).map((participant, index) => (
                    <div key={index} className="flex items-center gap-1">
                      <span className="font-medium text-gray-900">{participant.name}</span>
                      <span className="text-sm text-gray-500">({participant.studentNum})</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* 🔥 예약 시작 1시간 미만이면 패널티 경고 메시지 추가 (삽입 위치) */}
          {timeDifference < 60 && (
            <p className="text-sm text-red-500 text-center font-medium">
              현재 예약 시작까지 1시간이 채 남지 않았습니다.<br />
              취소하면 패널티를 받을 수 있습니다.
            </p>
          )}

          {/* 기존 메시지 */}
          <p className="text-sm text-gray-600 text-center">
            예약을 취소하시겠습니까?
          </p>

          <div className="grid grid-cols-2 gap-3">
            <button 
              onClick={() => setShowCancelConfirm(false)}
              className="w-full py-3 text-sm font-medium text-gray-500 border-2 border-gray-200 rounded-xl hover:bg-gray-50 transition-colors"
            >
              돌아가기
            </button>
            <button 
              onClick={handleCancelReservation}
              className="w-full py-3 text-sm font-medium text-white bg-red-500 rounded-xl hover:bg-red-600 transition-colors"
            >
              취소하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

  // 현재 날짜를 가져오는 함수
  const getCurrentDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    
    // 요일 배열
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const dayName = days[today.getDay()];
    
    return `${year}.${month}.${day} (${dayName})`;
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
          <h1 className="font-semibold text-gray-900">정보통신공학과 스터디룸</h1>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700">
          <LogOut className="w-4 h-4" />
          로그아웃
        </button>
      </div>

      {/* Booking Info */}
      <div className="bg-white border-b">
        <div className="px-4 py-3 flex items-center justify-between border-b">
          <h2 className="text-lg font-semibold">예약 연장/취소</h2>
          <span className="text-sm text-gray-500">{getCurrentDate()}</span>
        </div>
      </div>
      
      <div className="overflow-y-auto h-[calc(100vh-180px)] pb-24">
        {/* 진행 중인 예약이 없을 경우 */}
        {!booking.id ? (
          <div className="p-4 text-center">
          <div className="flex flex-col items-center justify-center space-y-4">
            <AlertCircle className="w-10 h-10 text-gray-400" />
            <p className="text-gray-600 text-lg font-semibold">현재 진행 중인 예약이 없습니다.</p>
          </div>
        </div>
        ) : (
          <div className="p-4">
            <div className="rounded-2xl border-2 border-slate-900 p-4 bg-white">
              <div className="space-y-4">
                <div>
                  <h2 className="text-2xl font-bold text-slate-900">{booking.room}</h2>
                  <div className="flex items-center gap-2 mt-1">
                    <CalendarDays className="w-4 h-4 text-gray-600" />
                    <span className="text-gray-600">{booking.date}</span>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Clock className="w-4 h-4 text-gray-600" />
                  <span className="text-gray-900 font-medium">{booking.time}</span>
                </div>
                <div className="space-y-2">
                  <div>
                    <div className="text-sm text-gray-600 mb-1">예약자</div>
                  <div className="flex items-center gap-1">
                    <span className="font-medium text-gray-900">{booking.userName}</span>
                  </div>
                  </div>

                  {booking.participants.length > 1 && (
                    <div>
                      <div className="text-sm text-gray-600 mb-1">참여자</div>
                      {booking.participants.slice(1).map((participant, index) => (
                        <div key={index} className="flex items-center gap-1">
                          <span className="font-medium text-gray-900">{participant.name}</span>
                          <span className="text-sm text-gray-500">({participant.studentNum})</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}        

        {/* Extension Notice */}
        <div className="px-4 mt-2">
          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200">
            <div className="flex items-center gap-2 mb-2">
              <AlertCircle className="w-5 h-5 text-slate-900" />
              <span className="font-semibold text-slate-900">연장 안내</span>
            </div>
            <p className="text-sm text-gray-600">
              연장은 예약 종료 10분 전부터 가능합니다.
              <br />
              (현재 예약: {booking.endTime} 종료 → {booking.extendDeadline}부터 연장 가능)
            </p>
          </div>
        </div>

        {/* Extension Options */}
        <div className="px-4 mt-6">
          <h3 className="text-lg font-bold text-slate-900 mb-3">연장 가능 시간</h3>
          <div className="space-y-2">
            {extensionSlots.map((slot, index) => (
              <button
              key={index}
              disabled={!slot.available} // 연장 가능 시간이 아닐 경우 클릭 불가능
              onClick={() => {
                if (slot.available) {
                  setSelectedExtension(selectedExtension === slot.time ? null : slot.time);
                }
              }}
              className={`
                w-full p-4 rounded-xl border-2 font-medium transition-all
                ${!slot.available 
                  ? 'bg-gray-50 border-gray-100 cursor-not-allowed text-gray-400' 
                  : selectedExtension === slot.time
                    ? 'bg-slate-900 border-slate-900 text-white' // ✅ 선택 시 대비 강화
                    : 'bg-white border-gray-300 hover:border-gray-500 text-gray-900' // ✅ 기본 상태
                }
              `}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Clock className={`
                    w-5 h-5
                    ${selectedExtension === slot.time ? 'text-white' : slot.available ? 'text-gray-600' : 'text-gray-300'}
                  `} /> 
                  <span className={selectedExtension === slot.time ? "text-white" : "text-gray-900"}>
                    {slot.time}
                  </span>
                </div>
                {selectedExtension === slot.time && slot.available && (
                  <CheckCircle2 className="w-5 h-5 text-white" /> // ✅ 선택된 상태에서도 잘 보이도록 유지
                )}
              </div>
            </button>            
            ))}
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t">
        <div className="max-w-[480px] w-full mx-auto p-4">
          <div className="grid grid-cols-2 gap-3">
          <button 
            onClick={handleCancelClick}
            disabled={isPastReservation()} // 🔥 예약 시간이 지났으면 비활성화
            className={`
              w-full py-3 text-sm font-medium rounded-xl transition-colors
              ${isPastReservation() 
                ? "bg-gray-300 text-gray-500 cursor-not-allowed"  // 🔥 비활성화 스타일
                : "text-red-500 border-2 border-red-500 hover:bg-red-50"}
            `}
          >
            예약 취소
          </button>
            <button 
              onClick={() => {
                if (selectedExtension) {
                  try {
                    extendReservation();
                    alert('예약이 연장되었습니다.');
                    navigate('/');
                  } catch (error) {
                    console.error("예약 연장 중 오류 발생", error);
                  }
                }
              }}
              disabled={!selectedExtension}
              className={`
                w-full py-3 text-sm font-medium rounded-xl transition-colors
                ${!selectedExtension || isPastReservation()  
                  ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                  : "bg-slate-900 hover:bg-slate-800 text-white"}
              `}
            >
              연장하기
            </button>
          </div>
        </div>
      </div>

      {/* Cancel Confirmation Modal */}
      {showCancelConfirm && <CancelConfirmation />}
    </div>
  );
};

export default StudyRoomManage;