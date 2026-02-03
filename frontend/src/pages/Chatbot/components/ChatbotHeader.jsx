import { useNavigate } from "react-router-dom";

const ChatbotHeader = () => {
  const navigate = useNavigate();

  return (
    <div className="relative flex items-center px-4 py-3 border-b">
      {/* 가운데 타이틀 */}
      <span className="absolute left-1/2 -translate-x-1/2 font-semibold text-sm">
        ice studyroom
      </span>

      {/* X 버튼 */}
      <button
        onClick={() => navigate("/")}
        className="ml-auto text-lg"
      >
        ✕
      </button>
    </div>
  );
};

export default ChatbotHeader;
