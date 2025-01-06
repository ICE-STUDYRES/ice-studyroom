import React, { useState } from 'react';
import { ChevronLeft, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const StudyRoomBooking = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('room');
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState('');
  const [userInfo, setUserInfo] = useState({
    mainUser: { name: '', studentId: '' },
    participants: [],
    totalUsers: 1
  });

  const getMaxParticipants = () => {
    const selectedRoomData = rooms.find(room => room.id === selectedRoom);
    return selectedRoomData?.id.startsWith('409') ? 6 : 4;
  };

  const tabs = [
    { id: 'room', label: '스터디룸' },
    { id: 'time', label: '시간 선택' },
    { id: 'info', label: '정보 입력' },
  ];

  const rooms = [
    { id: '305-1', name: '305-1', capacity: 4, facilities: ['PC'], location: '3층' },
    { id: '305-2', name: '305-2', capacity: 4, facilities: ['PC'], location: '3층' },
    { id: '305-3', name: '305-3', capacity: 4, facilities: ['PC'], location: '3층' },
    { id: '305-4', name: '305-4', capacity: 4, facilities: ['PC'], location: '3층' },
    { id: '305-5', name: '305-5', capacity: 4, facilities: ['PC'], location: '3층' },
    { id: '305-6', name: '305-6', capacity: 4, facilities: ['PC'], location: '3층' },
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
  ];

  const handleTimeSelection = (selectedTime) => {
    const timeIndex = timeSlots.indexOf(selectedTime);
    
    if (timeIndex === -1) return;

    if (selectedTimes.includes(selectedTime)) {
      setSelectedTimes(selectedTimes.filter(time => time !== selectedTime));
    } else {
      // 최대 2시간까지 선택 가능
      if (selectedTimes.length < 2) {
        // 이미 선택된 시간이 있는 경우
        if (selectedTimes.length === 1) {
          const existingTimeIndex = timeSlots.indexOf(selectedTimes[0]);
          // 인접한 시간만 선택 가능
          if (Math.abs(timeIndex - existingTimeIndex) === 1 && selectedTime !== '12:00~13:00') {
            setSelectedTimes([...selectedTimes, selectedTime].sort());
          }
        } else {
          // 첫 선택인 경우
          if (selectedTime !== '12:00~13:00') {
            setSelectedTimes([selectedTime]);
          }
        }
      }
    }
  };

  const canSelectTime = (time) => {
    if (time === '12:00~13:00') return false;
    
    if (selectedTimes.length === 0) return true;
    
    const timeIndex = timeSlots.indexOf(time);
    const selectedTimeIndexes = selectedTimes.map(t => timeSlots.indexOf(t));
    
    // 이미 선택된 시간인 경우 선택 가능
    if (selectedTimes.includes(time)) return true;
    
    // 선택된 시간이 하나이고, 인접한 시간인 경우 선택 가능
    if (selectedTimes.length === 1) {
      return Math.abs(timeIndex - selectedTimeIndexes[0]) === 1;
    }
    
    return false;
  };

  const getTimeRangeString = () => {
    if (selectedTimes.length === 0) return "시간을 선택해주세요";
    if (selectedTimes.length === 1) return selectedTimes[0];
    
    const [time1, time2] = selectedTimes.sort();
    const startTime = time1.split('~')[0];
    const endTime = time2.split('~')[1];
    return `${startTime}~${endTime}`;
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'room':
        return (
          <div className="px-4 overflow-y-auto" style={{ height: 'calc(100vh - 280px)' }}>
            <div className="py-4 space-y-3 pb-20">
              {rooms.map((room) => (
                <button
                  key={room.id}
                  onClick={() => setSelectedRoom(room.id)}
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
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                        </svg>
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
          <div className="p-4">
            {timeSlots.map((time) => {
              const isBooked = time === '12:00~13:00';
              const isSelected = selectedTimes.includes(time);
              const isSelectable = canSelectTime(time);

              return (
                <button
                  key={time}
                  onClick={() => isSelectable && handleTimeSelection(time)}
                  disabled={isBooked || (!isSelectable && !isSelected)}
                  className={`
                    w-full mb-2 rounded-2xl border-2 transition-all
                    ${isBooked 
                      ? 'bg-gray-50 border-gray-100 cursor-not-allowed' 
                      : isSelected
                        ? 'bg-slate-900 border-transparent'
                        : !isSelectable
                          ? 'bg-gray-50 border-gray-100 cursor-not-allowed'
                          : 'bg-white border-gray-100 hover:border-gray-200'
                    }
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
        const maxParticipants = getMaxParticipants();
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
                    {userInfo.participants.length} / {maxParticipants - 1}
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
                            totalUsers: newParticipants.length + 1
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
                            name: e.target.value
                          };
                          setUserInfo({
                            ...userInfo,
                            participants: newParticipants
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
                            studentId: e.target.value
                          };
                          setUserInfo({
                            ...userInfo,
                            participants: newParticipants
                          });
                        }}
                        className="w-full rounded-lg border border-gray-300 px-4 py-2"
                        placeholder="학번"
                      />
                    </div>
                  </div>
                ))}

                {userInfo.participants.length < maxParticipants - 1 && (
                  <button
                    onClick={() => {
                      setUserInfo({
                        ...userInfo,
                        participants: [
                          ...userInfo.participants,
                          { name: '', studentId: '' }
                        ],
                        totalUsers: userInfo.participants.length + 2
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

  const getUserInfoString = () => {
    if (!userInfo.mainUser.name) return "정보를 입력해주세요";
    if (userInfo.participants.length === 0) return userInfo.mainUser.name;
    return `${userInfo.mainUser.name} 외 ${userInfo.participants.length}명`;
  };

  return (
    <div className="w-[390px] mx-auto min-h-screen bg-gray-50">
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
        <button className="text-sm text-gray-500 hover:text-gray-700">
          로그아웃
        </button>
      </div>

      {/* Booking Info */}
      <div className="bg-white border-b">
        <div className="px-4 py-3 flex items-center justify-between border-b">
          <h2 className="text-lg font-semibold">예약 정보</h2>
          <span className="text-sm text-gray-500">2024.01.03 (수)</span>
        </div>
        
        <div className="p-4">
          <div className="rounded-2xl border-2 border-slate-900 p-4">
            <div className="flex justify-between items-start mb-3">
              <div className="space-y-1">
                <span className="text-2xl font-bold text-slate-900">
                  {selectedRoom || "스터디룸을 선택해주세요"}
                </span>
                {selectedRoom && (
                  <div className="flex flex-wrap gap-1">
                    {rooms.find(room => room.id === selectedRoom)?.facilities.map((facility, index) => (
                      <span
                        key={index}
                        className="px-2 py-0.5 text-xs font-medium bg-slate-100 text-slate-700 rounded-full"
                      >
                        {facility}
                      </span>
                    ))}
                  </div>
                )}
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

      {/* Tab Navigation */}
      <div className="flex border-b bg-white relative">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`
              flex-1 py-4 text-sm font-semibold relative
              ${activeTab === tab.id ? 'text-slate-900' : 'text-gray-500'}
            `}
          >
            {tab.label}
            {activeTab === tab.id && (
              <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-slate-900" />
            )}
          </button>
        ))}
      </div>

      {/* Content */}
      {renderContent()}

      {/* Bottom Action */}
      {((activeTab === 'room' && selectedRoom) ||
        (activeTab === 'time' && selectedTimes.length > 0) ||
        (activeTab === 'info' && userInfo.mainUser && userInfo.totalUsers > 0)) && (
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t">
          <div className="w-[390px] mx-auto px-4 py-4">
            <button 
              onClick={() => {
                if (activeTab === 'room') setActiveTab('time');
                else if (activeTab === 'time') setActiveTab('info');
                // info 탭에서는 최종 예약 처리
              }}
              className="w-full bg-slate-900 text-white py-3 text-sm font-medium rounded-xl hover:bg-slate-800 transition-colors"
            >
              {activeTab === 'info' ? '예약하기' : '다음'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudyRoomBooking;