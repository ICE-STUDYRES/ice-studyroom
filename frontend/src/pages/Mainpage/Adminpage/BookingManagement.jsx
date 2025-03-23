import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import axios from 'axios';

const getTodayDayOfWeek = () => {
  const today = new Date();
  return new Intl.DateTimeFormat('ko-KR', { weekday: 'short' }).format(today);
};

const DAYS = {
  "MONDAY": "월",
  "TUESDAY": "화",
  "WEDNESDAY": "수",
  "THURSDAY": "목",
  "FRIDAY": "금",
  "SATURDAY": "토",
  "SUNDAY": "일"
};

const ENGLISH_DAYS = Object.fromEntries(
  Object.entries(DAYS).map(([eng, kor]) => [kor, eng])
);

const BookingManagement = ({ rooms }) => {
  const [selectedDay, setSelectedDay] = useState(getTodayDayOfWeek());
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [bookings, setBookings] = useState({});
  const [mergedBookings, setMergedBookings] = useState({});

  useEffect(() => {
    const fetchBookings = async () => {
      try {
        let accessToken = sessionStorage.getItem("accessToken");
        if (!accessToken) {
          return;
        }
        const response = await axios.get('api/admin/room-time-slots/occupy-reserved', {
          headers: {
            Authorization: `Bearer ${accessToken}`
          }
        });

        const apiData = response.data.data;

        const formatTimeRange = (startTime) => {
          const [hour, minute] = startTime.split(':').map(Number);
          const endHour = hour + 1;
          return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}-${endHour.toString().padStart(2, '0')}:00`;
        };

        const rawBookings = {};

        apiData.forEach(({ roomNumber, startTime, dayOfWeek }) => {
          const day = DAYS[dayOfWeek] || dayOfWeek;
          const time = formatTimeRange(startTime);
        
          if (!rawBookings[day]) rawBookings[day] = {};
          if (!rawBookings[day][roomNumber]) rawBookings[day][roomNumber] = [];
        
          rawBookings[day][roomNumber].push(time);
        });

        setBookings(rawBookings);

        const mergeTimes = (times) => {
          if (!times || times.length === 0) return [];
          const sortedTimes = [...times].sort();
          const merged = [];
          let start = sortedTimes[0].split('-')[0];
          let end = sortedTimes[0].split('-')[1];

          for (let i = 1; i < sortedTimes.length; i++) {
            const [currentStart, currentEnd] = sortedTimes[i].split('-');

            if (currentStart === end) {
              end = currentEnd;
            } else {
              merged.push(`${start}-${end}`);
              start = currentStart;
              end = currentEnd;
            }
          }

          merged.push(`${start}-${end}`);
          return merged;
        };

        const formattedMergedBookings = Object.keys(rawBookings).reduce((acc, day) => {
          acc[day] = {};
          Object.keys(rawBookings[day]).forEach(room => {
            acc[day][room] = mergeTimes(rawBookings[day][room]);
          });
          return acc;
        }, {});

        setMergedBookings(formattedMergedBookings);
      } catch (error) {
        console.error('예약 정보를 불러오는 중 오류 발생:', error);
      }
    };

    fetchBookings();
  }, []);

  const handleRoomClick = (roomId) => {
    setSelectedRoom(roomId);
    setSelectedTimes([]);
    setIsModalOpen(true);
  };

  const handleTimeSelect = (time) => {
    setSelectedTimes(prev => {
      if (prev.includes(time)) {
        return prev.filter(t => t !== time);
      }
      return [...prev, time].sort((a, b) => {
        const timeA = parseInt(a.split('-')[0].replace(':', ''));
        const timeB = parseInt(b.split('-')[0].replace(':', ''));
        return timeA - timeB;
      });
    });
  };

  const fetchSchedules = async () => {
    const accessToken = sessionStorage.getItem("accessToken");
    const englishDay = ENGLISH_DAYS[selectedDay];
    const response = await axios.get(`/api/admin/room-time-slots?dayOfWeek=${englishDay}`, {
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    });
  
    const allSlots = response.data.data;
  
    const selectedIds = allSlots
      .filter(item => {
        const start = item.startTime.substring(0, 5).padStart(5, '0');
        const end = item.endTime.substring(0, 5).padStart(5, '0');
        const timeRange = `${start}-${end}`;
        return (
          item.roomNumber === selectedRoom &&
          selectedTimes.includes(timeRange)
        );
      })
      .map(item => item.id);
  
    return selectedIds;
  };
  

  const handleRelease = async () => {
    if (selectedTimes.length === 0 || !selectedRoom) return;
  
    try {
      const accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) return;
  
      const selectedIds = await fetchSchedules();
      if (selectedIds.length === 0) {
        console.warn("⚠️ 해제할 slot ID 없음");
        return;
      }

      const englishDay = ENGLISH_DAYS[selectedDay];
  
      const requestBody = {
        roomTimeSlotId: selectedIds,
        dayOfWeek: englishDay
      };      
  
      const response = await fetch("/api/admin/room-time-slots/release", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`
        },
        body: JSON.stringify(requestBody)
      });
  
      const responseData = await response.json();
      if (!response.ok) throw new Error(responseData.message || "해제 요청 실패");
  
      setBookings(prev => ({
        ...prev,
        [selectedDay]: {
          ...prev[selectedDay],
          [selectedRoom]: prev[selectedDay][selectedRoom].filter(
            time => !selectedTimes.includes(time)
          )
        }
      }));
  
      setIsModalOpen(false);
      setSelectedTimes([]);
      alert("해제가 완료되었습니다!");
      window.location.reload();
    } catch (error) {
      console.error("❌ 해제 실패:", error.message);
      alert("해제에 실패했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-semibold text-gray-900">예약 관리</h2>
      </div>

      {/* 요일 탭 */}
      <div className="flex gap-2 mb-6">
        {Object.values(DAYS).map(day => (
          <button
            key={day}
            onClick={() => setSelectedDay(day)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              selectedDay === day ? 'bg-gray-900 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:border-gray-400'
            }`}
          >
            {day}요일
          </button>
        ))}
      </div>

      {/* 스터디룸 카드 목록 */}
      <div className="grid grid-cols-2 gap-4">
        {rooms.map(room => (
          <div key={room.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold text-gray-900">{room.id}</h3>
              <button onClick={() => handleRoomClick(room.id)} className="px-3 py-1.5 text-sm font-medium text-gray-600 hover:text-gray-900">
                시간별 해제
              </button>
            </div>
            <div className="space-y-2">
              <div className="text-sm text-gray-500 mb-2">선점된 시간:</div>
              {mergedBookings[selectedDay]?.[room.id]?.length > 0 ? (
                <div className="grid grid-cols-2 gap-2">
                  {mergedBookings[selectedDay][room.id].map(time => (
                    <div key={time} className="px-3 py-1.5 bg-gray-50 rounded-lg text-sm text-gray-600">{time}</div>
                  ))}
                </div>
              ) : (
                <div className="text-sm text-gray-400">선점된 시간이 없습니다</div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* 시간별 해제 모달 */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-xl shadow-lg w-[400px]">
            <div className="flex justify-between items-center p-6 border-b border-gray-200">
              <h3 className="text-lg font-semibold text-gray-900">
                {selectedRoom} - {selectedDay}요일
              </h3>
              <button 
                onClick={() => {
                  setIsModalOpen(false);
                  setSelectedTimes([]);
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <X size={20} />
              </button>
            </div>
            
            <div className="p-6 max-h-[60vh] overflow-y-auto">
              {bookings[selectedDay][selectedRoom]?.length > 0 ? (
                <div className="space-y-2">
                  {bookings[selectedDay][selectedRoom].map(time => (
                    <label
                      key={time}
                      className="flex items-center space-x-3 p-3 rounded-lg hover:bg-gray-50"
                    >
                      <input
                        type="checkbox"
                        checked={selectedTimes.includes(time)}
                        onChange={() => handleTimeSelect(time)}
                        className="h-5 w-5 rounded border-gray-300 text-gray-900 focus:ring-gray-900"
                      />
                      <span className="text-sm font-medium text-gray-700">
                        {time}
                      </span>
                    </label>
                  ))}
                </div>
              ) : (
                <div className="text-center text-sm text-gray-500 py-4">
                  선점된 시간이 없습니다
                </div>
              )}
            </div>

            <div className="flex justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
              <button 
                onClick={() => {
                  setIsModalOpen(false);
                  setSelectedTimes([]);
                }}
                className="px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-800 transition-colors"
              >
                취소
              </button>
              <button
                onClick={handleRelease}
                disabled={selectedTimes.length === 0}
                className="px-4 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition-colors disabled:bg-gray-100 disabled:text-gray-400 disabled:cursor-not-allowed"
              >
                선택 해제하기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookingManagement;