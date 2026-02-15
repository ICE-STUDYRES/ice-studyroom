{/* RestAPI로 데이터 받을 예정 */}

import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const NotificationPage = () => {
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  {/* 문구 생성 함수 */}
  const makeNotificationMessage = (notification,name) => {
    const { eventType, rank, previousRank, gapWithUpper, gapToEnter } = notification;

    switch (eventType) {
      case "RANK_TOP":
        return `[${name}님] 현재 1위입니다. 축하드립니다!`;
      case "RANK_IN_RANGE":
        return `[${name}님] 현재 ${rank}위입니다. ${rank - 1}위까지 점수 차는 ${gapWithUpper}점입니다.`;
      case "RANK_OUT_RANGE":
        return `[${name}님] 현재 ${rank}위입니다. 순위권 진입까지 ${gapToEnter}점이 필요합니다.`;
      case "TOP6_10_RANK_CHANGED":
        return `[${name}님] ${previousRank}위에서 ${rank}위로 진입했습니다!`;
      case "TOP5_RANK_CHANGED":
        return `[${name}님] ${previousRank}위 → ${rank}위로 순위가 상승했습니다!`;
      default:
        return "";
    }
  };

  {/* API 조회 */}
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        setLoading(true);
        
        {/* 더미 데이터 */}
        const dummyData = [
          { id: 1, eventType: "RANK_TOP", rank: 1, createdAt: "2026-02-15T09:00:00", read: false },
          { id: 2, eventType: "RANK_IN_RANGE", rank: 3, gapWithUpper: 15, createdAt: "2026-02-14T18:30:00", read: true },
          { id: 3, eventType: "RANK_OUT_RANGE", rank: 7, gapToEnter: 50, createdAt: "2026-02-10T12:00:00", read: true }
        ];
        
        setNotifications(dummyData);
      } catch (error) {
        console.error("알림 로드 실패:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchNotifications();
  }, []);

  {/* 삭제 핸들러 */}
  const handleDelete = async (id) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  {/* 전체 삭제 핸들러 */}
  const handleClearAll = async () => {
    setNotifications([]);
  };

  if (loading) return <div className="flex justify-center items-center h-screen">로딩 중...</div>;

  return (
    <div className="min-h-screen bg-white flex flex-col items-center p-4 pt-12">

      <div className="w-full max-w-[480px] flex flex-col flex-1">
        
        {notifications.length === 0 ? (
          //알림 없음 화면
          <div className="flex-1 flex flex-col items-center justify-center gap-6 pb-20">
            <h2 className="text-2xl font-bold text-gray-900">알림이 없습니다.</h2>
            <button
              onClick={() => navigate(-1)} 
              className="w-32 py-3 bg-blue-500 text-white rounded-xl font-bold hover:bg-blue-600 transition-colors shadow-md"
            >
              닫기
            </button>
          </div>
        ) : (
          //알림 목록 화면
          <>
            {/* 전체 삭제 버튼 */}
            <div className="mb-4">
              <button 
                onClick={handleClearAll}
                className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white text-sm font-bold rounded-xl transition-colors shadow-sm"
              >
                전체 알림 지우기
              </button>
            </div>

            {/* 리스트 영역 */}
            <div className="flex-1 space-y-4 overflow-y-auto mb-6">
              {notifications.map((noti) => (
                <div key={noti.id} className="bg-blue-100 p-5 rounded-2xl flex items-center justify-between shadow-sm relative min-h-[80px]">
                  <p className="text-gray-800 text-[15px] font-semibold leading-relaxed text-center w-full px-6 break-keep">
                    {makeNotificationMessage(noti)}
                  </p>
                  
                  {/* 삭제(X) 버튼 */}
                  <button 
                    onClick={() => handleDelete(noti.id)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-red-500 p-2"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>
              ))}
            </div>

            {/* 하단 닫기 버튼 */}
            <div className="pb-8 mt-auto flex justify-end">
              <button 
                onClick={() => navigate(-1)} 
                className="px-4 py-2 bg-blue-500 text-white rounded-xl font-bold hover:bg-blue-600 transition-colors shadow-sm text-sm"
              >
                닫기
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default NotificationPage;