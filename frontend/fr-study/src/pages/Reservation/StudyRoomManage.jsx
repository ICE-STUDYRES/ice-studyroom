import React, { useState } from 'react';
import { ChevronLeft, Clock, LogOut, CalendarDays, AlertCircle, CheckCircle2, X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const StudyRoomManage = () => {
  const navigate = useNavigate();
  const [selectedExtension, setSelectedExtension] = useState(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  
  // 예약 정보 상태
  const [booking] = useState({
    room: '305-1',
    date: '2024.01.03 (수)',
    time: '14:00~16:00',
    mainUser: {
      name: '김철수',
      studentId: '202012345'
    },
    participants: [
      { name: '이영희', studentId: '202012346' },
      { name: '박지민', studentId: '202012347' }
    ],
    endTime: '16:00',
    extendDeadline: '15:55'
  });

  const extensionSlots = [
    { time: '16:00~17:00', available: true },
    { time: '17:00~18:00', available: false },
  ];

  const isWithinExtensionTime = true;

  const CancelConfirmation = () => (
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
                  <span className="font-medium text-gray-900">{booking.mainUser.name}</span>
                  <span className="text-sm text-gray-500">({booking.mainUser.studentId})</span>
                </div>
              </div>
              
              {booking.participants.length > 0 && (
                <div>
                  <div className="text-sm text-gray-600 mb-1">참여자</div>
                  {booking.participants.map((participant, index) => (
                    <div key={index} className="flex items-center gap-1">
                      <span className="font-medium text-gray-900">{participant.name}</span>
                      <span className="text-sm text-gray-500">({participant.studentId})</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

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
              onClick={() => {
                // 실제 취소 처리 로직
                alert('예약이 취소되었습니다.');
                navigate('/');
              }}
              className="w-full py-3 text-sm font-medium text-white bg-red-500 rounded-xl hover:bg-red-600 transition-colors"
            >
              취소하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );

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
        <button className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700">
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
        {/* Current Booking Info */}
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
                    <span className="font-medium text-gray-900">{booking.mainUser.name}</span>
                    <span className="text-sm text-gray-500">({booking.mainUser.studentId})</span>
                  </div>
                </div>
                
                {booking.participants.length > 0 && (
                  <div>
                    <div className="text-sm text-gray-600 mb-1">참여자</div>
                    {booking.participants.map((participant, index) => (
                      <div key={index} className="flex items-center gap-1">
                        <span className="font-medium text-gray-900">{participant.name}</span>
                        <span className="text-sm text-gray-500">({participant.studentId})</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Extension Notice */}
        <div className="px-4 mt-2">
          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200">
            <div className="flex items-center gap-2 mb-2">
              <AlertCircle className="w-5 h-5 text-slate-900" />
              <span className="font-semibold text-slate-900">연장 안내</span>
            </div>
            <p className="text-sm text-gray-600">
              연장은 예약 종료 30분 전부터 5분 전까지 가능합니다.
              <br />
              (현재 예약: {booking.endTime} 종료 → {booking.extendDeadline}까지 연장 가능)
            </p>
          </div>
        </div>

        {/* Extension Options */}
        <div className="px-4 mt-6">
          <h3 className="text-lg font-bold text-slate-900 mb-3">연장 가능 시간</h3>
          {!isWithinExtensionTime ? (
            <div className="text-center py-8 text-gray-500">
              아직 연장 가능 시간이 아닙니다
            </div>
          ) : (
            <div className="space-y-2">
              {extensionSlots.map((slot, index) => (
                <button
                  key={index}
                  disabled={!slot.available}
                  onClick={() => setSelectedExtension(slot.available ? (selectedExtension === slot.time ? null : slot.time) : null)}
                  className={`
                    w-full p-4 rounded-xl border-2 transition-all
                    ${!slot.available 
                      ? 'bg-gray-50 border-gray-100 cursor-not-allowed' 
                      : selectedExtension === slot.time
                        ? 'bg-slate-900 border-slate-900'
                        : 'bg-white border-gray-200 hover:border-gray-300'
                    }
                  `}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <Clock className={`w-5 h-5 ${
                        !slot.available 
                          ? 'text-gray-300'
                          : selectedExtension === slot.time
                            ? 'text-white'
                            : 'text-gray-600'
                      }`} />
                      <span className={`font-medium ${
                        !slot.available 
                          ? 'text-gray-400'
                          : selectedExtension === slot.time
                            ? 'text-white'
                            : 'text-gray-900'
                      }`}>
                        {slot.time}
                      </span>
                    </div>
                    {selectedExtension === slot.time && (
                      <CheckCircle2 className="w-5 h-5 text-white" />
                    )}
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Action Buttons */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t">
        <div className="max-w-[480px] w-full mx-auto p-4">
          <div className="grid grid-cols-2 gap-3">
            <button 
              onClick={() => setShowCancelConfirm(true)}
              className="w-full py-3 text-sm font-medium text-red-500 border-2 border-red-500 rounded-xl hover:bg-red-50 transition-colors"
            >
              예약 취소
            </button>
            <button 
              onClick={() => {
                if (selectedExtension) {
                  // 실제 연장 처리 로직
                  alert('예약이 연장되었습니다.');
                  navigate('/');
                }
              }}
              disabled={!selectedExtension}
              className={`
                w-full py-3 text-sm font-medium text-white rounded-xl transition-colors
                ${selectedExtension 
                  ? 'bg-slate-900 hover:bg-slate-800' 
                  : 'bg-gray-300 cursor-not-allowed'}
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