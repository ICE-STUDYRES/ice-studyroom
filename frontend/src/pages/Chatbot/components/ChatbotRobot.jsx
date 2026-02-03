import robot from "../../../assets/images/chatbot/Chatbot Hi.png";

const ChatbotRobot = () => {
  return (
    <div className="flex flex-col items-center mt-4 mb-6">
      <span className="text-xs text-gray-400 mb-2">2026.01.17</span>
      <img src={robot} alt="chatbot" className="w-20 mb-2" />
      <h2 className="font-semibold text-base">무엇을 도와드릴까요?</h2>
    </div>
  );
};

export default ChatbotRobot;
