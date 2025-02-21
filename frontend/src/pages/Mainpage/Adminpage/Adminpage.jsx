import React from 'react';
import { Home, LogOut, Monitor } from 'lucide-react';
import useAdminPageHandler from './useAdminPageHandler';
import PenaltyManagement from './PenaltyManagement';
import BookingManagement from './BookingManagement';

const AdminPage = () => {
  const {
    activeTab,
    selectedRoom,
    selectedTimes,
    formattedSelectedTimes,
    rooms,
    timeSlots,
    penaltyData,
    availableRoomsCount,
    handleTabChange,
    handleRoomSelect,
    handleTimeSelect
  } = useAdminPageHandler();

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="w-full bg-gray-900 text-white shadow-md">
        <div className="w-[1024px] mx-auto flex justify-between items-center h-16">
          <div className="flex items-center gap-6">
            <button className="p-2 hover:bg-gray-800 rounded-lg transition-colors">
              <Home size={22} />
            </button>
            <h1 className="text-xl tracking-tight font-semibold">정보통신공학과 스터디룸</h1>
          </div>
          <button className="p-2 hover:bg-gray-800 rounded-lg transition-colors">
            <LogOut size={22} />
          </button>
        </div>
      </header>

      <nav className="w-full bg-white border-b border-gray-200">
        <div className="w-[1024px] mx-auto flex gap-10 h-14">
          {['booking', 'penalty', 'management'].map((tab) => (
            <button 
              key={tab}
              className={`px-2 font-medium text-sm tracking-wide transition-colors ${
                activeTab === tab 
                  ? 'text-gray-900 border-b-2 border-gray-900' 
                  : 'text-gray-600 hover:text-gray-900'
              }`}
              onClick={() => handleTabChange(tab)}>
              {tab === 'booking' ? '예약하기' : 
               tab === 'penalty' ? '패널티 관리' : '예약 관리'}
            </button>
          ))}
        </div>
      </nav>

      <main className="w-[1024px] mx-auto py-8">
        <div className="w-full">
          {activeTab === 'penalty' ? (
            <PenaltyManagement penaltyData={penaltyData} />
          ) : activeTab === 'management' ? (
            <BookingManagement rooms={rooms} timeSlots={timeSlots} />
          ) : (
            <div className="flex gap-8">
              <div className="flex-1">
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-xl font-semibold text-gray-900">스터디룸 목록</h2>
                  <div className="text-sm bg-white px-4 py-2 rounded-full shadow-sm">
                    <span className="text-gray-900">전체 {availableRoomsCount.total}개</span>
                    <span className="mx-2 text-gray-300">|</span>
                    <span className="text-gray-900">예약가능 {availableRoomsCount.available}개</span>
                  </div>
                </div>

                <div className="h-[calc(100vh-280px)] overflow-y-auto pr-4 -mr-4">
                  <div className="space-y-4">
                    {rooms.map(room => (
                      <div key={room.id} 
                        className={`p-5 rounded-xl shadow-sm transition-all duration-200 ${
                          room.status === 'selected' 
                            ? 'bg-white border-2 border-gray-900 ring-4 ring-gray-100' 
                            : room.status === 'unavailable' 
                              ? 'bg-gray-50' 
                              : 'bg-white border border-gray-200 hover:border-gray-400'
                        }`}>
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <span className="text-lg font-semibold text-gray-900">{room.id}</span>
                            {room.starred && <span className="text-yellow-400 text-lg">★</span>}
                          </div>
                          <div className="flex items-center gap-4">
                            <div className="flex items-center gap-2 bg-gray-50 px-3 py-1.5 rounded-full">
                              <span className="text-sm font-medium text-gray-600">
                                {room.capacity}명
                              </span>
                            </div>
                            <Monitor className="text-gray-400" size={20} />
                          </div>
                        </div>

                        <div className="flex flex-wrap gap-2 mt-3">
                          {room.features.map(feature => (
                            <span key={feature} 
                              className="px-3 py-1.5 text-sm font-medium text-gray-600 bg-gray-50 rounded-full">
                              {feature}
                            </span>
                          ))}
                        </div>

                        <button 
                          onClick={() => handleRoomSelect(room.id)}
                          className={`w-full mt-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                            room.status === 'selected' 
                              ? 'bg-gray-900 text-white hover:bg-gray-800' 
                              : room.status === 'unavailable' 
                                ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                                : 'bg-white border border-gray-200 text-gray-700 hover:border-gray-400 hover:bg-gray-50'
                          }`}
                          disabled={room.status === 'unavailable'}>
                          {room.status === 'selected' ? '선택됨' :
                           room.status === 'unavailable' ? '예약불가' : '선택하기'}
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="w-80">
                <div className="mb-8">
                  <h2 className="text-xl font-semibold text-gray-900 mb-4">시간 선택</h2>
                  <div className="h-[calc(100vh-520px)] overflow-y-auto pr-4 -mr-4">
                    <div className="space-y-2">
                    {timeSlots.map(time => (
                      <button
                        key={time}
                        onClick={() => handleTimeSelect(time)}
                        className={`w-full p-3.5 rounded-lg text-sm font-medium transition-colors ${
                          selectedTimes.includes(time)
                            ? 'bg-gray-900 text-white' 
                            : 'bg-white border border-gray-200 text-gray-700 hover:border-gray-400 hover:bg-gray-50'
                        }`}
                      >
                        {time}
                      </button>
                    ))}
                    </div>
                  </div>
                </div>

                <div>
                  <h2 className="text-xl font-semibold text-gray-900 mb-4">예약 정보</h2>
                  <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                    <div className="mb-4">
                      <div className="text-sm font-medium text-gray-500 mb-1.5">선택한 스터디룸</div>
                      <div className="text-lg font-semibold text-gray-900">{selectedRoom}</div>
                    </div>
                    <div className="mb-6">
                      <div className="text-sm font-medium text-gray-500 mb-1.5">선택한 시간</div>
                      <div className="space-y-1">
                        {formattedSelectedTimes.map((timeRange, index) => (
                          <div key={index} className="text-lg font-semibold text-gray-900">
                            {timeRange}
                          </div>
                        ))}
                      </div>
                    </div>
                    <button 
                      className={`w-full py-3 text-sm font-medium rounded-lg transition-colors ${
                        selectedTimes.length > 0
                          ? 'bg-gray-900 text-white hover:bg-gray-800'
                          : 'bg-gray-100 text-gray-400 cursor-not-allowed'
                      }`}
                      disabled={selectedTimes.length === 0}
                    >
                      {selectedTimes.length > 0 ? '예약하기' : '시간을 선택해주세요'}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default AdminPage;