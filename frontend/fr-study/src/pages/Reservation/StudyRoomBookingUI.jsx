import React from 'react';
import { ChevronLeft, Clock, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useStudyRoomBooking } from './useStudyRoomBookings';

const StudyRoomBookingUI = () => {
  const {
    activeTab,
    setActiveTab,
    selectedTimes,
    selectedRoom,
    setSelectedRoom,
    bookedSlots,
    userInfo,
    setUserInfo,
    rooms,
    timeSlots,
    handleReservation,
    handleTimeClick,
    canSelectTime,
  } = useStudyRoomBooking();

  const navigate = useNavigate();

  const getTimeRangeString = () => {
    if (selectedTimes.length === 0) return '시간을 선택해주세요';
    if (selectedTimes.length === 1) return selectedTimes[0];

    const sortedTimes = selectedTimes.sort();
    const startTime = sortedTimes[0].split('~')[0];
    const endTime = sortedTimes[sortedTimes.length - 1].split('~')[1];
    return `${startTime}~${endTime}`;
  };

  const getUserInfoString = () => {
    if (!userInfo.mainUser.name) return '예약자 정보를 입력해주세요';
    if (userInfo.participants.length === 0) return userInfo.mainUser.name;
    return `${userInfo.mainUser.name} 외 ${userInfo.participants.length}명`;
  };



  // Render content for each tab
  const renderContent = () => {
    switch (activeTab) {
      case 'room':
        return (
          <div className="space-y-3">
            {rooms.map((room) => (
              <button
                key={room.id}
                onClick={() => setSelectedRoom(room.name)}
                className={`
                  w-full rounded-2xl border transition-all
                  ${selectedRoom === room.name 
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
        );
      
      case 'time':
        return (
          <div className="p-4 pb-32">
            {!selectedRoom ? (
              <div className="text-center py-8 text-gray-500">
                스터디룸을 먼저 선택해주세요
              </div>
            ) : (
              <>
                {timeSlots.map((time) => {
                  const roomId = rooms.find((room) => room.name === selectedRoom)?.id;
                  const isBooked = bookedSlots[roomId]?.[time]?.available === false;
                  const isSelected = selectedTimes.includes(time);
                  const isSelectable = canSelectTime(time);

                  return (
                    <button
                      key={time}
                      onClick={() => {
                        if (isBooked) return;
                        handleTimeClick(time);
                      }}
                      disabled={isBooked}
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
              </>
            )}
          </div>
        );
      
      case 'info':
        if (!selectedRoom || selectedTimes.length === 0) {
          return (
            <div className="text-center py-8 text-gray-500">
              스터디룸과 시간을 먼저 선택해주세요
            </div>
          );
        }
        const maxParticipants = rooms.find(room => room.name === selectedRoom)?.capacity - 1 || 0;
        return (
          <div className="space-y-6 p-4">
            <div className="space-y-6">
            <h3 className="text-lg font-bold text-slate-900">예약자 정보</h3>
            <div className="space-y-3">
              <input
              type="text"
              value={userInfo.mainUser.name}
              readOnly
              className="w-full rounded-lg border border-gray-300 px-4 py-2 bg-gray-100 text-gray-500 cursor-not-allowed"
              placeholder="이름"
              />
            <input
            type="text"
            value={userInfo.mainUser.email}
            readOnly
            className="w-full rounded-lg border border-gray-300 px-4 py-2 bg-gray-100 text-gray-500 cursor-not-allowed"
            placeholder="이메일"
            />
          </div>
        </div>


            <div className="space-y-6">
              <h3 className="text-lg font-bold text-slate-900">참여자 정보</h3>
              <div className="space-y-4">
                {userInfo.participants.map((participant, index) => (
                  <div key={index} className="space-y-2 relative">
                    <input
                      type="text"
                      value={participant.name}
                      onChange={(e) => {
                        const newParticipants = [...userInfo.participants];
                        newParticipants[index].name = e.target.value;
                        setUserInfo({ ...userInfo, participants: newParticipants });
                      }}
                      className="w-full rounded-lg border border-gray-300 px-4 py-2"
                      placeholder="이름"
                    />
                    <input
                      type="text"
                      value={participant.email}
                      onChange={(e) => {
                        const newParticipants = [...userInfo.participants];
                        newParticipants[index].email = e.target.value;
                        setUserInfo({ ...userInfo, participants: newParticipants });
                      }}
                      className="w-full rounded-lg border border-gray-300 px-4 py-2"
                      placeholder="이메일"
                    />
                    <button
                      onClick={() => {
                        const newParticipants = [...userInfo.participants];
                        newParticipants.splice(index, 1);
                        setUserInfo({ ...userInfo, participants: newParticipants });
                      }}
                      className="absolute -right-2 -top-2 w-6 h-6 bg-gray-500 text-white rounded-full hover:bg-gray-600 flex items-center justify-center text-sm"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
              {userInfo.participants.length < maxParticipants && (
                <button
                  onClick={() => setUserInfo({
                    ...userInfo,
                    participants: [...userInfo.participants, { name: '', email: '' }]
                  })}
                  className="w-full py-2 border-2 border-dashed border-gray-300 rounded-lg text-gray-500 hover:border-gray-400 hover:text-gray-600 transition-colors"
                >
                  + 참여자 추가
                </button>
              )}
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="fixed inset-0 max-w-[480px] mx-auto bg-gray-50 flex flex-col">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-b">
        <div className="flex items-center gap-2">
          <button onClick={() => navigate('/')} className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors">
            <ChevronLeft className="w-5 h-5 text-gray-700" />
          </button>
          <h1 className="font-semibold text-gray-900">스터디룸 예약</h1>
        </div>
        <button className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700">
          <LogOut className="w-4 h-4" />
          로그아웃
        </button>
      </div>

      {/* Booking Info */}
      <div className="bg-white border-b">
        <div className="px-4 py-3 flex items-center justify-between">
          <h2 className="text-lg font-semibold">예약 정보</h2>
          <span className="text-sm text-gray-500">{new Date().toLocaleDateString()}</span>
        </div>
        <div className="p-4">
          <div className="rounded-2xl border-2 border-slate-900 p-4">
            <span className="text-2xl font-bold text-slate-900">
              {selectedRoom || '스터디룸을 선택해주세요'}
            </span>
            <div className="mt-3">
              <Clock className="w-4 h-4 text-slate-900 inline-block mr-1" />
              <span className="text-slate-900 font-semibold">
                {getTimeRangeString()}
              </span>
            </div>
            <div className="mt-2">
              <span className="text-slate-900 font-medium">
                {getUserInfoString()}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Tab Navigation */}
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
      <div className="flex-1 overflow-y-auto">
        {renderContent()}
      </div>

      {/* Bottom Action */}
      <div className="bg-white border-t">
        <div className="px-4 py-4">
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

export default StudyRoomBookingUI;
