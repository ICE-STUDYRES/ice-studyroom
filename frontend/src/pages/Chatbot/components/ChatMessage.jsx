import CommonButton from "./CommonButton";

const ChatMessage = ({
  isUser = false,
  type = "chat", // "chat" | "system"
  children,
  showActions = false,
  onActionClick,
  answerCard,
}) => {
  // 문자열이면 자동으로 줄바꿈 추가
  const formattedText =
    typeof children === "string"
      ? children.endsWith("\n")
        ? children
        : children + "\n"
      : children;

  return (
    <div
      className={`flex ${
        isUser ? "justify-end" : "justify-start"
      } ${type === "system" ? "mb-4" : "mb-2"}`}
    >
      <div
        className={`max-w-[80%] rounded-xl px-4 py-3 text-sm leading-relaxed whitespace-pre-line
        ${
          isUser
            ? "bg-blue-200 text-gray-900 rounded-2xl rounded-br-none"
            : "bg-gray-200 text-gray-900 rounded-2xl rounded-bl-none"
        }`}
      >
        {formattedText}

        {/* 봇 메시지 하단 액션 버튼 */}
        {!isUser && showActions && (
          <div className="flex gap-2 mt-3">
            <CommonButton
              variant="ghost"
              onClick={() => onActionClick("evidence")}
            >
              근거
            </CommonButton>

            <CommonButton
              variant="ghost"
              onClick={() => onActionClick("links")}
            >
              관련 링크
            </CommonButton>

            <CommonButton
              variant="ghost"
              onClick={() => onActionClick("support")}
            >
              추가문의
            </CommonButton>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatMessage;
