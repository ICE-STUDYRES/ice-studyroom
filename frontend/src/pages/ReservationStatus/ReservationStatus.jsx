import React, { useState, useEffect } from "react";
import { ChevronLeft, LogOut, Clock, Users, ChevronDown, ChevronUp } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useMemberHandlers } from '../Mainpage/handlers/MemberHandlers';
import { useNotification } from '../Notification/Notification';
import { useTokenHandler } from "../Mainpage/handlers/TokenHandler";

const ReservationStatus = () => {
  const { addNotification } = useNotification();
  const navigate = useNavigate();
  const [expandedRooms, setExpandedRooms] = useState({});
  const [schedules, setSchedules] = useState([]);
  const {handleLogout} = useMemberHandlers();
  const { refreshTokens } = useTokenHandler();

  useEffect(() => {
    fetchSchedules();
  }, []);

  const fetchSchedules = async () => {
    try {
      let accessToken = sessionStorage.getItem('accessToken')
      if (!accessToken) {
        addNotification('member', 'error');
        navigate("/");
        return;
      }
      const response = await fetch('/api/schedules', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (response.status === 401) {

          accessToken = await refreshTokens();
          if (accessToken) {
              return fetchSchedules();
          }
      }

      if (response.status === 418) {
        navigate('/');
        addNotification("penalty", "error");
      }

      if (!response.ok) {
        throw new Error('Failed to fetch schedules');
      }
      const data = await response.json();
      if (data.code === 'S200') {
        mapSchedulesToRooms(data.data);
      } else {
        throw new Error(data.message);
      }
    } catch {
    }
  };
  
const mapSchedulesToRooms = (scheduleData) => {
  const roomsMap = {};
  const groupMergeMap = {};

  scheduleData.forEach((schedule) => {
    const roomId = schedule.roomNumber;
    const isModified = schedule.createdAt !== schedule.updatedAt;

    if (isModified) {
      if (!groupMergeMap[roomId]) groupMergeMap[roomId] = {};
      if (!groupMergeMap[roomId][schedule.updatedAt]) {
        groupMergeMap[roomId][schedule.updatedAt] = [];
      }
      groupMergeMap[roomId][schedule.updatedAt].push(schedule);
      return;
    }

    if (!roomsMap[roomId]) {
      roomsMap[roomId] = {
        id: roomId,
        name: roomId,
        location: getRoomLocation(roomId),
        facilities: schedule.facilities || [],
        capacity: schedule.capacity || 0,
        reservations: [],
      };
    }

    if (schedule.currentRes >= 1) {
      roomsMap[roomId].reservations.push({
        time: `${schedule.startTime.slice(0, 5)}-${schedule.endTime.slice(0, 5)}`,
        reserver: schedule.reserverEmail,
        participants: `${schedule.currentRes}`,
        status: schedule.status,
        available: schedule.available,
      });
    }
  });

  // 병합된 group 예약 처리
  Object.entries(groupMergeMap).forEach(([roomId, updatedGroups]) => {
    if (!roomsMap[roomId]) {
      roomsMap[roomId] = {
        id: roomId,
        name: roomId,
        location: getRoomLocation(roomId),
        facilities: [],
        capacity: 0,
        reservations: [],
      };
    }

    Object.values(updatedGroups).forEach((groupSchedules) => {
      // currentRes > 0인 예약만 추출
      const validReservations = groupSchedules.filter(s => s.currentRes > 0);

      // 유효한 예약이 하나도 없으면 push하지 않음
      if (validReservations.length === 0) return;

      const sorted = validReservations.sort((a, b) => a.startTime.localeCompare(b.startTime));
      const mergedTime = `${sorted[0].startTime.slice(0, 5)}-${sorted[sorted.length - 1].endTime.slice(0, 5)}`;
      const mergedReserver = [...new Set(sorted.map(s => s.reserverEmail))].join(', ');
      const status = sorted[0].status;
      const available = sorted[0].available;
      const representativeParticipants = `${sorted[0].currentRes}`;

      roomsMap[roomId].reservations.push({
        time: mergedTime,
        reserver: mergedReserver,
        participants: representativeParticipants,
        status: status,
        available: available,
      });
    });

  });

  setSchedules(Object.values(roomsMap));
};


const getRoomLocation = (roomId) => {
  if (roomId.startsWith("3")) return "3층";
  if (roomId.startsWith("4")) return "4층";
  return "알 수 없음";
};


  const formatDate = (date) => {
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const dayOfWeek = days[date.getDay()];
    
    return `${year}.${month}.${day} (${dayOfWeek})`;
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
          <span className="text-sm text-gray-500">{formattedDate}</span>
        </div>
      </div>

      {/* Room List */}
      <div className="px-4 overflow-y-auto" style={{ height: 'calc(100vh - 130px)' }}>
        <div className="py-4 space-y-3">
          {schedules.map((room) => (
            <div
              key={room.id}
              className="w-full rounded-2xl border border-gray-100 bg-white"
            >
              <div className="p-4">
                {/* 방 이름 및 기본 정보 */}
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

                {/* 예약 정보 */}
                <div className="space-y-2">
                  {room.reservations && room.reservations.length > 0 ? (
                    <>
                      {(expandedRooms[room.id]
                        ? room.reservations
                        : room.reservations.slice(0, MAX_VISIBLE_RESERVATIONS)
                      ).map((res, index) => (
                        <div
                          key={index}
                          className={`flex items-center gap-3 text-sm`}
                        >
                          <Clock className="w-4 h-4 text-gray-400" />
                          <span>{res.time}</span>
                          <div className="w-px h-4 bg-gray-300"></div>
                          <span>{res.participants}명</span>
                        </div>
                      ))}

                      {/* 더보기 / 접기 버튼 */}
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
      </div>
    </div>
  );
};

export default ReservationStatus;