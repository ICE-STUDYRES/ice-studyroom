const ChatbotFooter = ({ onResetCategory, onShowFaqAgain }) => {
  return (
    <div className="border-t bg-gray-50 px-4 py-4 text-xs text-gray-600">
      
      {/* 안내 문구 */}
      <p className="mb-4 text-center text-gray-500 leading-relaxed">
        AI 답변은 참고용이니 중요한 내용은 스터디룸 오픈채팅방을 이용해주세요.
      </p>

      {/* 버튼 영역 */}
      <div className="flex justify-between items-center px-12">
        <button onClick={onResetCategory} className="flex items-center gap-1">
          🔮 카테고리 변경하기
        </button>

        <button onClick={onShowFaqAgain} className="flex items-center gap-1">
          🔍 대표 질문 다시 보기
        </button>
      </div>
    </div>
  );
};

export default ChatbotFooter;
