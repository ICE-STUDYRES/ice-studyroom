{/* 월간/학기/연간: RestAPI로 데이터 받을 예정 */}
{/* 주간: WebSocket으로 데이터 받을 예정 */}

import React, { useState, useEffect } from 'react';
import axios from 'axios';

/* MainPage가 WebSocket으로 실시간 받아온 주간 랭킹, 로그인 여부 */
const RankingSection = ({ weeklyData, isLoggedIn }) => {

  const [activeTab, setActiveTab] = useState('WEEKLY');
  const [apiRankingData, setApiRankingData] = useState([]);
  const [loading, setLoading] = useState(false);

  /* 탭 설정 (label: 화면 표시용) */
  const tabs = [
    { key: 'WEEKLY', label: '주간' },
    { key: 'MONTHLY', label: '월간' },
    { key: 'SEMESTER', label: '학기' },
    { key: 'YEARLY', label: '연간' },
  ];

  /* 탭 변경 시 데이터 가져오기 (주간은 소켓이라 제외) */
  useEffect(() => {
    /* 주간 탭이면 API 호출 안 함 (부모가 준 weeklyData 사용) */
    if (activeTab === 'WEEKLY') return;

    /* 로그인 안 했으면 API 호출 안 함 */
    if (!isLoggedIn) return;

    const fetchOtherRankings = async () => {
      setLoading(true);

      try {
        const token = sessionStorage.getItem('accessToken');
        
        const response = await axios.get(`/api/rankings?period=${activeTab}`, {
          headers: {
            Authorization: `Bearer ${token}`
          },
        });

        if (response.data.code === "S200") {
          setApiRankingData(response.data.data);
        }
      } catch (error) {
        const errorCode = error.response?.data?.code;

        if (errorCode === "UNAUTHORIZED") {
          console.error("UNAUTHORIZED: 로그인 세션이 만료되었거나 유효하지 않습니다.");
        } else if (errorCode === "C400") {
          console.error("잘못된 요청: 허용되지 않는 파라미터입니다.");
        } else if (errorCode === "E500") {
          console.error("Internal Server Error.");
        } else {
          console.error(`${activeTab} 랭킹 데이터 로드 실패:`, error);
        }
      } finally {
        setLoading(false);
      }
    };
    
    fetchOtherRankings();
  }, [activeTab, isLoggedIn]);

  const handleTabClick = (tabKey) => {
    setActiveTab(tabKey);
  };

  /* 현재 보여줄 데이터 결정 */
  const displayData = activeTab === 'WEEKLY' ? weeklyData : apiRankingData;

  return (
    <div className="px-4 pb-16 mt-4">
      <h2 className="text-lg font-bold mb-3 text-gray-900">Ranking Top 5</h2>
      {/* 탭 버튼 영역 */}
      <div className="flex gap-2 mb-4 bg-gray-100 p-1 rounded-xl">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => handleTabClick(tab.key)}
            className={`
              flex-1 py-1.5 text-sm font-medium rounded-lg transition-all
              ${activeTab === tab.key 
                ? 'bg-[#EBF8FF] text-[#3182CE] shadow-sm'
                : 'text-gray-500 hover:text-gray-700'}
            `}
          >
            {tab.label}
          </button>
        ))}
      </div>
      
      <div className="bg-white rounded-2xl border border-gray-100 p-5 shadow-sm min-h-[180px] flex items-center justify-center">
        {/* 주간 탭이거나, 로그인이 되어있으면 노란 박스 */}
        {activeTab === 'WEEKLY' || isLoggedIn ? (
          <div className="w-full bg-yellow-200 border-4 border-yellow-400 rounded-xl py-6 flex flex-col items-center justify-center gap-2">
             {displayData && displayData.length > 0 ? (
              displayData.slice(0, 5).map((user, index) => (
                <div key={index} className="font-bold text-gray-800 text-base">
                  {user.rank}위: {user.name}
                </div>
              ))
            ) : (
              /* 로딩 중이라도 아직 데이터가 없으면 이 문구가 뜹니다 */
              <p className="text-gray-500 text-sm">랭킹 데이터가 없습니다.</p>
            )}

          </div>
        ) : (
          <div className="w-full py-12 rounded-xl bg-[#FFF5F5] border border-red-100 flex flex-col items-center justify-center">
            <p className="text-sm font-bold text-[#991B1B]">로그인 후 이용해주세요.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default RankingSection;

