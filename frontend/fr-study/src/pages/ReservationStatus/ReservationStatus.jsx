import React, { useState, useEffect } from "react";
import { ChevronLeft, LogOut, Clock, Users, ChevronDown, ChevronUp } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useMainpageHandlers } from '../Mainpage/MainpageHandlers';

const rooms = [
  { id: "305-1", name: "305-1", details: "4인실 | PC, 모니터", location: "3층", facilities: ["PC", "모니터"], capacity: 4, reservations: [] },
  { id: "305-2", name: "305-2", details: "4인실 | PC, 모니터", location: "3층", facilities: ["PC", "모니터"], capacity: 4, reservations: [] },
  { id: "305-3", name: "305-3", details: "4인실 | PC, 모니터", location: "3층", facilities: ["PC", "모니터"], capacity: 4, reservations: [] },
  { id: "305-4", name: "305-4", details: "4인실 | PC, 모니터", location: "3층", facilities: ["PC", "모니터"], capacity: 4, reservations: [] },
  { id: "305-5", name: "305-5", details: "4인실 | PC, 모니터", location: "3층", facilities: ["PC", "모니터"], capacity: 4, reservations: [] },
  { id: "305-6", name: "305-6", details: "4인실 | PC, 모니터", location: "3층", facilities: ["PC", "모니터"], capacity: 4, reservations: [] },
  { id: "409-1", name: "409-1", details: "4인실 | PC, 대형 모니터", location: "4층", facilities: ["PC", "대형 모니터", "화이트보드"], capacity: 6, reservations: [] },
  { id: "409-2", name: "409-2", details: "4인실 | PC, 대형 모니터", location: "4층", facilities: ["PC", "대형 모니터", "화이트보드"], capacity: 6, reservations: [] },
];

