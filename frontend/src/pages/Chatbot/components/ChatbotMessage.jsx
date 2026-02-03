const ChatbotMessage = ({ text }) => {
  return (
    <div className="bg-gray-100 rounded-base px-4 py-3 max-w-[80%]">
      {text}
    </div>
  );
};

export default ChatbotMessage;
