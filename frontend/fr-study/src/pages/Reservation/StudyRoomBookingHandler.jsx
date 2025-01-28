import { useState, useEffect } from "react";

export const useStudyRoomBooking = () => {
  const [activeTab, setActiveTab] = useState("room");
  const [selectedTimes, setSelectedTimes] = useState([]); // 선택된 시간 슬롯
  const [selectedRoom, setSelectedRoom] = useState(""); // 선택된 방 이름
  const [bookedSlots, setBookedSlots] = useState({}); // 예약 상태
  const [timeSlots, setTimeSlots] = useState([
    "09:00~10:00",
    "10:00~11:00",
    "11:00~12:00",
    "12:00~13:00",
    "13:00~14:00",
    "14:00~15:00",
    "15:00~16:00",
    "16:00~17:00",
    "17:00~18:00",
    "18:00~19:00",
    "19:00~20:00",
    "20:00~21:00",
    "21:00~22:00",
  ]); // 기본 타임 슬롯
  const [userInfo, setUserInfo] = useState({
    mainUser: { name: "", email: "" },
    participants: [],
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const rooms = [
    { name: "305-1", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    { name: "305-2", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    { name: "305-3", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    { name: "305-4", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    { name: "305-5", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    { name: "409-1", capacity: 6, facilities: ["화이트보드", "PC", "대형 모니터"], location: "4층" },
    { name: "409-2", capacity: 6, facilities: ["화이트보드", "PC", "대형 모니터"], location: "4층" },
];


  // 사용자 정보 가져오기
  const fetchUserInfo = async () => {
    try {
      const accessToken = localStorage.getItem("accessToken");
      if (!accessToken) {
        alert("로그인이 필요합니다.");
        return;
      }

      const response = await fetch("/api/users", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (!response.ok) throw new Error("사용자 정보를 가져오는 데 실패했습니다.");

      const responseData = await response.json();
      console.log("Users 데이터:", responseData); // 사용자 정보 로그 출력

      if (responseData.code !== "S200") {
        throw new Error(responseData.message || "알 수 없는 오류");
      }

      setUserInfo((prev) => ({
        ...prev,
        mainUser: {
          name: responseData.data.name,
          email: responseData.data.email,
        },
      }));
    } catch (err) {
      setError(err.message);
      console.error("사용자 정보 오류:", err.message);
    }
  };

  // 예약 가능 여부 확인
  const canSelectTime = (time) => {
    if (!selectedRoom) return false;

    const roomId = selectedRoom;
    const isBooked = bookedSlots[roomId]?.[time]?.available === false; // 예약 불가 여부
    if (isBooked) return false;

    if (selectedTimes.length === 0) return true; // 첫 시간은 무조건 선택 가능

    const timeIndex = timeSlots.indexOf(time); // 현재 선택한 시간 인덱스
    const selectedTimeIndexes = selectedTimes.map((t) => timeSlots.indexOf(t));

    if (selectedTimes.includes(time)) return true; // 이미 선택된 시간은 재선택 가능
    if (selectedTimes.length === 1) {
      // 연속된 시간대만 선택 가능
      return Math.abs(timeIndex - selectedTimeIndexes[0]) === 1;
    }

    return false; // 연속되지 않은 시간대 선택 불가
  };

  // 시간 클릭 처리
  const handleTimeClick = (time) => {
    if (!canSelectTime(time)) {
      console.log(`선택 불가: ${time}`);
      return;
    }

    if (selectedTimes.includes(time)) {
      // 선택된 시간 제거
      setSelectedTimes(selectedTimes.filter((t) => t !== time));
    } else if (selectedTimes.length < 2) {
      // 선택된 시간 추가 (최대 2개)
      setSelectedTimes([...selectedTimes, time].sort((a, b) => timeSlots.indexOf(a) - timeSlots.indexOf(b)));
    }
    console.log("선택된 시간:", selectedTimes);
  };

  // 예약 처리
  const handleReservation = async () => {
    if (!selectedRoom || selectedTimes.length === 0 || !userInfo.mainUser.email) {
      alert("모든 필수 정보를 입력해주세요.");
      return;
    }

    const roomId = selectedRoom; // 선택된 방 이름
    const scheduleIds = selectedTimes
      .map((time) => bookedSlots[roomId]?.[time]?.scheduleId)
      .filter(Boolean); // scheduleId 매핑

    if (scheduleIds.length === 0) {
      alert("선택한 시간에 예약 가능한 슬롯이 없습니다.");
      return;
    }

  // 참여자의 이메일만 병합
  const participantEmails = userInfo.participants.map((participant) => participant.email.trim());

  // 중복된 이메일 제거
  const uniqueParticipantEmails = [...new Set(participantEmails)];

    const requestData = {
      scheduleId: scheduleIds,
      participantEmail: uniqueParticipantEmails,
      roomNumber: selectedRoom,
      startTime: selectedTimes[0].split("~")[0],
      endTime: selectedTimes[selectedTimes.length - 1].split("~")[1],
    };

    const accessToken = localStorage.getItem("accessToken");
    if (!accessToken) {
      alert("로그인이 필요합니다.");
      return;
    }
    console.log("액세스 토큰:", accessToken); // 액세스 토큰 로그
    console.log("Request Data:", requestData); // 예약 요청 데이터 로그 출력

    try {
      const response = await fetch("/api/reservations", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(requestData),
      });

      if (!response.ok) throw new Error("예약 요청에 실패했습니다.");

      const responseData = await response.json();
      console.log("예약 응답 데이터:", responseData);

      if (responseData.code !== "S200") {
        throw new Error(responseData.message || "예약에 실패했습니다.");
      }

      alert("예약이 성공적으로 완료되었습니다!");
      fetchSchedules(); // 예약 완료 후 스케줄 새로고침
    } catch (error) {
      console.error("예약 요청 오류:", error);
      alert("예약에 실패했습니다. 다시 시도해주세요.");
    }
  };

  // 스케줄 데이터 가져오기
  const fetchSchedules = async () => {
    setLoading(true);
    try {
      const accessToken = localStorage.getItem("accessToken");
      if (!accessToken) {
        alert("로그인이 필요합니다.");
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

      console.log("API에서 받은 스케줄 데이터:", responseData.data);

      const mappedData = responseData.data.reduce(
        (acc, item) => {
          const { roomNumber, startTime, endTime, id: scheduleId, available } = item;
  
          // `rooms` 배열에서 해당 방 찾기
          const matchedRoom = rooms.find((room) => room.name === roomNumber);
  
          if (matchedRoom) {
            const roomName = matchedRoom.name;
            const timeRange = `${startTime.substring(0, 5)}~${endTime.substring(0, 5)}`;
  
            // 해당 방에 대한 예약 상태 초기화
            if (!acc.bookedSlots[roomName]) acc.bookedSlots[roomName] = {};
  
            // 예약 상태 저장
            acc.bookedSlots[roomName][timeRange] = { available, scheduleId };
  
            // 타임 슬롯 추가 (중복 방지)
            if (!acc.timeSlots.includes(timeRange)) acc.timeSlots.push(timeRange);
          }
  
          return acc;
        },
        { bookedSlots: {}, timeSlots: [] }
      );
  
      console.log("매핑된 예약 상태:", mappedData.bookedSlots);
      console.log("매핑된 타임 슬롯:", mappedData.timeSlots);
  
      // 상태 업데이트
      setBookedSlots(mappedData.bookedSlots);
      setTimeSlots((prev) =>
        [...new Set([...prev, ...mappedData.timeSlots])].sort()
      );
    } catch (err) {
      setError(err.message);
      console.error("스케줄 데이터 오류:", err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUserInfo(); // 사용자 정보 가져오기
    fetchSchedules(); // 스케줄 데이터 가져오기
  }, []);

  return {
    activeTab,
    setActiveTab,
    selectedTimes,
    setSelectedTimes,
    selectedRoom,
    setSelectedRoom,
    bookedSlots,
    setBookedSlots,
    userInfo,
    setUserInfo,
    rooms,
    timeSlots,
    handleReservation,
    canSelectTime,
    handleTimeClick,
    loading,
    error,
  };
};