const ReservationStatus = () => {
  const navigate = useNavigate();
  const [expandedRooms, setExpandedRooms] = useState({});
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAllSchedules, setShowAllSchedules] = useState(false);
  const [myReservations, setMyReservations] = useState([]);
  const [myReservationsLoading, setMyReservationsLoading] = useState(true);
  const [myReservationsError, setMyReservationsError] = useState(null);
  const {handleLogout} = useMainpageHandlers();

  const handleLogoutClick = async () => {
    try {
      await handleLogout();
      navigate('/');  // 메인 페이지로 이동
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  useEffect(() => {
    fetchSchedules();
  }, []);

  const fetchSchedules = async () => {
    try {
      const response = await fetch('/api/schedules');
      if (!response.ok) {
        throw new Error('Failed to fetch schedules');
      }
      const data = await response.json();
      if (data.code === 'S200') {
        mapSchedulesToRooms(data.data);
      } else {
        throw new Error(data.message);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const mapSchedulesToRooms = (scheduleData) => {
    const mappedRooms = rooms.map(room => {
      const roomSchedules = scheduleData.filter(
        schedule => schedule.roomNumber === room.id
      ).map(schedule => ({
        time: `${schedule.startTime.slice(0, 5)}-${schedule.endTime.slice(0, 5)}`,
        reserver: schedule.status === 'RESERVED' ? '예약됨' : '가능',
        participants: schedule.capacity,
        status: schedule.status,
        available: schedule.available
      }));

      return {
        ...room,
        reservations: roomSchedules
      };
    });

    setSchedules(mappedRooms);
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

  const toggleRoomExpansion = (roomId) => {
    setExpandedRooms(prev => ({
      ...prev,
      [roomId]: !prev[roomId]
    }));
  };

  const MAX_VISIBLE_RESERVATIONS = 2;

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

      {/* Room List */}
      <div className="px-4 overflow-y-auto" style={{ height: 'calc(100vh - 130px)' }}>
        <div className="py-4 space-y-3">
          {rooms.map((room) => (
            <div
              key={room.id}
              className="w-full rounded-2xl border border-gray-100 bg-white"
            >
              <div className="p-4">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-lg font-bold text-slate-900">{room.name}</span>
                  <span className="text-sm text-gray-500 font-medium">{room.location}</span>
                </div>
                
                <div className="flex flex-wrap items-center gap-2 mb-3">
                  <span className="flex items-center gap-1 px-2 py-1 bg-gray-50 rounded-lg text-sm text-gray-600">
                    <Users className="w-4 h-4" />
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

                <div className="space-y-2">
                  {loading ? (
                    <p className="text-sm text-gray-500">로딩 중...</p>
                  ) : error ? (
                    <p className="text-sm text-red-500">오류가 발생했습니다.</p>
                  ) : room.reservations && room.reservations.length > 0 ? (
                    <>
                      {(expandedRooms[room.id] 
                        ? room.reservations 
                        : room.reservations.slice(0, MAX_VISIBLE_RESERVATIONS)
                      ).map((res, index) => (
                        <div 
                          key={index}
                          className={`flex items-center gap-3 text-sm ${
                            res.status === 'RESERVED' ? 'text-gray-900' : 'text-green-600'
                          }`}
                        >
                          <Clock className="w-4 h-4 text-gray-400" />
                          <span>{res.time}</span>
                          <span>
                            {res.status === 'RESERVED' ? '예약됨' : '예약 가능'} / {res.participants}명
                          </span>
                        </div>
                      ))}
                      
                      {room.reservations.length > MAX_VISIBLE_RESERVATIONS && (
                        <button
                          onClick={() => toggleRoomExpansion(room.id)}
                          className="flex items-center gap-1 mt-2 text-sm text-gray-500 hover:text-gray-700"
                        >
                          {expandedRooms[room.id] ? (
                            <>
                              <ChevronUp className="w-4 h-4" />
                              접기
                            </>
                          ) : (
                            <>
                              <ChevronDown className="w-4 h-4" />
                              더보기 ({room.reservations.length - MAX_VISIBLE_RESERVATIONS}개)
                            </>
                          )}
                        </button>
                      )}
                    </>
                  ) : (
                    <p className="text-sm text-gray-500">예약된 정보가 없습니다.</p>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Buttons Section */}
        <div className="mt-8 mb-20 space-y-3">
          <button
            onClick={() => setShowAllSchedules(!showAllSchedules)}
            className="w-full py-2 px-4 bg-white rounded-lg border border-gray-200 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            {showAllSchedules ? "전체 스케줄 숨기기" : "전체 스케줄 보기"}
          </button>
          
          {/* All Schedules Section */}
          {showAllSchedules && (
            <div className="mt-4">
              <div className="bg-white rounded-lg border border-gray-200 p-4">
                <h3 className="text-lg font-semibold mb-4">전체 예약 현황</h3>
                {loading ? (
                  <p className="text-sm text-gray-500">로딩 중...</p>
                ) : error ? (
                  <p className="text-sm text-red-500">오류가 발생했습니다.</p>
                ) : (
                  <div className="space-y-4">
                    {schedules.map((room) => (
                      <div key={room.id} className="border-t border-gray-100 pt-4 first:border-t-0 first:pt-0">
                        <h4 className="font-medium mb-2">{room.name} ({room.location})</h4>
                        <div className="space-y-2">
                          {room.reservations.length > 0 ? (
                            room.reservations.map((res, index) => (
                              <div 
                                key={index}
                                className={`flex items-center gap-3 text-sm ${
                                  res.status === 'RESERVED' ? 'text-gray-900' : 'text-green-600'
                                }`}
                              >
                                <Clock className="w-4 h-4 text-gray-400" />
                                <span>{res.time}</span>
                                <span>
                                  {res.status === 'RESERVED' ? '예약됨' : '예약 가능'} / {res.participants}명
                                </span>
                              </div>
                            ))
                          ) : (
                            <p className="text-sm text-gray-500">예약된 정보가 없습니다.</p>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ReservationStatus;