import robot from "../../../assets/images/chatbot/Chatbot hi.png";

const ChatbotRobot = () => {
  return (
    <div className="flex flex-col items-center mb-4">
      <img src={robot} alt="chatbot" className="w-20 mt-4 mb-4" />
      <span className="text-xs font-bold text-blue-600 mb-2">Today</span>
      <h2 className="text-base">무엇을 도와드릴까요?</h2>
    </div>
  );
};

export default ChatbotRobot;
