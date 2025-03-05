import React, { useState, useEffect } from 'react';
import { ChevronLeft, Clock, LogOut, CalendarDays, AlertCircle, CheckCircle2, X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useNotification } from '../Notification/Notification';
import { useTokenHandler } from '../Mainpage/handlers/TokenHandler';
import { useMemberHandlers } from '../Mainpage/handlers/MemberHandlers';

const StudyRoomManage = () => {
  const { addNotification } = useNotification();
  const { handleLogout } = useMemberHandlers();
  const navigate = useNavigate();
  const [selectedExtension, setSelectedExtension] = useState(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [booking, setBooking] = useState({
    id: '',
    room: '',
    date: '',
    time: '',
    userName: '',
    participants: [{ studentNum: '', name: '' }],
    endTime: '',
    extendDeadline: '',
    status: '',
  }); 
  const { refreshTokens } = useTokenHandler();

  useEffect(() => {
    const fetchBookingData = async () => {
      try {
        let accessToken = sessionStorage.getItem('accessToken');
        const response = await fetch('/api/reservations/my', {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${accessToken}`
          }
        });
  
        if (response.status === 401) {
          console.warn('토큰이 만료됨. 새로고침 시도.');
          accessToken = await refreshTokens();
          if (accessToken) {
            return fetchBookingData();
          } else {
            console.error('토큰 갱신 실패. 로그아웃 필요.');
            return;
          }
        }
  
        const result = await response.json();
  
        if (result.code === 'S200' && result.data.length > 0) {
          const reservedBookings = result.data.filter(
            booking => 
              booking.reservation?.status === "RESERVED" || 
              booking.reservation?.status === "ENTRANCE" || 
              booking.reservation?.reserved === true
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
                userId: bookingData.studentId || '',
                participants: Array.isArray(bookingData.participants) ? bookingData.participants : [],
                endTime: getFormattedTime(bookingData.endTime),
                extendDeadline: getExtendDeadline(bookingData.endTime),
                status: bookingData.status || '', // 🔹 추가됨
              });
            }
          } else {
            setBooking({});
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
  
    const extensionStartTime = new Date();
    extensionStartTime.setHours(endHour, endMinute - 10, 0, 0);
  
    const extensionEndTime = new Date();
    extensionEndTime.setHours(endHour, endMinute, 0, 0);
  
    return now >= extensionStartTime && now < extensionEndTime;
  };

  const getExtensionSlots = () => {
    if (!booking.endTime) return [];
  
    const [endHour, endMinute] = booking.endTime.split(':').map(Number);

    if (endHour === 22 && endMinute === 0) {
      return [];
    }
  
    const extendedHour = endHour + 1;
    const startTime = `${String(endHour).padStart(2, '0')}:${String(endMinute).padStart(2, '0')}`;
    const endTime = `${String(extendedHour).padStart(2, '0')}:${String(endMinute).padStart(2, '0')}`;
  
    return [{ time: `${startTime}~${endTime}`, available: isWithinExtensionTime() }];
  };
  
  const extensionSlots = getExtensionSlots();  

  const getFormattedTime = (time) => {
    if (!time) return '';
    return time.slice(0, 5); 
  };

  const getExtendDeadline = (endTime) => {
    if (!endTime) return '';
  
    let [endHour, endMinute] = endTime.split(':').map(Number);
  
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
      .map(({ reservation, participants }) => { 
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
          participants,
          userName: reservation.userName
        };
      })
      .filter(booking => booking && booking.endTimeObj >= now) 
      .sort((a, b) => a.startTimeObj - b.startTimeObj)[0]; 
  };
  

  const handleCancelReservation = async () => {
    try {
      let accessToken = sessionStorage.getItem("accessToken");
  
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
  
      if (response.data?.code === "S200") {
        setShowCancelConfirm(false); 
        setBooking({}); 
        addNotification('cancellation', 'success');
        navigate("/"); 
      } else {
        alert("예약 취소 실패: " + (response.data?.message || "알 수 없는 오류"));
      }
    } catch (error) {
      if (error.response?.status === 401) { 
        const newAccessToken = await refreshTokens();

        if (newAccessToken) {
            return handleCancelReservation();
        } else {
            sessionStorage.clear();
            navigate('/');
        }
      } else {
        alert("알 수 없는 오류가 발생했습니다. 다시 시도해주세요.");
      }
    }
  };  
  
  const extendReservation = async () => {
    try {
      let accessToken = sessionStorage.getItem("accessToken");
      const response = await axios.patch(
        `/api/reservations/${booking.id}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        }
      );
  
      if (response.status === 200 && response.data?.code === "S200") {
        addNotification("extension", "success");
        navigate('/');
      } else {
        const errorMessage = response.data?.message || "알 수 없는 오류가 발생했습니다.";
        addNotification("extension", "error", errorMessage);
      }
    } catch (error) {

      if (error.response?.status === 401) {
        console.warn("토큰이 만료됨. 새로고침 시도.");
        const newAccessToken = await refreshTokens();
        if (newAccessToken) {
          return extendReservation(); // 토큰 갱신 후 다시 요청
        }
      }
      const errorMessage = error.response?.data?.message || "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
      addNotification("extension", "error", errorMessage);
    }
  };

  const handleCancelClick = () => {
    if (!booking.id) {
      alert("진행 중인 예약이 없습니다.");
      return;
    }
    setShowCancelConfirm(true); 
  };

  const isPastReservation = () => {
    if (!booking.time || !booking.date) return true; 
  
    const now = new Date();
    const [startHour, startMinute] = booking.time.split("~")[0].split(":").map(Number); 
  
    const reservationStartTime = new Date(booking.date);
    reservationStartTime.setHours(startHour, startMinute, 0, 0);
  
    return now >= reservationStartTime; 
  };

  const CancelConfirmation = () => {
    const now = new Date();
    const [startHour, startMinute] = booking.time.split("~")[0].split(":").map(Number);
    const startTime = new Date(booking.date);
    startTime.setHours(startHour, startMinute, 0, 0);

    const timeDifference = (startTime - now) / (1000 * 60);
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

  const getCurrentDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    
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
              연장은 사용 종료 10분 전부터 가능합니다.
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
              disabled={!slot.available || booking.status !== "ENTRANCE"}
              onClick={() => {
                if (slot.available && booking.status === "ENTRANCE") {
                  setSelectedExtension(selectedExtension === slot.time ? null : slot.time);
                }
              }}
              className={`
                w-full p-4 rounded-xl border-2 font-medium transition-all
                ${!slot.available || booking.status !== "ENTRANCE"
                  ? 'bg-gray-50 border-gray-100 cursor-not-allowed text-gray-400' 
                  : selectedExtension === slot.time
                    ? 'bg-slate-900 border-slate-900 text-white'
                    : 'bg-white border-gray-300 hover:border-gray-500 text-gray-900'
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
                {selectedExtension === slot.time && slot.available && booking.status === "ENTRANCE" && (
                  <CheckCircle2 className="w-5 h-5 text-white" />
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
            disabled={isPastReservation()}
            className={`
              w-full py-3 text-sm font-medium rounded-xl transition-colors
              ${isPastReservation() 
                ? "bg-gray-300 text-gray-500 cursor-not-allowed" 
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
                } catch (error) {
                  console.error("예약 연장 중 오류 발생", error);
                }
              }
            }}
            className="
              w-full py-3 text-sm font-medium rounded-xl transition-colors
              bg-slate-900 hover:bg-slate-800 text-white
            "
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