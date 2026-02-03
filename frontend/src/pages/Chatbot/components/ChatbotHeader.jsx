import { useNavigate } from "react-router-dom";

const ChatbotHeader = () => {
  const navigate = useNavigate();

  return (
    <div className="relative flex items-center px-4 py-3 border-b">
      <span className="absolute left-1/2 -translate-x-1/2 font-semibold text-sm text-gray-800">
        ice studyroom
      </span>
      <button
        onClick={() => navigate("/")}
        className="ml-auto text-2xl text-gray-500"
      >
        ✕
      </button>
    </div>
  );
};

export default ChatbotHeader;