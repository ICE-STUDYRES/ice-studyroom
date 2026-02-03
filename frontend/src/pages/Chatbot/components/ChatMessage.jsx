const ChatMessage = ({ isUser = false, children }) => {
  return (
    <div
      className={`flex ${
        isUser ? "justify-end" : "justify-start"
      } mb-2`}   // 간격 증가
    >
      <div
        className={`max-w-[85%] rounded-xl px-4 py-3 text-sm leading-relaxed
        ${isUser
          ? "bg-blue-500 text-white"
          : "bg-gray-100 text-black"
        }`}
      >
        {children}
      </div>
    </div>
  );
};

export default ChatMessage;
