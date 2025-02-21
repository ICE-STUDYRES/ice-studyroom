import { useState, useMemo, useCallback } from 'react';

const useAdminPageHandler = () => {
  // State
  const [activeTab, setActiveTab] = useState('booking');
  const [selectedRoom, setSelectedRoom] = useState('');
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [roomsState, setRoomsState] = useState([
    { id: '305-1', capacity: 4, features: ['PC', '모니터'], status: 'available' },
    { id: '305-2', capacity: 4, features: ['PC', '모니터'], status: 'available' },
    { id: '305-3', capacity: 4, features: ['PC', '모니터'], status: 'available' },
    { id: '305-4', capacity: 4, features: ['PC', '모니터'], status: 'available' },
    { id: '305-5', capacity: 4, features: ['PC', '모니터'], status: 'available' },
    { id: '305-6', capacity: 4, features: ['PC', '모니터'], status: 'available' },
    { id: '409-1', capacity: 8, features: ['화이트보드', 'PC', '대형 모니터'], status: 'available' },
    { id: '409-2', capacity: 8, features: ['화이트보드', 'PC', '대형 모니터'], status: 'available' }
  ]);

  const timeSlots = useMemo(() => [
    '09:00-10:00', '10:00-11:00', '11:00-12:00', '12:00-13:00',
    '13:00-14:00', '14:00-15:00', '15:00-16:00', '16:00-17:00',
    '17:00-18:00', '18:00-19:00', '19:00-20:00', '20:00-21:00','21:00-22:00'
  ], []);

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
    // 같은 방을 다시 클릭하면 선택 해제
    if (selectedRoom === roomId) {
      setSelectedRoom('');
      setSelectedTimes([]);
      setRoomsState(prev => prev.map(room => ({
        ...room,
        status: 'available'
      })));
      return;
    }

    // 다른 방을 선택하면 기존 선택 초기화 후 새로운 방 선택
    setSelectedRoom(roomId);
    setSelectedTimes([]);
    setRoomsState(prev => prev.map(room => ({
      ...room,
      status: room.id === roomId ? 'selected' : 'available'
    })));
  }, [selectedRoom]);

  const handleTimeSelect = useCallback((time) => {
    setSelectedTimes(prev => {
      const newTimes = prev.includes(time)
        ? prev.filter(t => t !== time)
        : [...prev, time].sort();
      return newTimes;
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
    if (selectedTimes.length === 0) return [];
    
    const sortedTimes = [...selectedTimes].sort((a, b) => {
      const timeA = parseInt(a.split(':')[0]);
      const timeB = parseInt(b.split(':')[0]);
      return timeA - timeB;
    });
    
    const result = [];
    let start = sortedTimes[0];
    let prev = start;

    for (let i = 1; i <= sortedTimes.length; i++) {
      const current = sortedTimes[i];
      const prevEnd = prev?.split('-')[1];
      const currentStart = current?.split('-')[0];
      
      if (current && prevEnd === currentStart) {
        prev = current;
      } else {
        const timeRange = start === prev ? start : `${start.split('-')[0]}-${prev.split('-')[1]}`;
        result.push(timeRange);
        start = current;
        prev = current;
      }
    }

    return result;
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