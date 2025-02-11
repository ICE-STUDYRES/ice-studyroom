import { useState, useEffect } from "react";
import { useMainpageHandlers } from '../Mainpage/MainpageHandlers';
import { useNavigate } from "react-router-dom";

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
   const navigate = useNavigate();

  const rooms = [
    {id:"305-1", name: "305-1", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    {id:"305-2", name: "305-2", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    {id:"305-3", name: "305-3", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    {id:"305-4", name: "305-4", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    {id:"305-5", name: "305-5", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    {id:"305-6", name: "305-6", capacity: 4, facilities: ["PC", "모니터"], location: "3층" },
    {id:"409-1", name: "409-1", capacity: 6, facilities: ["화이트보드", "PC", "대형 모니터"], location: "4층" },
    {id:"409-2", name: "409-2", capacity: 6, facilities: ["화이트보드", "PC", "대형 모니터"], location: "4층" },
];

  const {
    refreshTokens
  } = useMainpageHandlers();

  // 사용자 정보 가져오기
  const fetchUserInfo = async (retry = true) => {
    try {
      let accessToken = localStorage.getItem("accessToken");
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

      if (response.status === 401 && retry) {
        console.warn("Access token expired. Refreshing tokens...");
        console.log("Current access token:", accessToken);
        accessToken = await refreshTokens();
        
        if (accessToken) {
          console.log("New access token after refresh:", accessToken);
          console.log("Retrying fetchUserInfo with new access token...");
          return fetchUserInfo(false); // 한 번만 재시도
        } else {
          console.error("Token refresh failed. Logging out.");
          return;
        }
      }

      if (!response.ok) throw new Error("사용자 정보를 가져오는 데 실패했습니다.");

      const responseData = await response.json();

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

  const handleReservation = async () => {
    if (!selectedRoom || selectedTimes.length === 0 || !userInfo.mainUser.email) {
      alert("모든 필수 정보를 입력해주세요.");
      return;
    }
  
    const roomId = selectedRoom;
    
    // ✅ 올바르게 scheduleId 가져오기
    const scheduleIds = selectedTimes
      .map((time) => bookedSlots[roomId]?.slots?.[time]?.scheduleId)
      .filter(Boolean); // undefined 제거
  
    if (scheduleIds.length === 0) {
      alert("선택한 시간에 예약 가능한 슬롯이 없습니다.");
      return;
    }
  
    console.log("🔹 예약할 scheduleIds:", scheduleIds);
  
    // 참여자 이메일 리스트 (중복 제거)
    const participantEmails = [...new Set(userInfo.participants.map((p) => p.email.trim()))];
  
    // 예약 요청 데이터 생성
    const requestData = {
      scheduleId: scheduleIds,
      participantEmail: participantEmails,
      roomNumber: selectedRoom,
      startTime: selectedTimes[0].split("~")[0],
      endTime: selectedTimes[selectedTimes.length - 1].split("~")[1],
    };
  
    console.log("📝 예약 요청 데이터:", requestData);
  
    const roomData = bookedSlots[roomId]; 
    const roomType = roomData?.roomType;
    const apiEndpoint = roomType === "INDIVIDUAL" ? "/reservations/individual" : "/reservations/group";
  
    const accessToken = localStorage.getItem("accessToken");
    if (!accessToken) {
      alert("로그인이 필요합니다.");
      return;
    }
  
    try {
      const response = await fetch("/api" + apiEndpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(requestData),
      });
  
      if (!response.ok) throw new Error("예약 요청에 실패했습니다.");
  
      const responseData = await response.json();
  
      if (responseData.code !== "S200") {
        throw new Error(responseData.message || "예약에 실패했습니다.");
      }
  
      alert("예약이 성공적으로 완료되었습니다!");
      navigate('/');
      await fetchSchedules(); // 예약 완료 후 스케줄 새로고침
    } catch (error) {
      console.error("예약 요청 오류:", error);
      alert("예약에 실패했습니다. 다시 시도해주세요.");
    }
  };
  
  

  const fetchSchedules = async (retry = true) => {
    setLoading(true);
    try {
      let accessToken = localStorage.getItem("accessToken");
      if (!accessToken) {
        alert("로그인이 필요합니다.");
        return;
      }
  
      const response = await fetch("/api/schedules", {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
  
      if (response.status === 401 && retry) {
        console.warn("Access token expired. Refreshing tokens...");
        accessToken = await refreshTokens();
        if (accessToken) return fetchSchedules(false);
        return;
      }
  
      if (!response.ok) throw new Error("스케줄 정보를 가져오는 데 실패했습니다.");
  
      const responseData = await response.json();
  
      console.log("📌 스케줄 API 응답 데이터:", responseData);
  
      if (responseData.code !== "S200") {
        throw new Error(responseData.message || "알 수 없는 오류");
      }

      const mappedData = responseData.data.reduce((acc, item) => {
        const { roomNumber, roomType, startTime, endTime, id: scheduleId, available, currentRes } = item; // ✅ currentRes 확인
        const matchedRoom = rooms.find((room) => room.name === roomNumber);
  
        if (matchedRoom) {
          const timeRange = `${startTime.substring(0, 5)}~${endTime.substring(0, 5)}`;
  
          if (!acc.bookedSlots[roomNumber]) {
            acc.bookedSlots[roomNumber] = { 
              roomType, 
              slots: {}, 
              capacity: matchedRoom.capacity, 
            };
          }
  
          // ✅ 필드명 변경: currentRes 사용
          acc.bookedSlots[roomNumber].slots[timeRange] = {
            available, 
            scheduleId,
            current_res: currentRes ?? 0 // ✅ currentRes 사용, 없으면 기본값 0
          };
  
          console.log(`🔹 ${roomNumber} - ${timeRange} 매핑된 current_res:`, acc.bookedSlots[roomNumber].slots[timeRange].current_res);
  
          if (!acc.timeSlots.includes(timeRange)) acc.timeSlots.push(timeRange);
        }
        return acc;
      }, { bookedSlots: {}, timeSlots: [] });
  
      console.log("📌 최신화된 예약 데이터:", mappedData);
  
      setBookedSlots(mappedData.bookedSlots);
      setTimeSlots((prev) => [...new Set([...prev, ...mappedData.timeSlots])].sort());
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