const ChatbotFooter = () => {
  return (
    <div className="
      border-t
      bg-gray-50
      px-4
      py-3
      text-xs
      flex
      justify-between
      items-center
    ">
      <button className="flex items-center gap-1 text-gray-600">
        🔄 카테고리 변경하기
      </button>

      <button className="flex items-center gap-1 text-gray-600">
        ☑ 대표 질문 다시 보기
      </button>
    </div>
  );
};

export default ChatbotFooter;
