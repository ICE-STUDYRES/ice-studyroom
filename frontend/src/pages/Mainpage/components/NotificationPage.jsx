{/* 알림페이지 */}

import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useUser } from "../handlers/UserContext";
import axios from 'axios';

const NotificationPage = () => {
  const navigate = useNavigate();

  /* 유저 정보 가져오기 (초기엔 null일 수 있으므로 안전하게 접근) */
  const userData = useUser();
  const name = userData?.name || "";

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  /* 문구 생성 함수 - 서버에서 받은 알림 정보(notification)와 사용자 이름(name)을 받음 */
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

  /* 1. API 조회: 알림 목록 가져오기 (GET) */
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        setLoading(true);
        
        const token = sessionStorage.getItem('accessToken'); 

        /* 비로그인 시 */
        if (!token) {
        alert("로그인이 필요합니다. 로그인 페이지로 이동합니다.");
        navigate('/auth/signin');
        return;
        }

        const response = await axios.get('/api/notifications', {
          headers: { Authorization: `Bearer ${token}`}
        });

        /* 명세서 기준 S200 성공 코드 확인 */
        if (response.data.code === "S200") {
          /* 서버에서 받은 데이터 상태에 저장 */
          setNotifications(response.data.data);
        }

      } catch (error) {
        if (error.response?.data?.code === "C401") {
          console.error("Unauthorized: 인증이 필요하거나 토큰이 만료되었습니다.");
        } else if (error.response?.data?.code === "E500") {
          console.error("Internal Server Error:", error);
        } else {
          console.error("알림 데이터 로드 실패:", error);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchNotifications();
  }, []);

  /* 2. API 조회: 단건 알림 읽음(삭제) 처리 (PATCH) */
  const handleDelete = async (noti) => {

    const targetId = noti.id;
    
    if (!targetId) {
      alert("알림 고유 ID를 찾을 수 없습니다.");
      return;
    }

    try {
      const token = sessionStorage.getItem('accessToken');
      
      const response = await axios.patch(`/api/notifications/${targetId}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.code === "S200") {
        /*성공 시 화면에서 삭제 */
        setNotifications(prev => prev.filter(n => n.id !== targetId));
      }

    } catch (error) {
      const errerCode = error.response?.data?.code;

      if (errerCode === "C404") {
        alert("Not Found.");
      } else if (errerCode === "C403") {
        alert("Forbidden.");
      } else if (errerCode === "E500") {
        alert("Internal Server Error.");
      } else {
        console.error("알림 단건 삭제 실패:", error);
      }
    }
  };

  /* 3. API 조회: 전체 알림 읽음(삭제) 처리 (PATCH) */
  const handleClearAll = async () => {
    try {
      const token = sessionStorage.getItem('accessToken');
      
      const response = await axios.patch('/api/notifications', {}, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.code === "S200") {
        /* 성공 시 화면에서 전체 삭제 */
        setNotifications([]);
      }

    } catch (error) {
      if (error.response?.data?.code === "C401") {
        alert("Unauthorized.");
        navigate('/auth/signin');
      } else if (error.response?.data?.code === "E500") {
        alert("Internal Server Error.");
      } else {
        console.error("전체 알림 삭제 실패:", error);
      }
    }
  };

  if (loading) return <div className="flex justify-center items-center h-screen">로딩 중...</div>;

  return (
    <div className="min-h-screen bg-white flex flex-col items-center p-4 pt-12">
      <div className="w-full max-w-[480px] flex flex-col flex-1">
        
        {notifications.length === 0 ? (
          /* 알림 없음 화면 */
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
              {notifications.map((noti, index) => {
                return (
                  <div key={noti.id} className="bg-blue-100 p-5 rounded-2xl flex items-center justify-between shadow-sm relative min-h-[80px]">
                    <p className="text-gray-800 text-[15px] font-semibold leading-relaxed text-center w-full px-6 break-keep">
                      {makeNotificationMessage(noti, name)}
                    </p>
                    
                    {/* 삭제(X) 버튼 */}
                    <button
                      onClick={() => handleDelete(noti)}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-red-500 p-2"
                    >
                      <X className="w-5 h-5" />
                    </button>
                  </div>
                );
              })}
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