import CommonButton from "./CommonButton";

const ChatbotFooter = ({ onResetCategory, onShowFaqAgain }) => {
  return (
    <div className="border-t bg-gray-100 px-4 py-2 text-xs text-gray-600">
      
      {/* 안내 문구 */}
      <p className="mb-2 text-center text-gray-500 leading-relaxed">
        AI 답변은 참고용이니 중요한 내용은 스터디룸 오픈채팅방을 이용해주세요.
      </p>

      {/* 버튼 영역 */}
      <div className="flex justify-between items-center px-10">
        <CommonButton
          variant="ghost"
          onClick={onResetCategory}
          className="bg-gray-100 text-gray-600 hover:bg-white transition"
        >
          카테고리 변경
        </CommonButton>

        <CommonButton
          variant="ghost"
          onClick={onShowFaqAgain}
          className="bg-gray-100 text-gray-600 hover:bg-white transition"
        >
          대표 질문 다시 보기
        </CommonButton>
      </div>
    </div>
  );
};

export default ChatbotFooter;
