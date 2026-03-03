{/* 월간/학기/연간: RestAPI로 데이터 받을 예정 */}
{/* 주간: WebSocket으로 데이터 받을 예정 */}

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import OwlIcon from '../../../assets/images/Owl.png';

/* MainPage가 WebSocket으로 실시간 받아온 주간 랭킹, 로그인 여부 */
const RankingSection = ({ weeklyData, isLoggedIn }) => {

  const [activeTab, setActiveTab] = useState('WEEKLY');
  const [tabCache, setTabCache] = useState({});
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

    /* 이미 캐시된 데이터가 있으면 재요청 안 함 */
    if (tabCache[activeTab]) return;

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
          setTabCache(prev => ({ ...prev, [activeTab]: response.data.data }));
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
  }, [activeTab, isLoggedIn, tabCache]);

  const handleTabClick = (tabKey) => {
    setActiveTab(tabKey);
  };

  /* 현재 보여줄 데이터 결정 */
  const displayData = activeTab === 'WEEKLY' ? weeklyData : (tabCache[activeTab] || []);
  
  return (
    <div className="px-4 pb-16 mt-4">
      <h2 className="text-lg font-bold mb-3 text-gray-900 text-center">Ranking Top 5</h2>

      {/* 회색 테두리 */}
      <div className="bg-white rounded-[32px] border border-gray-300 p-6 shadow-sm min-h-[400px]">

        {/* 탭 버튼 영역 */}
        <div className="flex w-full mb-8 gap-4">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              onClick={() => handleTabClick(tab.key)}
              className={`
                flex-1 pb-3 text-sm font-bold transition-all relative
                ${activeTab === tab.key 
                  ? 'text-[#3182CE]'
                  : 'text-gray-400'}
              `}
            >
              {tab.label}

              {/* 모든 탭 아래에 회색 선 표시 */}
              <div className="absolute bottom-0 left-0 w-full h-[2px] bg-gray-200 rounded-full" />

              {/* 활성화된 탭 하단에만 파란색 강조선 표시 */}
              {activeTab === tab.key && (
                <div className="absolute bottom-0 left-0 w-full h-[4px] bg-[#3182CE] rounded-full z-10" />
              )}
            </button>
          ))}
        </div>
        
        {/* 주간 탭이거나, 로그인이 되어있으면 */}
        {activeTab === 'WEEKLY' || isLoggedIn ? (

          <div className="w-full flex flex-col gap-3">
            {displayData && displayData.length > 0 ? (
              displayData.slice(0, 5).map((user, index) => (
                <div 
                  key={user.rank || index} 
                  className="relative flex items-center bg-[#EBF4FF] rounded-[24px] px-8 py-5 shadow-sm transition-all"
                > 
                  {/* 데이터의 rank가 1일 때만 왕관 */}
                  {user.rank === 1 && (
                    <span className="absolute -top-4 left-1/2 transform -translate-x-1/2 text-2xl drop-shadow-md select-none">
                      👑
                    </span>
                  )}

                  {/* 왼쪽 - 순위 */}
                  <div className="w-16 font-extrabold text-[#1A202C] text-sm">
                    {user.rank}위
                  </div>

                  {/* 중앙 - 이름 */}
                  <div className="flex-1 text-center font-bold text-[#2D3748] text-sm">
                    {user.name}
                  </div>

                  {/* 오른쪽 - 레이아웃 균형을 위한 빈 공간 */}
                  <div className="w-16"></div>
                </div>
              ))
            ) : !loading ? (
              <div className="w-full min-h-[350px] rounded-[32px] bg-[#EBF2FC] border border-red-50 flex flex-col items-center justify-center transition-all">

                {/* 부엉이 이미지 */}
                <img 
                  src={OwlIcon} 
                  alt="Owl" 
                  className="w-16 h-16 mb-2 object-contain" 
                />

                <p className="text-base font-bold text-[#000000]">랭킹 데이터가 없습니다.</p>
              </div>
            ) : null}
          </div>
        ) : (
          <div className="w-full min-h-[350px] rounded-[32px] bg-[#FFF5F5] border border-red-50 flex flex-col items-center justify-center transition-all">

            {/* 부엉이 이미지 */}
            <img 
              src={OwlIcon} 
              alt="Owl" 
              className="w-16 h-16 mb-2 object-contain" 
            />

            <p className="text-base font-bold text-[#991B1B]">로그인 후 이용해주세요.</p>
          </div>
        )}

      </div>
    </div>
  );
};

export default RankingSection;