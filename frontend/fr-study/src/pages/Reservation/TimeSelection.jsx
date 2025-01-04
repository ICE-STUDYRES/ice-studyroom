// // TimeSelection.jsx
// import React, { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import leftArrow from '../../assets/images/left.png';
// import logoutIcon from '../../assets/images/logout.png';
// import { useBooking } from './BookingContext';
// import './TimeSelection.css';   

// export const TimeSelection = ({ onTimeSelect }) => {
//   const [selectedTimes, setSelectedTimes] = useState([]);
//   const [availableSlot, setAvailableSlot] = useState(null);

//   const timeSlots = [
//     '09:00~10:00', '10:00~11:00', '11:00~12:00', '12:00~13:00',
//     '13:00~14:00', '14:00~15:00', '15:00~16:00', '16:00~17:00',
//     '17:00~18:00', '18:00~19:00', '19:00~20:00', '20:00~21:00',
//     '21:00~22:00'
//   ];

//   const bookedTimes = [
//     '16:00~17:00', '18:00~19:00'
//   ];

//   const getAdjacentTimes = (time) => {
//     const index = timeSlots.indexOf(time);
//     const adjacentTimes = [];
    
//     if (index > 0 && !bookedTimes.includes(timeSlots[index - 1])) {
//       adjacentTimes.push(timeSlots[index - 1]);
//     }
//     if (index < timeSlots.length - 1 && !bookedTimes.includes(timeSlots[index + 1])) {
//       adjacentTimes.push(timeSlots[index + 1]);
//     }
    
//     return adjacentTimes;
//   };

//   const formatTimeRange = (times) => {
//     if (times.length === 0) return '';
//     if (times.length === 1) return times[0];
    
//     const sortedTimes = times.sort();
//     const startTime = sortedTimes[0].split('~')[0];
//     const endTime = sortedTimes[sortedTimes.length - 1].split('~')[1];
    
//     return `${startTime}~${endTime}`;
//   };

//   const handleTimeSelect = (time) => {
//     if (bookedTimes.includes(time)) return;

//     if (selectedTimes.length === 0) {
//       setSelectedTimes([time]);
//       setAvailableSlot(time);
//       onTimeSelect(formatTimeRange([time]));
//     } else if (selectedTimes.length === 1) {
//       if (getAdjacentTimes(availableSlot).includes(time)) {
//         setSelectedTimes([...selectedTimes, time]);
//         setAvailableSlot(null);
//         onTimeSelect(formatTimeRange([...selectedTimes, time]));
//       } else {
//         setSelectedTimes([time]);
//         setAvailableSlot(time);
//         onTimeSelect(formatTimeRange([time]));
//       }
//     } else if (selectedTimes.includes(time)) {
//       const newTimes = selectedTimes.filter(t => t !== time);
//       setSelectedTimes(newTimes);
//       setAvailableSlot(newTimes.length === 1 ? newTimes[0] : null);
//       onTimeSelect(formatTimeRange(newTimes));
//     }
//   };

//   const getTimeSlotStyle = (time) => {
//     if (bookedTimes.includes(time)) {
//       return 'time-slot-booked';
//     }
//     if (selectedTimes.includes(time)) {
//       return 'time-slot-selected';
//     }
//     if (availableSlot && getAdjacentTimes(availableSlot).includes(time)) {
//       return 'time-slot-adjacent';
//     }
//     return 'time-slot-default';
//   };

//   return (
//     <div className="container">
//       <div className="time-slot-container">
//         {timeSlots.map((time) => (
//           <button
//             key={time}
//             onClick={() => handleTimeSelect(time)}
//             disabled={bookedTimes.includes(time)}
//             className={`time-slot-button ${getTimeSlotStyle(time)}`}
//           >
//             {time}
//           </button>
//         ))}
//       </div>
//       <p className="helper-text">
//         한 시간대를 선택하면 앞 뒤 하나씩 만 선택 가능
//       </p>
//     </div>
//   );
// };

