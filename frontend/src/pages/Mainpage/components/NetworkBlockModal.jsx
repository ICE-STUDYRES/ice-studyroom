const ServiceBlockModal = () => {
  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/60">
      <div className="w-[90%] max-w-sm bg-white rounded-2xl p-6 shadow-xl text-center">
        <h2 className="text-lg font-semibold mb-3">
          📢 스터디룸 예약 제한 안내
        </h2>

        <p className="text-sm text-gray-600 mb-5 whitespace-pre-line">
{`현재 네트워크 문제로 인해
정보통신공학과 스터디룸 
예약 시스템을 이용하실 수 없습니다.

당분간 아래 오픈채팅방을 통해
예약 및 문의를 진행해주시기 바랍니다.

※ 아래 URL은 기존 오픈채팅방이 아닌
새로 개설된 오픈채팅방입니다.`}
        </p>

        <a
          href="https://open.kakao.com/o/g7sppaji"
          target="_blank"
          rel="noopener noreferrer"
          className="block w-full py-2 bg-yellow-400 rounded-lg font-medium hover:bg-yellow-500 transition-colors"
        >
          오픈채팅방 바로가기
        </a>
      </div>
    </div>
  );
};

export default ServiceBlockModal;