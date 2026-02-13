{/* RestAPI로 데이터 받을 예정 */}

import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const NotificationPage = () => {
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  {/* API로부터 알림 데이터를 가져오는 함수 */}
  const fetchNotifications = async () => {
    try {
      setLoading(true);
      {/* 백엔드 서버와 연결하면 */}
      // const response = await fetch('https://~/notifications');
      // const data = await response.json();
      
      {/* 더미데이터 */}
      const dummyDataFromAPI = [
        { id: 1, name: "박다영", rank: 1, gap: 0, type: "RANK_1" },
        { id: 2, name: "박다영", rank: 3, gap: 15, type: "RANK_HIGH" }, // 2~5위
        { id: 3, name: "박다영", rank: 6, gap: 30, type: "RANK_OUT" },  // 6위 이하
      ];

      setNotifications(dummyDataFromAPI);
    } catch (error) {
      console.error("데이터를 불러오는데 실패했습니다.", error);
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    fetchNotifications();
  }, []);

  {/* 데이터에 따라 메시지를 생성하는 함수 */}
  const getMessage = (noti) => {
    if (noti.rank === 1) {
      return `[${noti.name}님] 현재 1위입니다. 축하드립니다!`;
    } else if (noti.rank >= 2 && noti.rank <= 5) {
      return `[${noti.name}님] 현재 ${noti.rank}위입니다. ${noti.rank - 1}위까지 점수차는 ${noti.gap}입니다.`;
    } else {
      return `[${noti.name}님] 현재 ${noti.rank}위입니다. 순위권 진입을 위해서 필요한 점수는 ${noti.gap}입니다.`;
    }
  };

  const handleGoBack = () => navigate(-1);

  const handleDelete = async (id) => {
  try {
    {/* 백엔드 서버에 DB에서 해당 ID 삭체 요청(DELETE 방식) */}
    await fetch(`https://~/notifications/${id}`, {
      method: 'DELETE',
    });

    {/* 서버에서 성공했다는 응답이 오면, 그때 화면에서도 삭제 */}
    setNotifications(prev => prev.filter(n => n.id !== id));
    
  } catch (error) {
    alert("삭제에 실패했습니다. 다시 시도해주세요.");
  }
  };

  const handleClearAll = async () => {
  try {
    {/* 백엔드 서버에 전체 삭제 요청 */}
    const response = await fetch(`https://~/notifications/clear-all`, {
      method: 'DELETE',
    });

    if (response.ok) {
      {/* 서버에서 성공했다는 응답이 오면, 그때 화면에서도 전체 삭제 */}
      setNotifications([]);
    }
  } catch (error) {
    console.error("전체 삭제 실패:", error);
    alert("알림을 모두 지우는 데 실패했습니다.");
  }
};

  if (loading) return <div className="flex h-screen items-center justify-center">로딩 중...</div>;

  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-white flex flex-col">
      <div className="h-8 bg-white" />
      {notifications.length === 0 ? (
        <div className="flex-1 flex flex-col items-center justify-center gap-6 p-6 bg-white">
          <h2 className="text-2xl font-bold text-black">알림이 없습니다.</h2>
          <button onClick={handleGoBack} className="w-32 py-3 bg-blue-500 text-white rounded-xl font-bold">닫기</button>
        </div>
      ) : (
        <>
          <div className="px-5 py-4 bg-white border-b border-gray-50">
            <button 
              onClick={handleClearAll}
              className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white text-sm font-bold rounded-lg transition-colors shadow-sm"
            >
              전체 알림 지우기
            </button>
          </div>

          <div className="flex-1 px-5 py-2 space-y-3 overflow-y-auto">
            {notifications.map((noti) => (
              <div key={noti.id} className="flex items-start justify-between p-4 bg-blue-50 rounded-xl shadow-sm">
                <span className="text-sm font-medium text-gray-800 leading-relaxed">
                  {getMessage(noti)}
                </span>
                <button onClick={() => handleDelete(noti.id)} className="text-gray-400 hover:text-red-500 ml-2">
                  <X className="w-5 h-5" />
                </button>
              </div>
            ))}
          </div>

          <div className="p-5 bg-white border-t border-gray-100 flex justify-end">
            <button onClick={handleGoBack} className="px-8 py-2.5 bg-blue-500 text-white rounded-xl font-bold">닫기</button>
          </div>
        </>
      )}
    </div>
  );
};

export default NotificationPage;