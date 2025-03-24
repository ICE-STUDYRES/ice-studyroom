import { useState, useMemo, useCallback, useEffect } from 'react';
import { useTokenHandler } from "../handlers/TokenHandler";

const getTodayDayOfWeek = () => {
  const today = new Date();
  return new Intl.DateTimeFormat('ko-KR', { weekday: 'short' }).format(today);
};

const useAdminPageHandler = () => {

  const getInitialTab = () => {
    return sessionStorage.getItem("adminActiveTab") || "booking";
  };

  const [activeTab, setActiveTab] = useState(getInitialTab);
  const [selectedRoom, setSelectedRoom] = useState('');
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [roomsState, setRoomsState] = useState([]);
  const [timeSlots, setTimeSlots] = useState([]);
  const [roomTimeSlots, setRoomTimeSlots] = useState([]);
  const [dayOfWeek, setDayOfWeek] = useState(getTodayDayOfWeek());
  const [disabledTimeSlots, setDisabledTimeSlots] = useState([]);
  const { refreshTokens } = useTokenHandler();

  const dayMapping = {
    '월': 'MONDAY',
    '화': 'TUESDAY',
    '수': 'WEDNESDAY',
    '목': 'THURSDAY',
    '금': 'FRIDAY',
    '토': 'SATURDAY',
    '일': 'SUNDAY',
  };

  const englishDay = dayMapping[dayOfWeek] || "MONDAY";

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
        
        const response = await fetch(`/api/admin/room-time-slots?dayOfWeek=${englishDay}`, {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });

        if (response.status === 401) {
          const newAccessToken = await refreshTokens();
          if (newAccessToken) {
            return fetchSchedules();
          } else {
            throw new Error("토큰 갱신 실패");
          }
        }
  
        if (!response.ok) throw new Error("스케줄 정보를 가져오는 데 실패했습니다.");
  
        const responseData = await response.json();
        if (responseData.code !== "S200") {
          throw new Error(responseData.message || "알 수 없는 오류");
        }
  
        const roomMap = new Map();
        const uniqueTimeSlots = new Set();
  
        const roomTimeSlotIds = responseData.data
        .filter(item => {
          const timeRange = `${item.startTime.substring(0, 5)}~${item.endTime.substring(0, 5)}`;
          const isMatch =
            item.roomNumber === selectedRoom &&
            selectedTimes.includes(timeRange);      
          return isMatch;
        })
        .map(item => item.id);
      
        const disabledSlots = [];
  
        responseData.data.forEach(item => {
          if (!roomMap.has(item.roomNumber)) {
            roomMap.set(item.roomNumber, {
              id: item.roomNumber,
              status: 'available'
            });
          }  
          const timeRange = `${item.startTime.substring(0, 5)}~${item.endTime.substring(0, 5)}`;
          uniqueTimeSlots.add(timeRange);
          if (item.roomNumber === selectedRoom && item.status === 'UNAVAILABLE') {
            disabledSlots.push(timeRange);
          }
        });
  
        setRoomsState([...roomMap.values()]);
        setTimeSlots([...uniqueTimeSlots].sort());
        setRoomTimeSlots(roomTimeSlotIds);
        setDisabledTimeSlots(disabledSlots);
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

  const handleTabChange = useCallback((tab) => {
    setActiveTab(tab);
    sessionStorage.setItem("adminActiveTab", tab);
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

const handleOccupy = async () => {
  try {
    let accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      return;
    }

    const selectedIds = getSelectedRoomTimeSlotIds();
    if (selectedIds.length === 0) {
      console.warn("⚠️ 선택된 시간대가 없습니다.");
      return;
    }

    const requestBody = {
      roomTimeSlotId: selectedIds,
      dayOfWeek: englishDay,
    };

    const response = await fetch("/api/admin/room-time-slots/occupy", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`
      },
      body: JSON.stringify(requestBody)
    });

    if (response.status === 401) {
      const newAccessToken = await refreshTokens();
      if (newAccessToken) {
        return handleOccupy();
      } else {
        throw new Error("토큰 갱신 실패");
      }
    }

    const responseData = await response.json();
    if (!response.ok) {
      throw new Error(responseData.message || "예약 처리 중 오류 발생");
    }

    alert("예약이 완료되었습니다!");
    window.location.reload();
  } catch (error) {
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
    disabledTimeSlots,
    
    // Data
    dayOfWeek,
    setDayOfWeek,
    rooms: roomsState,
    timeSlots,
    availableRoomsCount,
    
    // Handlers
    handleTabChange,
    handleRoomSelect,
    handleTimeSelect,
    handleOccupy,
  };
};

export default useAdminPageHandler;
