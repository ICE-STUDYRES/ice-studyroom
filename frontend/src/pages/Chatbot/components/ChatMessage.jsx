const ChatMessage = ({
  isUser = false,
  type = "chat", // "chat" | "system"
  children,
  showActions = false,
  onActionClick,
  answerCard,
}) => {
  return (
    <div
      className={`flex ${
        isUser ? "justify-end" : "justify-start"
      } ${type === "system" ? "mb-4" : "mb-2"}`}
    >
      <div
        className={`max-w-[85%] rounded-xl px-4 py-3 text-sm leading-relaxed
        ${
          isUser
            ? "bg-[#C4E2ED] text-black"
            : "bg-gray-200 text-black"
        }`}
      >
        {children}

        {/* 봇 메시지 하단 액션 버튼 */}
        {!isUser && showActions && (
          <div className="flex gap-2 mt-2">
            <button
              className="text-xs text-gray-500"
              onClick={() => onActionClick("evidence")}
            >
              근거
            </button>
            <button
              className="text-xs text-gray-500"
              onClick={() => onActionClick("links")}
            >
              관련 링크
            </button>
            <button
              className="text-xs text-gray-500"
              onClick={() => onActionClick("support")}
            >
              추가문의
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatMessage;
