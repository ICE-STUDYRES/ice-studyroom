const buttons = [
  "예약",
  "체크인(QR)",
  "연장",
  "취소 / 변경",
  "이용시간 / 규정",
  "패널티 / 결제",
  "시설 / 장비",
  "기타",
];

const ChatbotButtons = () => {
  return (
    <div className="grid grid-cols-2 gap-3 mt-4 mb-6">
      {buttons.map((text) => (
        <button
          key={text}
          className="
            border border-gray-300
            rounded-lg
            py-2
            text-xs
            bg-white
            hover:bg-gray-50
          "
        >
          {text}
        </button>
      ))}
    </div>
  );
};

export default ChatbotButtons;