// export const TimeSelectionPreview = () => {
//   const navigate = useNavigate();
//   const { bookingInfo, updateBookingInfo } = useBooking();
//   const [currentTab, setCurrentTab] = useState('time');

//   const tabs = [
//     { id: 'room', label: '스터디룸' },
//     { id: 'time', label: '시간 선택' },
//     { id: 'info', label: '정보 입력' }
//   ];

//   const handleTimeSelect = (timeRange) => {
//     updateBookingInfo({ timeRange });
//   };

//   const handleTabClick = (tabId) => {
//     if (tabId === 'room') {
//       navigate('/reservation/room');
//     } else if (tabId === 'info') {
//       if (!bookingInfo.timeRange) {
//         alert('시간을 먼저 선택해주세요.');
//         return;
//       }
//       navigate('/reservation/info');
//     } else {
//       setCurrentTab(tabId);
//     }
//   };

//   const handleLogout = () => {
//     navigate('/login');
//   };

//   const handleBack = () => {
//     navigate('/');
//   };

//   React.useEffect(() => {
//     if (!bookingInfo.roomNo) {
//       alert('스터디룸을 먼저 선택해주세요.');
//       navigate('/RoomSelection');
//     }
//   }, [bookingInfo.roomNo, navigate]);

//   return (
//     <div className="preview-container">
//       <div className="header">
//         <div className="header-left" onClick={handleBack}>
//           <img src={leftArrow} alt="뒤로가기" className="back-arrow" />
//           <span className="header-title">정보통신공학과 스터디룸</span>
//         </div>
//         <div className="logout-container" onClick={handleLogout}>
//           <img src={logoutIcon} alt="로그아웃" className="logout-icon" />
//           <span className="logout-text">로그아웃</span>
//         </div>
//       </div>

//       <div className="divider" />

//       <div className="nav-tabs">
//         {tabs.map(tab => (
//           <span
//             key={tab.id}
//             onClick={() => handleTabClick(tab.id)}
//             className={`tab-text ${currentTab === tab.id ? 'active' : ''}`}
//           >
//             {tab.label}
//           </span>
//         ))}
//       </div>

//       <div className="selected-info">
//         {bookingInfo.roomNo && <span>{bookingInfo.roomNo}</span>}
//         {bookingInfo.timeRange && (
//           <>
//             <span className="separator">|</span>
//             <span>{bookingInfo.timeRange}</span>
//           </>
//         )}
//         {bookingInfo.representativeName && (
//           <>
//             <span className="separator">|</span>
//             <span>{bookingInfo.representativeName}</span>
//           </>
//         )}
//       </div>

//       <TimeSelection onTimeSelect={handleTimeSelect} />
//     </div>
//   );
// };

// export default TimeSelectionPreview;
// TimeSelection.jsx
// TimeSelection.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBooking } from './BookingContext';
import { 
  ChevronLeft, 
  Clock, 
  LogOut, 
  Calendar, 
  Users, 
  AlertCircle, 
  MonitorSmartphone,
  Wifi,
  MapPin,
  ChevronRight
} from 'lucide-react';
import './TimeSelection.css';

