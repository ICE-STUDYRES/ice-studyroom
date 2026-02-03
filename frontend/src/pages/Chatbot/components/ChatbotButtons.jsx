const buttons = [
  "예약",
  "체크인(QR)",
  "연장",
  "취소 / 변경",
  "이용시간 / 규정",
  "패널티 / 제재",
  "시설 / 장비",
  "기타",
];

const ChatbotButtons = ({ onSelect }) => {
  return (
    <div className="grid grid-cols-2 gap-3 mt-6 mb-4">
      {buttons.map((text) => (
        <button
          key={text}
          onClick={() => onSelect(text)}
          className="border border-gray-300 rounded-xl py-3 text-xs font-medium bg-white hover:bg-gray-50 transition"
        >
          {text}
        </button>
      ))}
    </div>
  );
};

export default ChatbotButtons;