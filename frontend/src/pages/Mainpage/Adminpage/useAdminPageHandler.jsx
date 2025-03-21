import { useState, useMemo, useCallback, useEffect } from 'react';

const getTodayDayOfWeek = () => {
  const today = new Date();
  return new Intl.DateTimeFormat('ko-KR', { weekday: 'short' }).format(today);
};

const useAdminPageHandler = () => {
  const [activeTab, setActiveTab] = useState('booking');
  const [selectedRoom, setSelectedRoom] = useState('');
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [roomsState, setRoomsState] = useState([]);
  const [timeSlots, setTimeSlots] = useState([]);
  const [roomTimeSlots, setRoomTimeSlots] = useState([]);
  const [dayOfWeek, setDayOfWeek] = useState(getTodayDayOfWeek());

  const dayMapping = {
    '월': 'MONDAY',
    '화': 'TUESDAY',
    '수': 'WEDNESDAY',
    '목': 'THURSDAY',
    '금': 'FRIDAY'
  };

  useEffect(() => {
    setDayOfWeek(getTodayDayOfWeek());
  }, []); 

  useEffect(() => {
    setSelectedRoom('');
    setSelectedTimes([]);
  }, [dayOfWeek]);

  useEffect(() => {
    const fetchSchedules = async () => {
      try {
        let accessToken = sessionStorage.getItem("accessToken");
        if (!accessToken) {
          return;
        }
  
        const englishDay = dayMapping[dayOfWeek] || "Monday";
        const response = await fetch(`/api/admin/room-time-slots?dayOfWeek=${englishDay}`, {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });
  
        if (!response.ok) throw new Error("스케줄 정보를 가져오는 데 실패했습니다.");
  
        const responseData = await response.json();
        if (responseData.code !== "S200") {
          throw new Error(responseData.message || "알 수 없는 오류");
        }
  
        const roomMap = new Map();
        const uniqueTimeSlots = new Set();
  
        const roomTimeSlotIds = responseData.data
          .filter(item => 
            item.roomNumber === selectedRoom &&
            item.dayOfWeekStatus === englishDay &&
            selectedTimes.includes(`${item.startTime.substring(0, 5)}~${item.endTime.substring(0, 5)}`)
          )
          .map(item => item.roomTimeSlotId);
  
        responseData.data.forEach(item => {
          if (!roomMap.has(item.roomNumber)) {
            roomMap.set(item.roomNumber, {
              id: item.roomNumber,
              capacity: item.capacity || 0,
              features: item.facilities || [],
              status: 'available'
            });
          }
  
          const timeRange = `${item.startTime.substring(0, 5)}~${item.endTime.substring(0, 5)}`;
          uniqueTimeSlots.add(timeRange);
        });
  
        setRoomsState([...roomMap.values()]);
        setTimeSlots([...uniqueTimeSlots].sort());
        setRoomTimeSlots(roomTimeSlotIds);
      } finally {
      }
    };
  
    fetchSchedules();
  }, [dayOfWeek, selectedTimes]);
  
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

  const handleTabChange = useCallback((tab) => {
    setActiveTab(tab);
  }, []);

  const handleRoomSelect = useCallback((roomId) => {
    setRoomsState(prev => prev.map(room => ({
      ...room,
      status: room.id === roomId ? 'selected' : 'available'
    })));
  
    if (selectedRoom !== roomId) {
      setSelectedRoom(roomId);
      setSelectedTimes([]); 
    } else {
      setSelectedRoom('');
      setSelectedTimes([]); 
    }
  }, [selectedRoom]);
  
  
  const handleTimeSelect = useCallback((time) => {
    setSelectedTimes(prev => {
      if (prev.includes(time)) {
        return prev.filter(t => t !== time);
      } else {
        return [...prev, time];
      }
    });
  }, []);

  const availableRoomsCount = useMemo(() => {
    const total = roomsState.length;
    const available = roomsState.filter(room => room.status === 'available').length;
    return { total, available };
  }, [roomsState]);

  const formattedSelectedTimes = useMemo(() => {
    return mergeTimeSlots(selectedTimes);
  }, [selectedTimes]);  

  const getSelectedRoomTimeSlotIds = () => {
    return roomTimeSlots.filter(slot => slot !== undefined && slot !== null);
};

const handleReserve = async () => {
  try {
    let accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      console.error("⚠️ 인증 토큰이 없습니다.");
      return;
    }

    const selectedIds = getSelectedRoomTimeSlotIds();
    if (selectedIds.length === 0) {
      console.warn("⚠️ 선택된 시간대가 없습니다.");
      return;
    }

    const requestBody = {
      roomTimeSlotId: selectedIds,
      setOccupy: true
    };

    const response = await fetch("/api/admin/room-time-slots/occupy", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`
      },
      body: JSON.stringify(requestBody)
    });

    const responseData = await response.json();
    if (!response.ok) {
      throw new Error(responseData.message || "예약 처리 중 오류 발생");
    }

    console.log("✅ 예약 성공:", responseData.data.message);
    alert("예약이 완료되었습니다!");
  } catch (error) {
    console.error("❌ 예약 실패:", error.message);
    alert("예약에 실패했습니다. 다시 시도해주세요.");
  }
};

  return {
    // State
    activeTab,
    selectedRoom,
    selectedTimes,
    setSelectedTimes,
    getSelectedRoomTimeSlotIds,
    formattedSelectedTimes,
    
    // Data
    dayOfWeek,
    setDayOfWeek,
    rooms: roomsState,
    timeSlots,
    penaltyData,
    availableRoomsCount,
    
    // Handlers
    handleTabChange,
    handleRoomSelect,
    handleTimeSelect,
    handleReserve,
  };
};

export default useAdminPageHandler;