export const TimeSelection = ({ onTimeSelect }) => {
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [availableSlot, setAvailableSlot] = useState(null);

  const timeSlots = [
    '09:00~10:00', '10:00~11:00', '11:00~12:00', '12:00~13:00',
    '13:00~14:00', '14:00~15:00', '15:00~16:00', '16:00~17:00',
    '17:00~18:00', '18:00~19:00', '19:00~20:00', '20:00~21:00',
    '21:00~22:00'
  ];

  const bookedTimes = [
    '16:00~17:00', '18:00~19:00'
  ];

  const getAdjacentTimes = (time) => {
    const index = timeSlots.indexOf(time);
    const adjacentTimes = [];
    
    if (index > 0 && !bookedTimes.includes(timeSlots[index - 1])) {
      adjacentTimes.push(timeSlots[index - 1]);
    }
    if (index < timeSlots.length - 1 && !bookedTimes.includes(timeSlots[index + 1])) {
      adjacentTimes.push(timeSlots[index + 1]);
    }
    
    return adjacentTimes;
  };

  const formatTimeRange = (times) => {
    if (times.length === 0) return '';
    if (times.length === 1) return times[0];
    
    const sortedTimes = times.sort();
    const startTime = sortedTimes[0].split('~')[0];
    const endTime = sortedTimes[sortedTimes.length - 1].split('~')[1];
    
    return `${startTime}~${endTime}`;
  };

  const handleTimeSelect = (time) => {
    if (bookedTimes.includes(time)) return;

    if (selectedTimes.length === 0) {
      setSelectedTimes([time]);
      setAvailableSlot(time);
      onTimeSelect(formatTimeRange([time]));
    } else if (selectedTimes.length === 1) {
      if (getAdjacentTimes(availableSlot).includes(time)) {
        setSelectedTimes([...selectedTimes, time]);
        setAvailableSlot(null);
        onTimeSelect(formatTimeRange([...selectedTimes, time]));
      } else {
        setSelectedTimes([time]);
        setAvailableSlot(time);
        onTimeSelect(formatTimeRange([time]));
      }
    } else if (selectedTimes.includes(time)) {
      const newTimes = selectedTimes.filter(t => t !== time);
      setSelectedTimes(newTimes);
      setAvailableSlot(newTimes.length === 1 ? newTimes[0] : null);
      onTimeSelect(formatTimeRange(newTimes));
    }
  };

  const getTimeSlotStyle = (time) => {
    if (bookedTimes.includes(time)) return 'reserved';
    if (selectedTimes.includes(time)) return 'selected';
    if (availableSlot && getAdjacentTimes(availableSlot).includes(time)) return 'available';
    return '';
  };

  return (
    <div className="time-slots">
      {timeSlots.map((time) => (
        <div
          key={time}
          onClick={() => handleTimeSelect(time)}
          className={`time-slot ${getTimeSlotStyle(time)}`}
        >
          {getTimeSlotStyle(time) === 'reserved' && (
            <div className="user-count">4</div>
          )}
          <div className="time-slot-content">
            <div className="time-info">
              <span className="time">{time}</span>
              <span className="status-text">
                {getTimeSlotStyle(time) === 'reserved' 
                  ? '다른 사용자가 예약한 시간' 
                  : '예약 가능한 시간'
                }
              </span>
            </div>
            {getTimeSlotStyle(time) === 'reserved' && (
              <span className="status-badge reserved">예약됨</span>
            )}
            {getTimeSlotStyle(time) === 'selected' && (
              <span className="status-badge selected">선택됨</span>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

export const TimeSelectionP = () => {
  const navigate = useNavigate();
  const { bookingInfo, updateBookingInfo } = useBooking();
  const [currentTab, setCurrentTab] = useState('time');

  const tabs = [
    { id: 'room', label: '스터디룸' },
    { id: 'time', label: '시간 선택' },
    { id: 'info', label: '정보 입력' }
  ];

  const getRoomInfo = (roomNo) => {
    const roomsData = {
      '305-1': { capacity: 4, floor: 3, facilities: ['display'] },
      '305-2': { capacity: 6, floor: 3, facilities: ['display', 'wifi'] },
      '305-3': { capacity: 4, floor: 3, facilities: [] },
      '305-4': { capacity: 8, floor: 3, facilities: ['display', 'wifi'] },
      '305-5': { capacity: 4, floor: 3, facilities: ['display'] },
      '305-6': { capacity: 6, floor: 3, facilities: ['display', 'wifi'] },
      '409-1': { capacity: 8, floor: 4, facilities: ['display', 'wifi'] },
      '409-2': { capacity: 4, floor: 4, facilities: [] }
    };

    return roomsData[roomNo] || { capacity: 0, floor: 0, facilities: [] };
  };

  const selectedRoomInfo = getRoomInfo(bookingInfo.roomNo);

  const handleTimeSelect = (timeRange) => {
    updateBookingInfo({ timeRange });
  };

  const handleTabClick = (tabId) => {
    if (tabId === 'room') {
      navigate('/reservation/room');
    } else if (tabId === 'info') {
      if (!bookingInfo.timeRange) {
        alert('시간을 먼저 선택해주세요.');
        return;
      }
      navigate('/reservation/info');
    } else {
      setCurrentTab(tabId);
    }
  };

  const handleLogout = () => {
    navigate('/login');
  };

  const handleBack = () => {
    navigate('/');
  };

  useEffect(() => {
    if (!bookingInfo.roomNo) {
      alert('스터디룸을 먼저 선택해주세요.');
      navigate('/RoomSelection');
    }
  }, [bookingInfo.roomNo, navigate]);

  return (
    <div className="page-container">
      {/* Header */}
      <div className="header">
        <div className="header-left" onClick={handleBack}>
          <ChevronLeft className="icon" />
          <span className="header-title">정보통신공학과 스터디룸</span>
        </div>
        <div className="logout-container" onClick={handleLogout}>
          <LogOut className="icon-sm" />
          <span className="logout-text">로그아웃</span>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="nav-tabs">
        {tabs.map(tab => (
          <div
            key={tab.id}
            onClick={() => handleTabClick(tab.id)}
            className={`tab-item ${currentTab === tab.id ? 'active' : ''}`}
          >
            {tab.label}
            {currentTab === tab.id && <span className="active-indicator"></span>}
          </div>
        ))}
      </div>

      {/* Room Info Section */}
      <div className="selection-banner">
        <div className="date-time-section">
          <div className="selection-info">
            <div className="date-info">
              <Calendar className="icon-sm" />
              <span>2024.01.03 (수)</span>
            </div>
            {bookingInfo.timeRange && (
              <>
                <span className="separator">|</span>
                <div className="time-info">
                  <Clock className="icon-sm" />
                  <span className="selected-time">{bookingInfo.timeRange}</span>
                </div>
              </>
            )}
          </div>
          {bookingInfo.timeRange && (
            <div className="selection-badge">
              {bookingInfo.timeRange.includes('~') ? '2시간 선택됨' : '1시간 선택됨'}
            </div>
          )}
        </div>

        <div className="room-info-section">
          <div className="room-info-container">
            <div className="room-location">
              <MapPin className="icon-sm" />
              <span className="room-id">{bookingInfo.roomNo}</span>
              <span className="floor-info">{selectedRoomInfo.floor}층</span>
            </div>
            <div className="vertical-divider"></div>
            <div className="facilities-info">
              <div className="facility-item">
                <Users className="icon-sm" />
                <span>{selectedRoomInfo.capacity}인실</span>
              </div>
              {selectedRoomInfo.facilities.includes('display') && (
                <div className="facility-item">
                  <MonitorSmartphone className="icon-sm" />
                  <span>디스플레이</span>
                </div>
              )}
              {selectedRoomInfo.facilities.includes('wifi') && (
                <div className="facility-item">
                  <Wifi className="icon-sm" />
                  <span>와이파이</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Legend */}
      <div className="time-legend">
        <div className="legend-item">
          <div className="legend-dot available"></div>
          <span>선택 가능</span>
        </div>
        <div className="legend-item">
          <div className="legend-dot reserved"></div>
          <span>예약됨</span>
        </div>
        <div className="legend-item">
          <div className="legend-dot selected"></div>
          <span>선택한 시간</span>
        </div>
      </div>

      {/* Time Selection Component */}
      <TimeSelection onTimeSelect={handleTimeSelect} />

      {/* Bottom Notice */}
      <div className="bottom-notice">
        <div className="notice-content">
          <AlertCircle className="icon-sm warning" />
          <p>
            한 시간대 선택 시 앞뒤 시간만 추가 선택 가능합니다.
            <br />
            <span className="sub-text">최대 2시간까지 예약 가능</span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default TimeSelectionP;