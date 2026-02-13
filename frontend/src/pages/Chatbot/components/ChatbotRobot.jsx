import robot from "../../../assets/images/chatbot/Chatbot Hi.png";

const ChatbotRobot = () => {
  return (
    <div className="flex flex-col items-center mb-4">
      <img src={robot} alt="chatbot" className="w-20 mt-4 mb-4" />
      <h2 className="text-base mb-2">무엇을 도와드릴까요?</h2>
      <span className="text-xs text-gray-400">Today</span>
    </div>
  );
};

export default ChatbotRobot;
