const ChatMessage = ({
  isUser = false,
  type = "chat", // "chat" | "system"
  children,
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
      </div>
    </div>
  );
};

export default ChatMessage;
