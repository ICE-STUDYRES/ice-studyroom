import robot from "../../../assets/images/chatbot/Chatbot Hi.png";

const ChatbotRobot = () => {
  return (
    <div className="flex flex-col items-center mb-6">
      <img src={robot} alt="chatbot" className="w-20 mb-3" />
      <h2 className="font-semibold text-base mb-4">무엇을 도와드릴까요?</h2>
      <span className="text-xs text-gray-400">Today</span>
    </div>
  );
};

export default ChatbotRobot;
