import React from "react";
import "./ReservationStatus.css";
import backIcon from "../../assets/images/back.png";

const rooms = [
  {
    id: "305-1",
    name: "305-1 스터디룸",
    details: "4인실 | PC, 모니터",
    reservations: [],
    availableTimes: [],
  },
  {
    id: "305-2",
    name: "305-2 스터디룸",
    details: "4인실 | PC, 모니터",
    reservations: [
      { time: "11:00-12:00", reserver: "양재원", participants: 2 },
      { time: "16:00-18:00", reserver: "김원빈", participants: 3 },
    ],
  },
  {
    id: "305-3",
    name: "305-3 스터디룸",
    details: "4인실 | PC, 모니터",
    reservations: [],
  },
  {
    id: "305-4",
    name: "305-4 스터디룸",
    details: "4인실 | PC, 모니터",
    reservations: [],
  },
  {
    id: "305-5",
    name: "305-5 스터디룸",
    details: "4인실 | PC, 모니터",
    reservations: [],
  },
  {
    id: "305-6",
    name: "305-6 스터디룸",
    details: "4인실 | PC, 모니터",
    reservations: [],
  },
  {
    id: "305-7",
    name: "305-7 스터디룸",
    details: "4인실 | PC, 모니터",
    reservations: [],
  },
  {
    id: "409-1",
    name: "409-1 스터디룸",
    details: "4인실 | PC, 대형 모니터",
    reservations: [],
  },
  {
    id: "409-2",
    name: "409-2 스터디룸",
    details: "4인실 | PC, 대형 모니터",
    reservations: [],
  },
];

const ReservationStatus = () => {
  const handleBackClick = () => {
    window.history.back();
  };

  return (
    <div className="reservation-status">
      <header>
        <button className="back-button" onClick={handleBackClick}>
          <img src={backIcon} alt="Back" />
        </button>
        <h1>예약 현황</h1>
      </header>
      <p>2024년 12월 31일 (화)</p>
      {rooms.map((room) => (
        <div key={room.id} className="room">
          <div className="room-header">
            <h2>{room.name}</h2>
            <p>{room.details}</p>
          </div>
          <div className="reservations">
            {room.reservations.length > 0 ? (
              <ul>
                {room.reservations.map((res, index) => (
                  <li key={index}>
                    <span>{res.time}</span>
                    {res.reserver && <span> 예약자: {res.reserver}</span>}
                    <span> 참여인원: {res.participants}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p>예약된 정보가 없습니다.</p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

export default ReservationStatus;
