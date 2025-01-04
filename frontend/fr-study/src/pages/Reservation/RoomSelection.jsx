// // RoomSelection.jsx
// import React, { useState, useEffect } from 'react';
// import { useNavigate } from 'react-router-dom';
// import leftArrow from '../../assets/images/left.png';
// import logoutIcon from '../../assets/images/logout.png';
// import { useBooking } from './BookingContext';
// import './RoomSelection.css';

// const RoomSelection = () => {
//   const navigate = useNavigate();
//   const { bookingInfo, updateBookingInfo } = useBooking();
//   const [selectedRoom, setSelectedRoom] = useState(bookingInfo.roomNo);
//   const [currentTab, setCurrentTab] = useState('room');
  
//   const rooms = [
//     '305-1', '305-2', '305-3', '305-4', 
//     '305-5', '305-6', '409-1', '409-2'
//   ];

//   const tabs = [
//     { id: 'room', label: '스터디룸' },
//     { id: 'time', label: '시간 선택' },
//     { id: 'info', label: '정보 입력' }
//   ];

//   const handleRoomSelect = (room) => {
//     setSelectedRoom(room);
//     updateBookingInfo({ roomNo: room });
//   };

//   const handleTabClick = (tabId) => {
//     if (tabId === 'time') {
//       if (!selectedRoom) {
//         alert('스터디룸을 먼저 선택해주세요.');
//         return;
//       }
//       navigate('/reservation/time');
//     } else if (tabId === 'info') {
//       if (!selectedRoom || !bookingInfo.timeRange) {
//         alert('스터디룸과 시간을 먼저 선택해주세요.');
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

//   return (
//     <div className="page-container">
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

//       <div className="room-list">
//         {rooms.map((room) => (
//           <button
//             key={room}
//             onClick={() => handleRoomSelect(room)}
//             className={`room-item ${selectedRoom === room ? 'selected' : ''}`}
//           >
//             {room}
//           </button>
//         ))}
//       </div>
//     </div>
//   );
// };

// export default RoomSelection;
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBooking } from './BookingContext';
import { 
  ChevronLeft, 
  LogOut, 
  Users, 
  MonitorSmartphone,
  Clock,
  MapPin,
  Building,
  AlertCircle
} from 'lucide-react';
import './RoomSelection.css';

const RoomSelection = () => {
  const navigate = useNavigate();
  const { bookingInfo, updateBookingInfo } = useBooking();
  const [selectedRoom, setSelectedRoom] = useState(bookingInfo.roomNo);
  const [currentTab, setCurrentTab] = useState('room');
  
  const rooms = [
    { id: '305-1', capacity: 4, hasDisplay: true, floor: 3 },
    { id: '305-2', capacity: 6, hasDisplay: true, floor: 3 },
    { id: '305-3', capacity: 4, hasDisplay: false, floor: 3 },
    { id: '305-4', capacity: 8, hasDisplay: true, floor: 3 },
    { id: '305-5', capacity: 4, hasDisplay: true, floor: 3 },
    { id: '305-6', capacity: 6, hasDisplay: true, floor: 3 },
    { id: '409-1', capacity: 8, hasDisplay: true, floor: 4 },
    { id: '409-2', capacity: 4, hasDisplay: false, floor: 4 }
  ];

  const tabs = [
    { id: 'room', label: '스터디룸' },
    { id: 'time', label: '시간 선택' },
    { id: 'info', label: '정보 입력' }
  ];

  const handleRoomSelect = (roomId) => {
    setSelectedRoom(roomId);
    updateBookingInfo({ roomNo: roomId });
  };

  const handleTabClick = (tabId) => {
    if (tabId === 'time') {
      if (!selectedRoom) {
        alert('스터디룸을 먼저 선택해주세요.');
        return;
      }
      navigate('/reservation/time');
    } else if (tabId === 'info') {
      if (!selectedRoom || !bookingInfo.timeRange) {
        alert('스터디룸과 시간을 먼저 선택해주세요.');
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

      {/* Current Selection Banner */}
      <div className="selection-banner">
        <div className="date-time-section">
          <div className="selection-info">
            <div className="building-info">
              <Building className="icon-sm" />
              <span>정보통신공학과</span>
            </div>
            {bookingInfo.roomNo && (
              <>
                <span className="separator">|</span>
                <div className="room-info">
                  <MapPin className="icon-sm" />
                  <span className="selected-room">{bookingInfo.roomNo}</span>
                </div>
              </>
            )}
            {bookingInfo.timeRange && (
              <>
                <span className="separator">|</span>
                <div className="time-info">
                  <Clock className="icon-sm" />
                  <span>{bookingInfo.timeRange}</span>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Legend */}
      <div className="time-legend">
        <div className="legend-item">
          <div className="legend-dot available"></div>
          <span>선택 가능</span>
        </div>
        <div className="legend-dot selected"></div>
        <span>선택된 방</span>
      </div>

      {/* Room List */}
      <div className="room-list">
        {rooms.map((room) => (
          <div
            key={room.id}
            onClick={() => handleRoomSelect(room.id)}
            className={`room-item ${selectedRoom === room.id ? 'selected' : ''}`}
          >
            <div className="room-content">
              <div className="room-main-info">
                <div className="room-header">
                  <span className={`room-number ${selectedRoom === room.id ? 'selected' : ''}`}>
                    {room.id}
                  </span>
                  <span className="floor-info">{room.floor}층</span>
                </div>
                {selectedRoom === room.id && (
                  <div className="selected-badge">
                    선택됨
                  </div>
                )}
              </div>
              <div className="room-details">
                <div className="facility-info">
                  <div className="info-item">
                    <Users className="icon-sm" />
                    <span>{room.capacity}인실</span>
                  </div>
                  {room.hasDisplay && (
                    <div className="info-item">
                      <MonitorSmartphone className="icon-sm" />
                      <span>디스플레이</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Bottom Notice */}
      <div className="bottom-notice">
        <div className="notice-content">
          <AlertCircle className="icon-sm warning" />
          <p>
            스터디룸 선택 후 시간을 선택해주세요.
            <br />
            <span className="sub-text">최대 2시간까지 예약 가능</span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RoomSelection;