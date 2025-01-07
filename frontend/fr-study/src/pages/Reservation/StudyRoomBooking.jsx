import React, { useState } from 'react';
import { ChevronLeft, Clock, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const StudyRoomBooking = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('room');
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState('');
  const [bookedSlots, setBookedSlots] = useState([]); // 예약된 시간대 관리
  const [userInfo, setUserInfo] = useState({
    mainUser: { name: '', studentId: '' },
    participants: [],
    totalUsers: 1,
  });

  const rooms = [
    { id: '305-1', name: '305-1', capacity: 4, facilities: ['PC', '모니터'], location: '3층' },
    { id: '305-2', name: '305-2', capacity: 4, facilities: ['PC', '모니터'], location: '3층' },
    { id: '305-3', name: '305-3', capacity: 4, facilities: ['PC', '모니터'], location: '3층' },
    { id: '305-4', name: '305-4', capacity: 4, facilities: ['PC', '모니터'], location: '3층' },
    { id: '305-5', name: '305-5', capacity: 4, facilities: ['PC', '모니터'], location: '3층' },
    { id: '305-6', name: '305-6', capacity: 4, facilities: ['PC', '모니터'], location: '3층' },
    { id: '409-1', name: '409-1', capacity: 6, facilities: ['화이트보드', 'PC', '대형 모니터'], location: '4층' },
    { id: '409-2', name: '409-2', capacity: 6, facilities: ['화이트보드', 'PC', '대형 모니터'], location: '4층' },
  ];

  const timeSlots = [
    '09:00~10:00',
    '10:00~11:00',
    '11:00~12:00',
    '12:00~13:00',
    '13:00~14:00',
    '14:00~15:00',
    '15:00~16:00',
    '16:00~17:00',
    '17:00~18:00',
    '18:00~19:00',
    '19:00~20:00',
    '20:00~21:00',
    '21:00~22:00',
  ];

  const handleReservation = async () => {
    if (!selectedRoom || selectedTimes.length === 0 || !userInfo.mainUser.name || !userInfo.mainUser.studentId) {
      alert('모든 필수 정보를 입력해주세요.');
      return;
    }

    const [startTime, endTime] = selectedTimes.sort();

    const reservationData = {
      userId: 1, // 로그인된 사용자 ID. 실제로는 인증 정보에서 가져와야 함
      scheduleId: selectedTimes.map((time) => timeSlots.indexOf(time)),
      userName: [userInfo.mainUser.studentId, ...userInfo.participants.map((p) => p.studentId)],
      roomNumber: selectedRoom,
      startTime: startTime.split('~')[0],
      endTime: endTime ? endTime.split('~')[1] : startTime.split('~')[1],
    };

    try {
      const response = await fetch('/api/reservations', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(reservationData),
      });

      if (!response.ok) {
        throw new Error('예약 요청 실패');
      }

      const data = await response.json();

      // 예약된 시간대를 상태로 업데이트
      setBookedSlots(data.bookedSlots || []); // 서버에서 예약된 시간대를 반환해야 함

      alert('예약이 성공적으로 완료되었습니다!');
      navigate('/confirmation');
    } catch (error) {
      console.error('예약 요청 오류:', error);
      alert('예약에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const canSelectTime = (time) => {
    if (bookedSlots.includes(time)) return false; // 예약된 시간대는 선택 불가
    if (selectedTimes.length === 0) return true;

    const timeIndex = timeSlots.indexOf(time);
    const selectedTimeIndexes = selectedTimes.map((t) => timeSlots.indexOf(t));

    if (selectedTimes.includes(time)) return true;

    if (selectedTimes.length === 1) {
      return Math.abs(timeIndex - selectedTimeIndexes[0]) === 1;
    }

    return false;
  };

  const getTimeRangeString = () => {
    if (selectedTimes.length === 0) return '시간을 선택해주세요';
    if (selectedTimes.length === 1) return selectedTimes[0];

    const [time1, time2] = selectedTimes.sort();
    const startTime = time1.split('~')[0];
    const endTime = time2.split('~')[1];
    return `${startTime}~${endTime}`;
  };

  const getUserInfoString = () => {
    if (!userInfo.mainUser.name) return '정보를 입력해주세요';
    if (userInfo.participants.length === 0) return userInfo.mainUser.name;
    return `${userInfo.mainUser.name} 외 ${userInfo.participants.length}명`;
  };

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
  
  const renderContent = () => {
    switch (activeTab) {
      case 'room':
        return (
          <div className="px-4 h-full" style={{ height: 'calc(100vh - 280px)' }}>
            <div className="py-4 space-y-3 pb-20">
              {rooms.map((room) => (
                <button
                  key={room.id}
                  onClick={() => {
                    setSelectedRoom(room.id);
                    setBookedSlots([]); // 방 변경 시 예약된 시간 초기화
                  }}
                  className={`
                    w-full rounded-2xl border transition-all
                    ${selectedRoom === room.id 
                      ? 'bg-slate-50 border-2 border-slate-900'
                      : 'bg-white border border-gray-100 hover:border-gray-200'}
                  `}
                >
                  <div className="p-4">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-lg font-bold text-slate-900">{room.name}</span>
                      <span className="text-sm text-gray-500 font-medium">{room.location}</span>
                    </div>
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="flex items-center gap-1 px-2 py-1 bg-gray-50 rounded-lg text-sm text-gray-600">
                        {room.capacity}인실
                      </span>
                      {room.facilities.map((facility, index) => (
                        <span 
                          key={index}
                          className="px-2 py-1 text-sm font-medium text-gray-600 bg-gray-50 rounded-lg"
                        >
                          {facility}
                        </span>
                      ))}
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        );
      case 'time':
        return (
          <div className="p-4 pb-32">
            {timeSlots.map((time) => {
              const isBooked = bookedSlots.includes(time);
              const isSelected = selectedTimes.includes(time);
              const isSelectable = canSelectTime(time);

              return (
                <button
                  key={time}
                  onClick={() => isSelectable && setSelectedTimes([...selectedTimes, time])}
                  disabled={isBooked || (!isSelectable && !isSelected)}
                  className={`
                    w-full mb-2 rounded-2xl border-2 transition-all
                    ${isBooked 
                      ? 'bg-gray-50 border-gray-100 cursor-not-allowed' 
                      : isSelected
                        ? 'bg-slate-900 border-transparent'
                        : !isSelectable
                          ? 'bg-gray-50 border-gray-100 cursor-not-allowed'
                          : 'bg-white border-gray-100 hover:border-gray-200'}
                  `}
                >
                  <div className="p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <Clock className={`w-5 h-5 ${
                        isSelected 
                          ? 'text-white' 
                          : isBooked || !isSelectable
                            ? 'text-gray-300'
                            : 'text-gray-400'
                      }`} />
                      <span className={`font-semibold ${
                        isSelected 
                          ? 'text-white' 
                          : isBooked || !isSelectable
                            ? 'text-gray-400'
                            : 'text-gray-900'
                      }`}>
                        {time}
                      </span>
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        );
      case 'info':
        const maxParticipants = rooms.find(room => room.id === selectedRoom)?.capacity - 1 || 0;
        return (
          <div className="px-4 overflow-y-auto" style={{ height: 'calc(100vh - 280px)' }}>
            <div className="py-4 space-y-6 pb-20">
              {/* 예약자 정보 */}
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold text-slate-900">예약자 정보</h3>
                  <span className="text-sm text-gray-500">필수 입력</span>
                </div>
                <div className="space-y-3">
                  <input
                    type="text"
                    value={userInfo.mainUser.name}
                    onChange={(e) => setUserInfo({
                      ...userInfo,
                      mainUser: { ...userInfo.mainUser, name: e.target.value }
                    })}
                    className="w-full rounded-lg border border-gray-300 px-4 py-2"
                    placeholder="이름"
                  />
                  <input
                    type="text"
                    value={userInfo.mainUser.studentId}
                    onChange={(e) => setUserInfo({
                      ...userInfo,
                      mainUser: { ...userInfo.mainUser, studentId: e.target.value }
                    })}
                    className="w-full rounded-lg border border-gray-300 px-4 py-2"
                    placeholder="학번"
                  />
                </div>
              </div>

              {/* 참여자 정보 */}
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold text-slate-900">참여자 정보</h3>
                  <span className="text-sm text-gray-500">
                    {userInfo.participants.length} / {maxParticipants}
                  </span>
                </div>

                {userInfo.participants.map((participant, index) => (
                  <div key={index} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-gray-600">참여자 {index + 1}</span>
                      <button
                        onClick={() => {
                          const newParticipants = [...userInfo.participants];
                          newParticipants.splice(index, 1);
                          setUserInfo({
                            ...userInfo,
                            participants: newParticipants,
                          });
                        }}
                        className="text-sm text-red-500 hover:text-red-600"
                      >
                        삭제
                      </button>
                    </div>
                    <div className="space-y-2">
                      <input
                        type="text"
                        value={participant.name}
                        onChange={(e) => {
                          const newParticipants = [...userInfo.participants];
                          newParticipants[index] = {
                            ...newParticipants[index],
                            name: e.target.value,
                          };
                          setUserInfo({
                            ...userInfo,
                            participants: newParticipants,
                          });
                        }}
                        className="w-full rounded-lg border border-gray-300 px-4 py-2"
                        placeholder="이름"
                      />
                      <input
                        type="text"
                        value={participant.studentId}
                        onChange={(e) => {
                          const newParticipants = [...userInfo.participants];
                          newParticipants[index] = {
                            ...newParticipants[index],
                            studentId: e.target.value,
                          };
                          setUserInfo({
                            ...userInfo,
                            participants: newParticipants,
                          });
                        }}
                        className="w-full rounded-lg border border-gray-300 px-4 py-2"
                        placeholder="학번"
                      />
                    </div>
                  </div>
                ))}
                {userInfo.participants.length < maxParticipants && (
                  <button
                    onClick={() => {
                      setUserInfo({
                        ...userInfo,
                        participants: [
                          ...userInfo.participants,
                          { name: '', studentId: '' },
                        ],
                      });
                    }}
                    className="w-full py-2 border-2 border-dashed border-gray-300 rounded-lg text-gray-500 hover:border-gray-400 hover:text-gray-600 transition-colors"
                  >
                    + 참여자 추가
                  </button>
                )}
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-b">
        <div className="flex items-center gap-2">
          <button onClick={() => navigate('/')} className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors">
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
          <h2 className="text-lg font-semibold">예약 정보</h2>
          <span className="text-sm text-gray-500">{getCurrentDate()}</span>
        </div>
        <div className="p-4">
          <div className="rounded-2xl border-2 border-slate-900 p-4">
            <div className="flex justify-between items-start mb-3">
              <div className="space-y-1">
                <span className="text-2xl font-bold text-slate-900">
                  {selectedRoom || '스터디룸을 선택해주세요'}
                </span>
              </div>
            </div>
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <Clock className="w-4 h-4 text-slate-900" />
                <span className="text-slate-900 font-semibold">
                  {getTimeRangeString()}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded-full bg-slate-900 flex items-center justify-center">
                  <span className="text-xs text-white font-medium">
                    {userInfo.totalUsers || 0}
                  </span>
                </div>
                <span className="text-slate-900 font-medium">
                  {getUserInfoString()}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="flex border-b bg-white">
        <button
          onClick={() => setActiveTab('room')}
          className={`flex-1 py-4 text-sm font-semibold text-center ${
            activeTab === 'room' ? 'text-black border-b-2 border-black' : 'text-gray-500'
          }`}
        >
          스터디룸
        </button>
        <button
          onClick={() => setActiveTab('time')}
          className={`flex-1 py-4 text-sm font-semibold text-center ${
            activeTab === 'time' ? 'text-black border-b-2 border-black' : 'text-gray-500'
          }`}
        >
          시간 선택
        </button>
        <button
          onClick={() => setActiveTab('info')}
          className={`flex-1 py-4 text-sm font-semibold text-center ${
            activeTab === 'info' ? 'text-black border-b-2 border-black' : 'text-gray-500'
          }`}
        >
          정보 입력
        </button>
      </div>

      {/* Content */}
      <div>{renderContent()}</div>

      {/* Bottom Action */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t">
        <div className="max-w-[480px] w-full mx-auto px-4 py-4">
          <button
            onClick={() => {
              if (activeTab === 'info') {
                handleReservation();
              } else {
                setActiveTab(activeTab === 'room' ? 'time' : 'info');
              }
            }}
            className="w-full bg-slate-900 text-white py-3 text-sm font-medium rounded-xl hover:bg-slate-800 transition-colors"
          >
            {activeTab === 'info' ? '예약하기' : '다음'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default StudyRoomBooking;