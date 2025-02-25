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
  const handleReservation = async () => {
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

    const apiEndpoint = bookedSlots[roomId]?.roomType === "INDIVIDUAL" ? "/reservations/individual" : "/reservations/group";
    let accessToken = sessionStorage.getItem("accessToken");

    try {
      const response = await fetch("/api" + apiEndpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(requestData),
      });

      if (response.status === 401) {
        const newAccessToken = await refreshTokens();
        if (!newAccessToken) return;
        return handleReservation();
      }

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
  
  const fetchSchedules = async () => {
    setLoading(true);
    try {
      let accessToken = sessionStorage.getItem("accessToken");

      const response = await fetch("/api/schedules", {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (response.status === 401) {
        const newAccessToken = await refreshTokens();
        if (!newAccessToken) return;
        return fetchSchedules();
      }

      if (!response.ok) throw new Error("스케줄 정보를 가져오는 데 실패했습니다.");

      const responseData = await response.json();
      if (responseData.code !== "S200") {
        throw new Error(responseData.message || "알 수 없는 오류");
      }

      const mappedData = responseData.data.reduce((acc, item) => {
        let { roomNumber, roomType, startTime, endTime, id: scheduleId, available, currentRes, capacity, facilities, location } = item;

        if (!facilities) {
          facilities = roomNumber.startsWith("305") ? ["PC", "화이트보드"] : roomNumber.startsWith("409") ? ["화이트보드", "대형 모니터", "PC"] : [];
        }

        if (!location) {
          location = roomNumber.startsWith("305") ? "3층" : roomNumber.startsWith("409") ? "4층" : "알 수 없음";
        }

        if (!acc.rooms.find((room) => room.name === roomNumber)) {
          acc.rooms.push({ id: roomNumber, name: roomNumber, capacity, facilities, location });
        }

        const timeRange = `${startTime.substring(0, 5)}~${endTime.substring(0, 5)}`;

        if (!acc.bookedSlots[roomNumber]) {
          acc.bookedSlots[roomNumber] = { roomType, slots: {}, capacity };
        }

        acc.bookedSlots[roomNumber].slots[timeRange] = {
          available: Boolean(available),
          scheduleId,
          current_res: currentRes ?? 0
        };

        if (!acc.timeSlots.includes(timeRange)) acc.timeSlots.push(timeRange);

        return acc;
      }, { bookedSlots: {}, timeSlots: [], rooms: [] });

      setBookedSlots(mappedData.bookedSlots);
      setTimeSlots([...new Set(mappedData.timeSlots)].sort());
      setRooms(mappedData.rooms);
    } finally {
      setLoading(false);
    }
  };

  const fetchUserInfo = async () => {
    try {
        let accessToken = sessionStorage.getItem("accessToken");
        const response = await fetch("/api/users", {
            method: "GET",
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        });

        if (response.status === 401) {
          const newAccessToken = await refreshTokens();
          if (!newAccessToken) return;
          return fetchUserInfo();
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
        console.error("🚨 사용자 정보 오류:", err.message);
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
