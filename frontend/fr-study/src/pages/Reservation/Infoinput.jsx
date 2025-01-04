import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import leftArrow from '../../assets/images/back.png';
import logoutIcon from '../../assets/images/Log out.png';
import { useBooking } from './BookingContext';
import './InfoInput.css';

const InfoInput = ({ onSubmit }) => {
  const [memberCount, setMemberCount] = useState(2);
  const [members, setMembers] = useState([
    { name: '', studentId: '' },
    { name: '', studentId: '' }
  ]);
  const [isInitialMount, setIsInitialMount] = useState(true);

  const navigate = useNavigate();
  const { bookingInfo, updateBookingInfo } = useBooking();
  const [currentTab, setCurrentTab] = useState('info');

  const tabs = [
    { id: 'room', label: '스터디룸' },
    { id: 'time', label: '시간 선택' },
    { id: 'info', label: '정보 입력' }
  ];

  // 초기 마운트 시에만 시간 선택 체크
  useEffect(() => {
    if (isInitialMount) {
      if (!bookingInfo.timeRange) {
        alert('시간을 먼저 선택해주세요.');
        navigate('/reservation/time');
      }
      setIsInitialMount(false);
    }
  }, [isInitialMount, bookingInfo.timeRange, navigate]);

  // 멤버 정보 업데이트는 실제 변경이 있을 때만 수행
  useEffect(() => {
    if (members[0].name && !isInitialMount) {
      const displayName = memberCount > 1 ? 
        `${members[0].name} 외 ${memberCount - 1}명` : 
        members[0].name;
      
      const newBookingInfo = {
        ...bookingInfo,
        representativeName: displayName,
        members: members
      };

      // 이전 상태와 비교하여 실제 변경이 있을 때만 업데이트
      if (JSON.stringify(newBookingInfo) !== JSON.stringify(bookingInfo)) {
        updateBookingInfo(newBookingInfo);
      }
    }
  }, [members, memberCount]);

  const handleMemberCountChange = (count) => {
    const newCount = Math.max(1, Math.min(4, count));
    setMemberCount(newCount);
    
    if (newCount > members.length) {
      setMembers([...members, ...Array(newCount - members.length).fill({ name: '', studentId: '' })]);
    } else {
      setMembers(members.slice(0, newCount));
    }
  };

  const handleMemberChange = (index, field, value) => {
    const newMembers = [...members];
    newMembers[index] = { ...newMembers[index], [field]: value };
    setMembers(newMembers);
  };

  const handleTabClick = (tabId) => {
    if (tabId === currentTab) return; // 현재 탭을 다시 클릭하는 경우 무시

    if (tabId === 'room') {
      navigate('/reservation/room');
    } else if (tabId === 'time') {
      navigate('/reservation/time');
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
    <div className="preview-container">
      <div className="header">
        <div className="header-left" onClick={handleBack}>
          <img src={leftArrow} alt="뒤로가기" className="back-arrow" />
          <span className="header-title">정보통신공학과 스터디룸</span>
        </div>
        <div className="logout-container" onClick={handleLogout}>
          <img src={logoutIcon} alt="로그아웃" className="logout-icon" />
          <span className="logout-text">로그아웃</span>
        </div>
      </div>

      <div className="divider" />

      <div className="nav-tabs">
        {tabs.map(tab => (
          <span
            key={tab.id}
            onClick={() => handleTabClick(tab.id)}
            className={`tab-text ${currentTab === tab.id ? 'active' : ''}`}
          >
            {tab.label}
          </span>
        ))}
      </div>

      <div className="selected-info">
        {bookingInfo.roomNo && <span>{bookingInfo.roomNo}</span>}
        {bookingInfo.timeRange && (
          <>
            <span className="separator">|</span>
            <span>{bookingInfo.timeRange}</span>
          </>
        )}
        {bookingInfo.representativeName && (
          <>
            <span className="separator">|</span>
            <span>{bookingInfo.representativeName}</span>
          </>
        )}
      </div>

      <div className="info-input-container">
        <div className="info-input-content">
          <div className="info-input-member-count">
            <div className="info-input-label">인원 수:</div>
            <div className="info-input-controls">
              <button 
                className="info-input-button"
                onClick={() => handleMemberCountChange(memberCount - 1)}
                disabled={memberCount <= 1}
              >
                -
              </button>
              <span className="info-input-member-count-display">{memberCount}</span>
              <button 
                className="info-input-button"
                onClick={() => handleMemberCountChange(memberCount + 1)}
                disabled={memberCount >= 4}
              >
                +
              </button>
            </div>
          </div>

          {members.map((member, index) => (
            <div key={index} className="info-input-member-info">
              <input
                type="text"
                placeholder="이름"
                value={member.name}
                onChange={(e) => handleMemberChange(index, 'name', e.target.value)}
                className="info-input-field"
              />
              <input
                type="text"
                placeholder="학번"
                value={member.studentId}
                onChange={(e) => handleMemberChange(index, 'studentId', e.target.value)}
                className="info-input-field"
              />
            </div>
          ))}

          <p className="info-input-notice">
            사용인원 명단을 정확히 기입하여 불이익이 없길 바랍니다.
          </p>
        </div>
      </div>
    </div>
  );
};

export default InfoInput;