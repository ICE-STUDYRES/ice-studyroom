import { useState, useMemo, useCallback, useEffect } from 'react';

const useAdminPageHandler = () => {
  // State
  const [activeTab, setActiveTab] = useState('booking');
  const [selectedRoom, setSelectedRoom] = useState('');
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [roomsState, setRoomsState] = useState([]);
  const [timeSlots, setTimeSlots] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchSchedules = async () => {
      setLoading(true);
      try {
        let accessToken = sessionStorage.getItem("accessToken");
        if (!accessToken) {
          return;
        }

        const response = await fetch("/api/schedules", {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });

        if (!response.ok) throw new Error("스케줄 정보를 가져오는 데 실패했습니다.");

        const responseData = await response.json();
        if (responseData.code !== "S200") {
          throw new Error(responseData.message || "알 수 없는 오류");
        }

        const uniqueRooms = [];
        const roomMap = new Map();
        const uniqueTimeSlots = new Set();

        responseData.data.forEach(item => {
          if (!roomMap.has(item.roomNumber)) {
            roomMap.set(item.roomNumber, {
              id: item.roomNumber,
              capacity: item.capacity,
              features: item.facilities || [],
              status: 'available'
            });
          }

          const timeRange = `${item.startTime.substring(0, 5)}~${item.endTime.substring(0, 5)}`;
          uniqueTimeSlots.add(timeRange);
        });

        uniqueRooms.push(...roomMap.values());
        setRoomsState(uniqueRooms);
        setTimeSlots([...uniqueTimeSlots].sort());
      } finally {
        setLoading(false);
      }
    };

    fetchSchedules();
  }, []);
  
  const mergeTimeSlots = (times) => {
    if (times.length === 0) return [];

    const sortedTimes = times
      .map(time => time.split("~").map(t => t.trim()))
      .sort((a, b) => a[0].localeCompare(b[0]));

    const merged = [];
    let start = sortedTimes[0][0];
    let end = sortedTimes[0][1];

    for (let i = 1; i < sortedTimes.length; i++) {
      const [currentStart, currentEnd] = sortedTimes[i];
      
      if (currentStart === end) {
        end = currentEnd;
      } else {
        merged.push(`${start}~${end}`);
        start = currentStart;
        end = currentEnd;
      }
    }
    merged.push(`${start}~${end}`);
    return merged;
  };

  const penaltyData = useMemo(() => [
    {
      name: '김원빈',
      studentId: '201900969',
      reason: '취소 패널티 2회',
      issueDate: '2024-01-20',
      expiryDate: '2024-01-30',
      status: 'active'
    },
    {
      name: '양재원',
      studentId: '201900123',
      reason: '예약 불참 2회',
      issueDate: '2024-01-15',
      expiryDate: '2024-01-25',
      status: 'inactive'
    },
    {
      name: '정순인',
      studentId: '201800234',
      reason: '취소 패널티 2회',
      issueDate: '2024-01-20',
      expiryDate: '2024-01-30',
      status: 'active'
    },
    {
      name: '도성현',
      studentId: '201900345',
      reason: '취소 패널티 2회',
      issueDate: '2024-01-20',
      expiryDate: '2024-01-30',
      status: 'active'
    }
  ], []);

  // Handlers
  const handleTabChange = useCallback((tab) => {
    setActiveTab(tab);
  }, []);

  const handleRoomSelect = useCallback((roomId) => {
    setRoomsState(prev => prev.map(room => ({
      ...room,
      status: room.id === roomId ? 'selected' : 'available'
    })));
  
    if (selectedRoom === roomId) {
      // 방 선택 해제 시, 선택된 시간도 초기화
      setSelectedRoom('');
      setSelectedTimes([]);
    } else {
      setSelectedRoom(roomId);
      // 🔥 기존 선택된 시간을 유지! (여기서 초기화 안 함)
    }
  }, [selectedRoom]);
  
  const handleTimeSelect = useCallback((time) => {
    setSelectedTimes(prev => {
      if (prev.includes(time)) {
        return prev.filter(t => t !== time);  // 선택 해제
      } else {
        return [...prev, time];  // 선택 추가
      }
    });
  }, []);

  // Computed Values
  const availableRoomsCount = useMemo(() => {
    const total = roomsState.length;
    const available = roomsState.filter(room => room.status === 'available').length;
    return { total, available };
  }, [roomsState]);

  // Format selected times to merge consecutive slots
  const formattedSelectedTimes = useMemo(() => {
    return mergeTimeSlots(selectedTimes);  // 예약 정보에는 병합된 시간만 표시
  }, [selectedTimes]);  

  return {
    // State
    activeTab,
    selectedRoom,
    selectedTimes,
    formattedSelectedTimes,
    
    // Data
    rooms: roomsState,
    timeSlots,
    penaltyData,
    availableRoomsCount,
    
    // Handlers
    handleTabChange,
    handleRoomSelect,
    handleTimeSelect
  };
};

export default useAdminPageHandler;