import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useNotification } from "../../Notification/Notification";
import { useTokenHandler } from "../../Mainpage/handlers/TokenHandler";

export const roomBookingManager = () => {
  const [activeTab, setActiveTab] = useState("room");
  const [selectedTimes, setSelectedTimes] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState("");
  const [bookedSlots, setBookedSlots] = useState({});
  const [timeSlots, setTimeSlots] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { addNotification } = useNotification();
  const { refreshTokens } = useTokenHandler();
  const [userInfo, setUserInfo] = useState({
    mainUser: { name: "", email: "" },
    participants: [],
  });

  // 예약 가능 여부 확인
  const canSelectTime = (time) => {
    if (!selectedRoom) return false;

    const roomId = selectedRoom;
    const isBooked = bookedSlots[roomId]?.slots?.[time]?.available === false;
    if (isBooked) return false;

    // 현재 시간 가져오기
    const now = new Date();
    const currentTime = now.toTimeString().slice(0, 5); // HH:mm 형식

    // 선택하려는 타임슬롯의 시작 시간 가져오기
    const [startTime] = time.split("~");

    // 현재 시간보다 이전이면 선택 불가
    if (startTime < currentTime) return false;

    if (selectedTimes.length === 0) return true;

    const timeIndex = timeSlots.indexOf(time);
    const selectedTimeIndexes = selectedTimes.map((t) => timeSlots.indexOf(t));

    if (selectedTimes.includes(time)) return true;
    if (selectedTimes.length === 1) {
    return Math.abs(timeIndex - selectedTimeIndexes[0]) === 1;
  }

    return false;
  };


  // 시간 클릭 처리
  const handleTimeClick = (time) => {
    if (!canSelectTime(time)) return;
    if (selectedTimes.includes(time)) {
      setSelectedTimes(selectedTimes.filter((t) => t !== time));
    } else if (selectedTimes.length < 2) {
      setSelectedTimes([...selectedTimes, time].sort((a, b) => timeSlots.indexOf(a) - timeSlots.indexOf(b)));
    }
  };

  // 예약 처리
  const handleReservation = async (updatedUserInfo) => {
    if (!selectedRoom || selectedTimes.length === 0 || !userInfo.mainUser.email) {
      addNotification("reservation", "missingFields");
      return;
    }

    const roomId = selectedRoom;
    const scheduleIds = selectedTimes
      .map((time) => bookedSlots[roomId]?.slots?.[time]?.scheduleId)
      .filter(Boolean);

    if (scheduleIds.length === 0) {
      addNotification('reservation', 'scheduleId_error');
      return;
    }

    const participantEmails = [...new Set(userInfo.participants.map((p) => p.email.trim()))];

    const requestData = {
      scheduleId: scheduleIds,
      participantEmail: participantEmails,
      roomNumber: selectedRoom,
      startTime: selectedTimes[0].split("~")[0],
      endTime: selectedTimes[selectedTimes.length - 1].split("~")[1],
    };

    console.log(requestData);
    const roomData = bookedSlots[roomId]; 
    const roomType = roomData?.roomType;
    const apiEndpoint = roomType === "INDIVIDUAL" ? "/reservations/individual" : "/reservations/group";

    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      addNotification('member', 'error');
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

      const responseData = await response.json();

      if (!response.ok || responseData.code !== "S200") {
        throw new Error(responseData.message);
      }

      addNotification("reservation", "success");
      navigate("/");
      await fetchSchedules();
    } catch (error) {
      addNotification("reservation", "error", error.message);
    }
  };

  // Schedule 불러오기 및 상태 업데이트
  const fetchSchedules = async (retry = true) => {
    setLoading(true);
    try {
      let accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) {
        addNotification('member', 'error');
        return;
      }

      const response = await fetch("/api/schedules", {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (response.status === 401 && retry) {
        accessToken = await refreshTokens();
        if (accessToken) return fetchSchedules(false);
        return;
      }

      if (!response.ok) throw new Error("스케줄 정보를 가져오는 데 실패했습니다.");

      const responseData = await response.json();
      if (responseData.code !== "S200") {
        throw new Error(responseData.message || "알 수 없는 오류");
      }

      // 데이터를 가공하여 상태 업데이트
      const mappedData = responseData.data.reduce((acc, item) => {
        let { roomNumber, roomType, startTime, endTime, id: scheduleId, available, currentRes, capacity, facilities, location } = item;

        // ✅ facilities 및 location 자동 설정
        if (!facilities) {
          if (roomNumber.startsWith("305")) {
            facilities = ["PC", "화이트보드"];
          } else if (roomNumber.startsWith("409")) {
            facilities = ["화이트보드", "대형 모니터", "PC"];
          } else {
            facilities = [];
          }
        }

        if (!location) {
            if (roomNumber.startsWith("305")) {
                location = "3층";
            } else if (roomNumber.startsWith("409")) {
                location = "4층";
            } else {
                location = "알 수 없음";
            }
        }
        

        // `rooms` 목록 업데이트 (중복 방지)
        if (!acc.rooms.find((room) => room.name === roomNumber)) {
          acc.rooms.push({ id: roomNumber, name: roomNumber, capacity, facilities, location });
        }

        // 시간 범위 변환 (예: "18:00~19:00")
        const timeRange = `${startTime.substring(0, 5)}~${endTime.substring(0, 5)}`;

        // `bookedSlots`에 추가
        if (!acc.bookedSlots[roomNumber]) {
          acc.bookedSlots[roomNumber] = { 
            roomType, 
            slots: {}, 
            capacity
          };
        }

        acc.bookedSlots[roomNumber].slots[timeRange] = {
          available: Boolean(available),  // 서버에서 오는 값을 그대로 유지
          scheduleId,
          current_res: currentRes ?? 0
        };

        // `timeSlots`에 시간 추가 (중복 제거)
        if (!acc.timeSlots.includes(timeRange)) acc.timeSlots.push(timeRange);

        return acc;
      }, { bookedSlots: {}, timeSlots: [], rooms: [] });

      // 상태 업데이트
      setBookedSlots(mappedData.bookedSlots);
      setTimeSlots([...new Set(mappedData.timeSlots)].sort());
      setRooms(mappedData.rooms);

    } finally {
      setLoading(false);
    }
  };

  const fetchUserInfo = async (retry = true) => {
    try {
      let accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) {
        addNotification('member', 'error');
        return;
      }
  
      const response = await fetch("/api/users", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
  
      if (response.status === 401 && retry) {
        accessToken = await refreshTokens();
        
        if (accessToken) {
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

  useEffect(() => {
    fetchUserInfo();
    fetchSchedules();
  }, []);

  return {
    error,
    setUserInfo,
    fetchUserInfo,
    userInfo,
    activeTab,
    setActiveTab,
    selectedTimes,
    setSelectedTimes,
    selectedRoom,
    setSelectedRoom,
    bookedSlots,
    setBookedSlots,
    timeSlots,
    setTimeSlots,
    rooms,
    setRooms,
    handleReservation,
    canSelectTime,
    handleTimeClick,
    loading,
  };
};
