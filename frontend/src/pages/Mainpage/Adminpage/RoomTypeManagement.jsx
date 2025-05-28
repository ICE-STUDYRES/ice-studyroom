import { useState } from 'react';
import useAdminPageHandler from './useAdminPageHandler';

const RoomTypeManagement = () => {
  const { rooms, setRooms } = useAdminPageHandler();
  const [pendingRoomTypes, setPendingRoomTypes] = useState({});

  const selectRoomType = (roomId, newType) => {
    const originalType = rooms.find(room => room.id === roomId)?.type;
    const currentPending = pendingRoomTypes[roomId];

    if (currentPending === newType || (!currentPending && originalType === newType)) {
      const { [roomId]: _, ...rest } = pendingRoomTypes;
      setPendingRoomTypes(rest);
    } else {
      setPendingRoomTypes(prev => ({
        ...prev,
        [roomId]: newType
      }));
    }
  };

const applyChanges = async () => {
  try {
    const entries = Object.entries(pendingRoomTypes);
    const results = await Promise.all(
      entries.map(([roomId, newType]) =>
        changeRoomType(roomId, newType)
      )
    );

    const updated = rooms.map(room => {
      const newType = pendingRoomTypes[room.id];
      return newType ? { ...room, type: newType } : room;
    });

    setRooms(updated);
    setPendingRoomTypes({});

    alert("변경 사항이 적용되었습니다.");
    window.location.reload()
  } catch (err) {
    console.error(err);
    alert("변경 적용 중 오류가 발생했습니다.");
  }
};

const changeRoomType = async (roomNumber, newType) => {
  try {
    const url = `/api/admin/room-time-slots/room-number/${roomNumber}`;
    const bodyData = JSON.stringify({ roomType: newType.toUpperCase() });
    let accessToken = sessionStorage.getItem("accessToken");

    const res = await fetch(url, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
      body: bodyData,
    });

    if (!res.ok) throw new Error('방 타입 변경 실패');
    return await res.json();
  } catch (err) {
    console.error(err);
    alert('방 타입 변경 중 오류가 발생했습니다.');
  }
};

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-gray-900">스터디룸 유형 관리</h2>
        <button
          onClick={applyChanges}
          disabled={Object.keys(pendingRoomTypes).length === 0}
          className={`px-6 py-2 rounded-lg text-sm font-medium transition-colors
            ${Object.keys(pendingRoomTypes).length === 0
              ? 'bg-white border border-gray-300 text-gray-400 cursor-not-allowed'
              : 'bg-gray-900 text-white hover:bg-gray-800'}
          `}
        >
          변경 사항 적용
        </button>
      </div>
      {rooms.map((room) => (
        <div key={room.id} className="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm border border-gray-200">
          <div className="text-lg font-semibold text-gray-800">{room.id}</div>
          <div className="flex gap-2">
            {['group', 'individual'].map((type) => {
              const selectedType = pendingRoomTypes[room.id] ?? room.type;
              return (
                <button
                  key={type}
                  onClick={() => selectRoomType(room.id, type)}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    selectedType === type
                      ? 'bg-gray-900 text-white'
                      : 'bg-white border border-gray-300 hover:bg-gray-50'
                  }`}
                >
                  {type === 'group' ? '그룹' : '개인'}
                </button>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
};

export default RoomTypeManagement;
